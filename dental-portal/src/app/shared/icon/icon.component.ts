import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Icons } from '../icons';

@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span [innerHTML]="getSafeIcon()" [class]="iconClass"></span>
  `
})
export class IconComponent {
  @Input() name!: keyof typeof Icons;
  @Input() class: string = '';

  constructor(private sanitizer: DomSanitizer) {}

  get iconClass(): string {
    return `inline-block ${this.class}`;
  }

  getSafeIcon(): SafeHtml {
    if (!this.name || !Icons[this.name]) {
      console.warn(`Icon "${this.name}" not found`);
      return this.sanitizer.bypassSecurityTrustHtml('');
    }
    return this.sanitizer.bypassSecurityTrustHtml(Icons[this.name]);
  }
}