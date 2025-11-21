import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TreatmentDetailResponse } from '../../interfaces/treatment.interface';
import { TreatmentSessionDialogComponent } from './treatment-session-dialog.component';
import { MatDialog } from '@angular/material/dialog';

export interface TreatmentDetailData {
  treatment: TreatmentDetailResponse;
  dentistId: number;
  patientId: number;
}

@Component({
  selector: 'app-treatment-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './treatment-detail-dialog.component.html',
  styleUrl: './treatment-detail-dialog.component.css'
})
export class TreatmentDetailDialogComponent implements OnInit {
  treatment: TreatmentDetailResponse;

  constructor(
    public dialogRef: MatDialogRef<TreatmentDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TreatmentDetailData,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.treatment = data.treatment;
  }

  ngOnInit() {}

  formatDate(dateStr: string | null): string {
    if (!dateStr) return 'No especificada';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      const year = parseInt(parts[0]);
      const month = parseInt(parts[1]) - 1;
      const day = parseInt(parts[2]);
      const date = new Date(year, month, day);
      return date.toLocaleDateString('es-ES');
    }
    return dateStr;
  }

  getProgressPercentage(): number {
    if (!this.treatment.totalSessions || this.treatment.totalSessions === 0) return 0;
    return Math.round((this.treatment.completedSessions / this.treatment.totalSessions) * 100);
  }

  getProgressColor(): string {
    const percentage = this.getProgressPercentage();
    if (percentage >= 80) return 'primary';
    if (percentage >= 50) return 'accent';
    return 'warn';
  }

  getStatusLabel(status: string): string {
    const statusUpper = status?.toUpperCase() || '';
    if (statusUpper === 'COMPLETADO') return 'Completado';
    if (statusUpper === 'EN_CURSO' || statusUpper === 'EN CURSO') return 'En Curso';
    if (statusUpper === 'ABANDONADO') return 'Abandonado';
    return status || 'Sin estado';
  }

  getStatusColor(status: string): string {
    const statusUpper = status?.toUpperCase() || '';
    if (statusUpper === 'COMPLETADO') return 'status-completed';
    if (statusUpper === 'EN_CURSO' || statusUpper === 'EN CURSO') return 'status-in-progress';
    if (statusUpper === 'ABANDONADO') return 'status-abandoned';
    return 'status-default';
  }

  openAddSessionDialog() {
    const dialogRef = this.dialog.open(TreatmentSessionDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: {
        dentistId: this.data.dentistId,
        patientId: this.data.patientId,
        treatmentId: this.treatment.id,
        treatmentName: this.treatment.name
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Recargar el detalle del tratamiento
        this.dialogRef.close(true);
      }
    });
  }

  onClose() {
    this.dialogRef.close(false);
  }
}

