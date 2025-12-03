import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDeleteOdontogramData {
  patientName: string;
  odontogramDate: string;
  isRecent: boolean;
}

@Component({
  selector: 'app-confirm-delete-odontogram-dialog',
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
          <h2 class="text-2xl font-bold text-blue-800 mb-1">
            Eliminar Odontograma
          </h2>
          <p class="text-gray-600 text-sm">
            Esta acción no se puede deshacer
          </p>
        </div>
      </div>

      <div class="bg-gray-50 rounded-lg p-4 mb-6">
        <p class="text-gray-700 mb-2">
          <span class="font-semibold">Paciente:</span> {{ data.patientName }}
        </p>
        <p class="text-gray-700 mb-2">
          <span class="font-semibold">Fecha:</span> {{ data.odontogramDate }}
        </p>
        <span 
          *ngIf="data.isRecent" 
          class="inline-block bg-blue-100 text-blue-700 text-xs px-2 py-1 rounded"
        >
          Más reciente
        </span>
      </div>

      <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
        <div class="flex items-start gap-3">
          <mat-icon class="text-yellow-600 mt-0.5">warning</mat-icon>
          <div>
            <p class="text-sm text-yellow-800 font-medium mb-1">
              Al eliminar este odontograma:
            </p>
            <ul class="text-sm text-yellow-700 list-disc list-inside space-y-1">
              <li>Se perderá permanentemente toda la información</li>
              <li>No podrás recuperar los datos más tarde</li>
              <li *ngIf="data.isRecent">Este es el odontograma más reciente del paciente</li>
            </ul>
          </div>
        </div>
      </div>

      <div class="flex justify-end gap-3">
        <button
          mat-button
          (click)="onCancel()"
          class="px-6"
        >
          Cancelar
        </button>
        <button
          mat-raised-button
          color="warn"
          (click)="onConfirm()"
          class="px-6"
        >
          <mat-icon class="mr-2">delete</mat-icon>
          Eliminar
        </button>
      </div>
    </div>
  `
})
export class ConfirmDeleteOdontogramDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteOdontogramDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDeleteOdontogramData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}

