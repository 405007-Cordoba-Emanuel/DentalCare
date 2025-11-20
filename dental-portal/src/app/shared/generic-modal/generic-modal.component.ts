import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { IconComponent } from '../icon/icon.component';

export type ModalType = 'confirm' | 'success' | 'error' | 'warning' | 'info';

export interface GenericModalData {
  type: ModalType;
  title: string;
  message: string;
  details?: string[];
  confirmText?: string;
  cancelText?: string;
  showCancel?: boolean;
}

@Component({
  selector: 'app-generic-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    IconComponent
  ],
  templateUrl: './generic-modal.component.html',
  styleUrls: ['./generic-modal.component.css']
})
export class GenericModalComponent {
  constructor(
    public dialogRef: MatDialogRef<GenericModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: GenericModalData
  ) {
    // Valores por defecto
    this.data.confirmText = this.data.confirmText || this.getDefaultConfirmText();
    this.data.cancelText = this.data.cancelText || 'Cancelar';
    this.data.showCancel = this.data.showCancel !== undefined ? this.data.showCancel : this.data.type === 'confirm';
  }

  private getDefaultConfirmText(): string {
    switch (this.data.type) {
      case 'confirm':
        return 'Confirmar';
      case 'success':
        return 'Aceptar';
      case 'error':
        return 'Entendido';
      case 'warning':
        return 'Continuar';
      case 'info':
        return 'Aceptar';
      default:
        return 'Aceptar';
    }
  }

  getIconConfig(): { name: string; bgColor: string; iconColor: string } {
    switch (this.data.type) {
      case 'confirm':
        return { name: 'help', bgColor: 'bg-blue-100', iconColor: 'text-blue-600' };
      case 'success':
        return { name: 'check', bgColor: 'bg-green-100', iconColor: 'text-green-600' };
      case 'error':
        return { name: 'alertCircle', bgColor: 'bg-red-100', iconColor: 'text-red-600' };
      case 'warning':
        return { name: 'alertTriangle', bgColor: 'bg-yellow-100', iconColor: 'text-yellow-600' };
      case 'info':
        return { name: 'info', bgColor: 'bg-blue-100', iconColor: 'text-blue-600' };
      default:
        return { name: 'info', bgColor: 'bg-gray-100', iconColor: 'text-gray-600' };
    }
  }

  getButtonColorClass(): string {
    switch (this.data.type) {
      case 'confirm':
        return 'dental-btn-primary';
      case 'success':
        return 'dental-btn-accent';
      case 'error':
        return 'dental-btn-warn';
      case 'warning':
        return 'dental-btn-secondary';
      default:
        return 'dental-btn-primary';
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
