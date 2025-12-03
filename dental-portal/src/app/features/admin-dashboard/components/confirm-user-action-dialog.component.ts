import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmUserActionData {
  userName: string;
  userEmail: string;
  isActive: boolean;
  action: 'activate' | 'deactivate';
}

@Component({
  selector: 'app-confirm-user-action-dialog',
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
        <div
          class="w-16 h-16 rounded-full flex items-center justify-center"
          [ngClass]="
            data.action === 'activate'
              ? 'bg-green-100'
              : 'bg-red-100'
          "
        >
          <mat-icon
            class="text-3xl"
            [ngClass]="
              data.action === 'activate'
                ? 'text-green-600'
                : 'text-red-600'
            "
          >
            {{ data.action === 'activate' ? 'check_circle' : 'cancel' }}
          </mat-icon>
        </div>
        <div class="flex-1">
          <h2 class="text-2xl font-bold text-blue-800 mb-1">
            {{ data.action === 'activate' ? 'Activar Usuario' : 'Desactivar Usuario' }}
          </h2>
          <p class="text-gray-600 text-sm">
            ¿Está seguro de esta acción?
          </p>
        </div>
      </div>

      <div class="bg-gray-50 rounded-lg p-4 mb-6">
        <p class="text-gray-700 mb-2">
          <span class="font-semibold">Usuario:</span> {{ data.userName }}
        </p>
        <p class="text-gray-700">
          <span class="font-semibold">Email:</span> {{ data.userEmail }}
        </p>
      </div>

      <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
        <div class="flex items-start gap-3">
          <mat-icon class="text-yellow-600 mt-0.5">warning</mat-icon>
          <div>
            <p class="text-sm text-yellow-800 font-medium mb-1">
              {{ data.action === 'activate' ? 'Al activar este usuario:' : 'Al desactivar este usuario:' }}
            </p>
            <ul class="text-sm text-yellow-700 list-disc list-inside space-y-1">
              <li *ngIf="data.action === 'activate'">
                El usuario podrá iniciar sesión nuevamente
              </li>
              <li *ngIf="data.action === 'activate'">
                Tendrá acceso a todas las funcionalidades según su rol
              </li>
              <li *ngIf="data.action === 'deactivate'">
                El usuario no podrá iniciar sesión
              </li>
              <li *ngIf="data.action === 'deactivate'">
                Perderá acceso a todas las funcionalidades del sistema
              </li>
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
          (click)="onConfirm()"
          [color]="data.action === 'activate' ? 'primary' : 'warn'"
          class="px-6"
        >
          <mat-icon class="mr-2">{{ data.action === 'activate' ? 'check' : 'block' }}</mat-icon>
          {{ data.action === 'activate' ? 'Activar' : 'Desactivar' }}
        </button>
      </div>
    </div>
  `
})
export class ConfirmUserActionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmUserActionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmUserActionData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}

