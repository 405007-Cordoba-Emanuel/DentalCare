import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { ClinicalHistoryService, ClinicalHistoryEntry } from '../../core/services/clinical-history.service';
import { PatientService } from '../../core/services/patient.service';
import { User } from '../../interfaces/user/user.interface';
import { ClinicalHistoryDetailDialogComponent } from './clinical-history-detail-dialog.component';
import { ImageViewerDialogComponent, ImageViewerData } from '../../shared/components/image-viewer-dialog.component';
import { TreatmentService } from '../../core/services/treatment.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TreatmentDetailDialogComponent, TreatmentDetailData } from '../../features/dentists/components/treatments/treatment-detail-dialog.component';
import { TreatmentDetailResponse } from '../../features/dentists/interfaces/treatment.interface';

@Component({
  selector: 'app-clinical-history-list',
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
    MatChipsModule,
    MatBadgeModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './clinical-history-list.component.html',
  styleUrl: './clinical-history-list.component.css'
})
export class ClinicalHistoryListComponent implements OnInit {
  private clinicalHistoryService = inject(ClinicalHistoryService);
  private localStorage = inject(LocalStorageService);
  private patientService = inject(PatientService);
  private treatmentService = inject(TreatmentService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  user: User | null = null;
  entries: ClinicalHistoryEntry[] = [];
  filteredEntries: ClinicalHistoryEntry[] = [];
  isLoading = false;
  searchTerm = '';
  showAdvancedSearch = false;
  startDate: Date | null = null;
  endDate: Date | null = null;

  ngOnInit() {
    this.loadData();
  }

  toggleAdvancedSearch() {
    this.showAdvancedSearch = !this.showAdvancedSearch;
    if (!this.showAdvancedSearch) {
      this.clearAllFilters();
    }
  }

  private loadData() {
    this.isLoading = true;

    // Obtener usuario desde localStorage
    const userStr = this.localStorage.getUserData();
    if (!userStr) {
      console.error('No hay usuario autenticado');
      this.isLoading = false;
      return;
    }

    this.user = JSON.parse(userStr);

    // Si no tiene patientId, obtenerlo usando userId
    if (!this.user?.patientId && this.user?.id) {
      this.patientService.getPatientIdByUserId(this.user.id).subscribe({
        next: (patientId) => {
          this.user!.patientId = patientId;
          // Actualizar en localStorage
          this.localStorage.setUserData(this.user!);
          this.loadClinicalHistory();
        },
        error: (error) => {
          console.error('Error al obtener patientId:', error);
          this.isLoading = false;
        }
      });
      return;
    }

    // Si ya tiene patientId, cargar directamente
    if (this.user?.patientId) {
      this.loadClinicalHistory();
    } else {
      console.error('No se pudo obtener patientId');
      this.isLoading = false;
    }
  }

  private loadClinicalHistory() {
    if (!this.user?.patientId) {
      this.isLoading = false;
      return;
    }

    this.clinicalHistoryService.getClinicalHistoryByPatientId(this.user.patientId).subscribe({
      next: (data) => {
        // Ordenar por fecha descendente (más reciente primero)
        this.entries = data.sort((a, b) => 
          new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime()
        );
        this.filteredEntries = this.entries;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar historia clínica:', error);
        this.isLoading = false;
      }
    });
  }

  onSearchChange() {
    this.performSearch();
  }

  onDateRangeSearch() {
    if (!this.user?.patientId || !this.startDate || !this.endDate) return;
    
    // Validar que la fecha de inicio sea antes que la de fin
    if (this.startDate > this.endDate) {
      console.warn('La fecha de inicio debe ser anterior a la fecha de fin');
      return;
    }
    
    const startDateStr = this.formatDateForSearch(this.startDate);
    const endDateStr = this.formatDateForSearch(this.endDate);
    
    console.log('Buscando por rango de fechas:', { startDate: startDateStr, endDate: endDateStr });
    
    this.clinicalHistoryService.searchClinicalHistoryByDateRange(
      this.user.patientId, 
      startDateStr, 
      endDateStr
    ).subscribe({
      next: (data) => {
        console.log('Resultados recibidos:', data.length, 'entradas');
        this.filteredEntries = data.sort((a, b) => 
          new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime()
        );
      },
      error: (error) => {
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
    
    if (search.length > 0 && this.user?.patientId) {
      this.clinicalHistoryService.searchClinicalHistoryByText(this.user.patientId, search).subscribe({
        next: (data) => {
          this.filteredEntries = data.sort((a, b) => 
            new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime()
          );
        },
        error: (error) => {
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

  openEntryDetail(entry: ClinicalHistoryEntry) {
    if (!this.user?.patientId) return;

    this.dialog.open(ClinicalHistoryDetailDialogComponent, {
      width: '700px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      data: entry
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

  getPreviewDescription(description: string): string {
    if (!description) return '';
    const maxLength = 150;
    if (description.length <= maxLength) return description;
    return description.substring(0, maxLength) + '...';
  }

  hasLinkedItems(entry: ClinicalHistoryEntry): boolean {
    return !!entry.hasFile;
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

  openTreatmentDetail(treatmentId: number) {
    if (!this.user || !this.user.patientId) {
      this.snackBar.open('Error: No se pudo obtener la información del paciente', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    const patientId = this.user.patientId;

    this.treatmentService.getTreatmentById(patientId, treatmentId).subscribe({
      next: (treatment) => {
        // Convertir TreatmentResponse a TreatmentDetailResponse para el modal
        const treatmentDetail: TreatmentDetailResponse = {
          ...treatment,
          sessions: [] // El servicio del paciente no devuelve sesiones
        };

        // Obtener el dentistId del tratamiento
        const dentistId = treatment.dentistId || 0;

        this.dialog.open(TreatmentDetailDialogComponent, {
          width: '900px',
          maxWidth: '90vw',
          maxHeight: '90vh',
          data: {
            treatment: treatmentDetail,
            dentistId: dentistId,
            patientId: patientId
          } as TreatmentDetailData
        });
      },
      error: (error) => {
        console.error('Error al cargar el detalle del tratamiento:', error);
        this.snackBar.open('Error al cargar el detalle del tratamiento', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }
}

