import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ClinicalHistoryEntry } from '../../core/services/clinical-history.service';

@Component({
  selector: 'app-clinical-history-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatChipsModule
  ],
  templateUrl: './clinical-history-detail-dialog.component.html',
  styleUrl: './clinical-history-detail-dialog.component.css'
})
export class ClinicalHistoryDetailDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ClinicalHistoryDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public entry: ClinicalHistoryEntry,
    private sanitizer: DomSanitizer
  ) {}

  close(): void {
    this.dialogRef.close();
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'No especificada';
    // Parse date string manually to avoid timezone issues
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]) - 1; // Month is 0-indexed
    const day = parseInt(parts[2]);
    const date = new Date(year, month, day);
    return date.toLocaleDateString('es-ES', { 
      weekday: 'long',
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    });
  }

  getFileUrl(): SafeUrl | null {
    if (!this.entry.fileUrl) return null;
    return this.sanitizer.bypassSecurityTrustUrl(this.entry.fileUrl);
  }

  isImageFile(): boolean {
    if (!this.entry.fileType) return false;
    return this.entry.fileType.startsWith('image/');
  }

  isPdfFile(): boolean {
    if (!this.entry.fileType) return false;
    return this.entry.fileType === 'application/pdf';
  }

  downloadFile(): void {
    if (this.entry.fileUrl) {
      window.open(this.entry.fileUrl, '_blank');
    }
  }
}

