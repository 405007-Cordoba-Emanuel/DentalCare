import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDeleteData {
  title: string;
  message: string;
  itemName: string;
  itemDetails: {
    patientName?: string;
    date?: string;
    [key: string]: any;
  };
}

@Component({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="p-6 max-w-md">
      <div class="flex items-center gap-4 mb-6">
        <div class="w-16 h-16 rounded-full flex items-center justify-center bg-red-100">
          <mat-icon class="text-3xl text-red-600">delete_forever</mat-icon>
        </div>
        <div class="flex-1">
          <h2 class="text-2xl font-bold text-blue-800 mb-1">{{ data.title }}</h2>
          <p class="text-gray-600 text-sm">{{ data.message }}</p>
        </div>
      </div>

      <div class="bg-gray-50 rounded-lg p-4 mb-6">
        @if (data.itemDetails.patientName) {
          <p class="text-gray-700 mb-2">
            <span class="font-semibold">Paciente:</span> {{ data.itemDetails.patientName }}
          </p>
        }
        @if (data.itemDetails.date) {
          <p class="text-gray-700">
            <span class="font-semibold">Fecha:</span> {{ data.itemDetails.date }}
          </p>
        }
      </div>

      <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
        <div class="flex items-start gap-3">
          <mat-icon class="text-yellow-600 flex-shrink-0" style="font-size: 24px; width: 24px; height: 24px; margin-top: 2px;">warning</mat-icon>
          <div class="flex-1">
            <p class="text-sm text-yellow-800 font-medium mb-1">
              Al eliminar este {{ data.itemName.toLowerCase() }}:
            </p>
            <ul class="text-sm text-yellow-700 list-disc list-inside space-y-1">
              <li>El registro será marcado como inactivo y no se mostrará en el historial.</li>
              <li>Esta acción no se puede deshacer.</li>
            </ul>
          </div>
        </div>
      </div>

      <div class="flex justify-end gap-3">
        <button mat-button (click)="onCancel()" class="px-6">
          Cancelar
        </button>
        <button mat-raised-button color="warn" (click)="onConfirm()" class="px-6">
          <mat-icon class="mr-2">delete</mat-icon>
          Eliminar
        </button>
      </div>
    </div>
  `
})
export class ConfirmDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDeleteData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}

