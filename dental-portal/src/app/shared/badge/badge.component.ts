import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

type BadgeVariant = 'ausente' | 'en-curso' | 'completado' | 'abandonado' | 'default';

@Component({
  selector: 'app-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span [class]="getBadgeClasses()">
      {{ text() }}
    </span>
  `
})
export class BadgeComponent {
  text = input.required<string>();
  variant = input<BadgeVariant>('default');

  getBadgeClasses(): string {
    const variant = this.variant();
    const baseClasses = 'border rounded-full px-3 py-1 text-xs';
    
    switch (variant) {
      case 'ausente':
        return `${baseClasses} bg-amber-200 text-amber-800 border-amber-300`;
      case 'en-curso':
        return `${baseClasses} bg-blue-200 text-blue-800 border-blue-300`;
      case 'completado':
        return `${baseClasses} bg-green-200 text-green-800 border-green-300`;
      case 'abandonado':
        return `${baseClasses} bg-red-200 text-red-800 border-red-300`;
      case 'default':
      default:
        return `${baseClasses} bg-gray-200 text-gray-800 border-gray-300`;
    }
  }
}