import { Injectable, inject } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { GenericModalComponent, GenericModalData, ModalType } from './generic-modal.component';

@Injectable({
  providedIn: 'root'
})
export class GenericModalService {
  private dialog = inject(MatDialog);

  /**
   * Abre un modal genérico
   */
  open(data: GenericModalData): MatDialogRef<GenericModalComponent> {
    return this.dialog.open(GenericModalComponent, {
      data,
      width: '500px',
      disableClose: data.type === 'confirm',
      panelClass: 'custom-modal-panel'
    });
  }

  /**
   * Abre un modal de confirmación
   */
  confirm(
    title: string,
    message: string,
    details?: string[],
    confirmText?: string,
    cancelText?: string
  ): Observable<boolean> {
    const dialogRef = this.open({
      type: 'confirm',
      title,
      message,
      details,
      confirmText,
      cancelText,
      showCancel: true
    });

    return dialogRef.afterClosed();
  }

  /**
   * Abre un modal de éxito
   */
  success(
    title: string,
    message: string,
    details?: string[],
    confirmText?: string
  ): Observable<boolean> {
    const dialogRef = this.open({
      type: 'success',
      title,
      message,
      details,
      confirmText,
      showCancel: false
    });

    return dialogRef.afterClosed();
  }

  /**
   * Abre un modal de error
   */
  error(
    title: string,
    message: string,
    details?: string[],
    confirmText?: string
  ): Observable<boolean> {
    const dialogRef = this.open({
      type: 'error',
      title,
      message,
      details,
      confirmText,
      showCancel: false
    });

    return dialogRef.afterClosed();
  }

  /**
   * Abre un modal de advertencia
   */
  warning(
    title: string,
    message: string,
    details?: string[],
    confirmText?: string,
    showCancel: boolean = false
  ): Observable<boolean> {
    const dialogRef = this.open({
      type: 'warning',
      title,
      message,
      details,
      confirmText,
      showCancel
    });

    return dialogRef.afterClosed();
  }

  /**
   * Abre un modal informativo
   */
  info(
    title: string,
    message: string,
    details?: string[],
    confirmText?: string
  ): Observable<boolean> {
    const dialogRef = this.open({
      type: 'info',
      title,
      message,
      details,
      confirmText,
      showCancel: false
    });

    return dialogRef.afterClosed();
  }
}

