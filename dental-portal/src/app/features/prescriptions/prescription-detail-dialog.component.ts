import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { Prescription } from '../../core/services/prescription.service';

@Component({
  selector: 'app-prescription-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule
  ],
  templateUrl: './prescription-detail-dialog.component.html',
  styleUrl: './prescription-detail-dialog.component.css'
})
export class PrescriptionDetailDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<PrescriptionDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public prescription: Prescription
  ) {}

  close(): void {
    this.dialogRef.close();
  }

  getInitials(name: string): string {
    if (!name) return '';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'No especificada';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-ES');
  }

  getMedicationsList(): string[] {
    if (!this.prescription.medications) return [];
    const lines = this.prescription.medications.split('\n').filter(line => line.trim());
    if (lines.length > 0) return lines;
    return this.prescription.medications.split(',').map(m => m.trim()).filter(m => m);
  }

  hasObservations(): boolean {
    return !!this.prescription.observations && this.prescription.observations.trim().length > 0;
  }
}

