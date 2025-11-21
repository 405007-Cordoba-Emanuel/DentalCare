import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ImageViewerData {
  imageUrl: string;
  imageName?: string;
}

@Component({
  selector: 'app-image-viewer-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="image-viewer-dialog">
      <button mat-icon-button (click)="onClose()" class="close-button">
        <mat-icon>close</mat-icon>
      </button>
      
      <mat-dialog-content class="dialog-content">
        <div class="image-container">
          <img [src]="data.imageUrl" [alt]="data.imageName || 'Imagen'" class="viewer-image">
        </div>
      </mat-dialog-content>
    </div>
  `,
  styles: [`
    .image-viewer-dialog {
      max-width: 90vw;
      max-height: 90vh;
      display: flex;
      flex-direction: column;
      position: relative;
    }

    .close-button {
      position: absolute;
      top: 8px;
      right: 8px;
      z-index: 1000;
      background: rgba(0, 0, 0, 0.5);
      color: white;
    }

    .close-button:hover {
      background: rgba(0, 0, 0, 0.7);
    }

    .dialog-content {
      padding: 0;
      overflow: auto;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 400px;
      max-height: 90vh;
    }

    .image-container {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      padding: 24px;
    }

    .viewer-image {
      max-width: 100%;
      max-height: 90vh;
      object-fit: contain;
      border-radius: 8px;
    }
  `]
})
export class ImageViewerDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ImageViewerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ImageViewerData
  ) {}

  onClose(): void {
    this.dialogRef.close();
  }

}

