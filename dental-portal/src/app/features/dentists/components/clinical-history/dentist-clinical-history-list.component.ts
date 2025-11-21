import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MAT_DATE_LOCALE } from '@angular/material/core';
import { provideNativeDateAdapter } from '@angular/material/core';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';
import { DentistClinicalHistoryService, ClinicalHistoryEntry } from '../../services/clinical-history.service';
import { PatientService } from '../../../../core/services/patient.service';
import { User } from '../../../../interfaces/user/user.interface';
import { ClinicalHistoryFormDialogComponent, ClinicalHistoryFormData } from './clinical-history-form-dialog.component';
import { ConfirmDeleteDialogComponent, ConfirmDeleteData } from '../prescriptions/confirm-delete-dialog.component';
import { ImageViewerDialogComponent, ImageViewerData } from '../../../../shared/components/image-viewer-dialog.component';

@Component({
  selector: 'app-dentist-clinical-history-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [
    provideNativeDateAdapter(),
    { provide: MAT_DATE_LOCALE, useValue: 'es-AR' }
  ],
  templateUrl: './dentist-clinical-history-list.component.html',
  styleUrl: './dentist-clinical-history-list.component.css'
})
export class DentistClinicalHistoryListComponent implements OnInit {
  private clinicalHistoryService = inject(DentistClinicalHistoryService);
  private localStorage = inject(LocalStorageService);
  private patientService = inject(PatientService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  user: User | null = null;
  patientId: number | null = null;
  dentistId: number | null = null;
  entries: ClinicalHistoryEntry[] = [];
  filteredEntries: ClinicalHistoryEntry[] = [];
  isLoading = false;
  searchTerm = '';
  showAdvancedSearch = false;
  startDate: Date | null = null;
  endDate: Date | null = null;
  deletingEntryIds = new Set<number>(); // Rastrear entradas que se están eliminando

  // Información del paciente
  patientInfo = {
    firstName: 'Cargando',
    lastName: '...',
    dni: '...'
  };

  ngOnInit() {
    this.loadData();
  }

  private loadData() {
    this.isLoading = true;

    // Obtener patientId de la ruta
    this.route.params.subscribe(params => {
      this.patientId = +params['patientId'];
      if (this.patientId) {
        this.loadUserAndDentist();
      } else {
        this.isLoading = false;
      }
    });
  }

  private loadUserAndDentist() {
    // Obtener usuario desde localStorage
    const userStr = this.localStorage.getUserData();
    if (!userStr) {
      console.error('No hay usuario autenticado');
      this.isLoading = false;
      return;
    }

    this.user = JSON.parse(userStr);

    // Obtener dentistId
    if (this.user?.dentistId) {
      this.dentistId = this.user.dentistId;
      this.loadPatientInfo();
    } else if (this.user?.id) {
      // Si no tiene dentistId, obtenerlo usando userId
      const userId = parseInt(this.user.id, 10);
      if (!isNaN(userId)) {
        // Aquí deberías tener un servicio para obtener el dentistId por userId
        // Por ahora asumo que viene en el user
        this.isLoading = false;
      }
    } else {
      this.isLoading = false;
    }
  }

  private loadPatientInfo() {
    if (!this.patientId || !this.dentistId) {
      this.isLoading = false;
      return;
    }

    this.patientService.getPatientById(this.patientId).subscribe({
      next: (patient) => {
        this.patientInfo = {
          firstName: patient.firstName || '',
          lastName: patient.lastName || '',
          dni: patient.dni || ''
        };
        this.loadClinicalHistory();
      },
      error: (error) => {
        console.error('Error al cargar información del paciente:', error);
        this.snackBar.open('Error al cargar información del paciente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.isLoading = false;
      }
    });
  }

  private loadClinicalHistory() {
    if (!this.patientId || !this.dentistId) {
      this.isLoading = false;
      return;
    }

    this.clinicalHistoryService.getClinicalHistoryByPatient(this.dentistId, this.patientId).subscribe({
      next: (data: ClinicalHistoryEntry[]) => {
        // Ordenar por fecha descendente (más reciente primero)
        this.entries = data.sort((a: ClinicalHistoryEntry, b: ClinicalHistoryEntry) =>
          new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime()
        );
        this.filteredEntries = this.entries;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error al cargar historia clínica:', error);
        this.snackBar.open('Error al cargar historia clínica', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.isLoading = false;
      }
    });
  }

  toggleAdvancedSearch() {
    this.showAdvancedSearch = !this.showAdvancedSearch;
    if (!this.showAdvancedSearch) {
      this.clearAllFilters();
    }
  }

  onSearchChange() {
    this.performSearch();
  }

  onDateRangeSearch() {
    if (!this.patientId || !this.dentistId || !this.startDate || !this.endDate) return;

    // Validar que la fecha de inicio sea antes que la de fin
    if (this.startDate > this.endDate) {
      this.snackBar.open('La fecha de inicio debe ser anterior a la fecha de fin', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    const startDateStr = this.formatDateForSearch(this.startDate);
    const endDateStr = this.formatDateForSearch(this.endDate);

    this.clinicalHistoryService.searchClinicalHistoryByDateRange(
      this.dentistId!,
      this.patientId!,
      startDateStr,
      endDateStr
    ).subscribe({
      next: (data: ClinicalHistoryEntry[]) => {
        this.filteredEntries = data.sort((a: ClinicalHistoryEntry, b: ClinicalHistoryEntry) =>
          new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime()
        );
      },
      error: (error: any) => {
        console.error('Error al buscar por rango de fechas:', error);
        this.filteredEntries = [];
      }
    });
  }

  clearDateRangeFilter() {
    this.startDate = null;
    this.endDate = null;
    this.performSearch();
  }

  clearAllFilters() {
    this.searchTerm = '';
    this.startDate = null;
    this.endDate = null;
    this.filteredEntries = this.entries;
  }

  private performSearch() {
    // Si hay filtros de fecha activos, no hacer búsqueda por texto
    if (this.startDate && this.endDate) {
      return; // Los filtros de fecha se manejan por separado
    }

    if (!this.searchTerm.trim()) {
      this.filteredEntries = this.entries;
      return;
    }

    const search = this.searchTerm.toLowerCase().trim();

    if (search.length > 0 && this.patientId && this.dentistId) {
      this.clinicalHistoryService.searchClinicalHistoryByText(this.dentistId, this.patientId, search).subscribe({
        next: (data: ClinicalHistoryEntry[]) => {
          this.filteredEntries = data.sort((a: ClinicalHistoryEntry, b: ClinicalHistoryEntry) =>
            new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime()
          );
        },
        error: (error: any) => {
          console.error('Error al buscar:', error);
          // Fallback a búsqueda local
          this.filterLocally(search);
        }
      });
    } else {
      this.filteredEntries = this.entries;
    }
  }

  private filterLocally(search: string) {
    this.filteredEntries = this.entries.filter(entry =>
      entry.description?.toLowerCase().includes(search) ||
      entry.dentistName?.toLowerCase().includes(search)
    );
  }

  private formatDateForSearch(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  openCreateEntryDialog() {
    if (!this.dentistId || !this.patientId) return;

    const dialogRef = this.dialog.open(ClinicalHistoryFormDialogComponent, {
      width: '700px',
      maxWidth: '90vw',
      data: {
        dentistId: this.dentistId,
        patientId: this.patientId,
        patientName: `${this.patientInfo.firstName} ${this.patientInfo.lastName}`,
        entry: null
      } as ClinicalHistoryFormData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadClinicalHistory();
      }
    });
  }

  openEditEntryDialog(entry: ClinicalHistoryEntry) {
    if (!this.dentistId || !this.patientId) return;

    const dialogRef = this.dialog.open(ClinicalHistoryFormDialogComponent, {
      width: '700px',
      maxWidth: '90vw',
      data: {
        dentistId: this.dentistId,
        patientId: this.patientId,
        patientName: `${this.patientInfo.firstName} ${this.patientInfo.lastName}`,
        entry: entry
      } as ClinicalHistoryFormData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadClinicalHistory();
      }
    });
  }

  confirmDeleteEntry(entry: ClinicalHistoryEntry) {
    if (!this.dentistId) return;

    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '500px',
      data: {
        title: 'Eliminar Entrada de Historia Clínica',
        message: '¿Está seguro de eliminar esta entrada?',
        itemName: 'Entrada',
        itemDetails: {
          date: this.formatDate(entry.entryDate)
        }
      } as ConfirmDeleteData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.deleteEntry(entry.id);
      }
    });
  }

  private deleteEntry(entryId: number) {
    if (!this.dentistId || this.deletingEntryIds.has(entryId)) return;

    this.deletingEntryIds.add(entryId);

    this.clinicalHistoryService.deleteClinicalHistoryEntry(this.dentistId, entryId).subscribe({
      next: () => {
        this.snackBar.open('Entrada eliminada exitosamente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.loadClinicalHistory();
        this.deletingEntryIds.delete(entryId);
      },
      error: (error: any) => {
        console.error('Error al eliminar entrada:', error);
        this.snackBar.open('Error al eliminar la entrada', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.deletingEntryIds.delete(entryId);
      }
    });
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
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  formatShortDate(dateStr: string): string {
    if (!dateStr) return '';
    // Parse date string manually to avoid timezone issues
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]) - 1; // Month is 0-indexed
    const day = parseInt(parts[2]);
    const date = new Date(year, month, day);
    return date.toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'short'
    });
  }

  getYear(dateStr: string): string {
    if (!dateStr) return '';
    // Parse date string manually to avoid timezone issues
    const parts = dateStr.split('T')[0].split('-');
    return parts[0];
  }

  getMonthName(dateStr: string): string {
    if (!dateStr) return '';
    // Parse date string manually to avoid timezone issues
    const parts = dateStr.split('T')[0].split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]) - 1; // Month is 0-indexed
    const day = parseInt(parts[2]);
    const date = new Date(year, month, day);
    return date.toLocaleDateString('es-ES', { month: 'long' });
  }

  shouldShowYearHeader(index: number): boolean {
    if (index === 0) return true;

    const currentEntry = this.filteredEntries[index];
    const previousEntry = this.filteredEntries[index - 1];

    const currentYear = this.getYear(currentEntry.entryDate);
    const previousYear = this.getYear(previousEntry.entryDate);

    return currentYear !== previousYear;
  }

  shouldShowMonthHeader(index: number): boolean {
    if (index === 0) return true;

    const currentEntry = this.filteredEntries[index];
    const previousEntry = this.filteredEntries[index - 1];

    // Parse dates manually to avoid timezone issues
    const currentParts = currentEntry.entryDate.split('T')[0].split('-');
    const previousParts = previousEntry.entryDate.split('T')[0].split('-');

    const currentMonth = parseInt(currentParts[1]);
    const currentYear = parseInt(currentParts[0]);
    const previousMonth = parseInt(previousParts[1]);
    const previousYear = parseInt(previousParts[0]);

    return currentMonth !== previousMonth || currentYear !== previousYear;
  }

  getInitials(): string {
    const first = this.patientInfo.firstName?.charAt(0) || '';
    const last = this.patientInfo.lastName?.charAt(0) || '';
    return (first + last).toUpperCase();
  }

  isImageFile(fileType: string): boolean {
    return fileType.startsWith('image/');
  }

  openImageDialog(imageUrl: string, imageName?: string) {
    this.dialog.open(ImageViewerDialogComponent, {
      width: '90vw',
      maxWidth: '1200px',
      maxHeight: '90vh',
      data: {
        imageUrl,
        imageName
      } as ImageViewerData
    });
  }
}

