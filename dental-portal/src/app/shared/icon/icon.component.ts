import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Icons } from '../icons';

@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [CommonModule],
  template: '<span [innerHTML]="iconHtml"></span>',
  styles: ['span { display: inline-block; }']
})
export class IconComponent {
  @Input() name: keyof typeof Icons = 'shield';
  @Input() size: string = '24';
  
  constructor(private sanitizer: DomSanitizer) {}
  
  get iconHtml(): SafeHtml {
    const icon = Icons[this.name];
    if (!icon) return '';
    
    // Ajustar el tamaño del icono
    const sizedIcon = icon.replace(/width="24"/, `width="${this.size}"`).replace(/height="24"/, `height="${this.size}"`);
    return this.sanitizer.bypassSecurityTrustHtml(sizedIcon);
  }
}
