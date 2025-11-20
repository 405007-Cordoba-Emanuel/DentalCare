import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { OdontogramService, OdontogramResponseDto } from '../../services/odontogram.service';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';
import { ConfirmDeleteOdontogramDialogComponent, ConfirmDeleteOdontogramData } from './confirm-delete-odontogram-dialog.component';

type ToothStatus =
  | 'healthy'
  | 'missing'
  | 'cavity-repair'
  | 'extraction-pending'
  | 'crown-pending'
  | 'crown-done'
  | 'previous-work';

type DentitionType = 'adult' | 'child';

interface ToothData {
  number: number;
  statuses: ToothStatus[];
}

interface ToothStatusConfig {
  label: string;
  color: string;
  textColor: string;
}

interface SavedOdontogram {
  id: number;
  createdDatetime: string;
  dentitionType: DentitionType;
  teeth: Record<number, ToothData>;
}

@Component({
  selector: 'app-odontogram',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatTooltipModule,
    MatDialogModule
  ],
  templateUrl: './odontogram.component.html',
  styleUrl: './odontogram.component.css'
})
export class OdontogramComponent implements OnInit {
  patientId: number | null = null;
  dentistId: number | null = null;
  
  // Datos del paciente
  patientInfo = {
    firstName: 'Cargando',
    lastName: '...',
    dni: '...',
    age: 0
  };

  isLoading = false;

  // Configuración de estados de dientes
  readonly TOOTH_STATUS_CONFIG: Record<ToothStatus, ToothStatusConfig> = {
    'healthy': {
      label: 'Sano',
      color: 'bg-green-500',
      textColor: 'text-white'
    },
    'missing': {
      label: 'Ausente',
      color: 'bg-red-500',
      textColor: 'text-white'
    },
    'cavity-repair': {
      label: 'Caries por arreglar',
      color: 'bg-blue-500',
      textColor: 'text-white'
    },
    'extraction-pending': {
      label: 'Extracciones por hacer',
      color: 'bg-cyan-400',
      textColor: 'text-black'
    },
    'crown-pending': {
      label: 'Corona por hacer',
      color: 'bg-violet-500',
      textColor: 'text-white'
    },
    'crown-done': {
      label: 'Corona hecha',
      color: 'bg-yellow-500',
      textColor: 'text-black'
    },
    'previous-work': {
      label: 'Arreglos hechos anteriormente',
      color: 'bg-orange-500',
      textColor: 'text-white'
    }
  };

  // Numeración dental estándar
  readonly ADULT_UPPER_TEETH = [18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28];
  readonly ADULT_LOWER_TEETH = [48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38];
  readonly CHILD_UPPER_TEETH = [55, 54, 53, 52, 51, 61, 62, 63, 64, 65];
  readonly CHILD_LOWER_TEETH = [85, 84, 83, 82, 81, 71, 72, 73, 74, 75];

  dentitionType: DentitionType = 'adult';
  teeth: Record<number, ToothData> = {};
  selectedTooth: number | null = null;
  
  // Odontogramas guardados (mock por ahora)
  savedOdontograms: SavedOdontogram[] = [];
  
  // Panel de historial expandido o colapsado
  historyPanelExpanded = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private odontogramService: OdontogramService,
    private localStorageService: LocalStorageService
  ) {}

  ngOnInit(): void {
    // Obtener IDs de los parámetros y localStorage
    const patientIdParam = this.route.snapshot.paramMap.get('patientId');
    this.patientId = patientIdParam ? parseInt(patientIdParam) : null;
    
    // Obtener dentistId desde localStorage
    this.dentistId = this.localStorageService.getDentistId();

    if (!this.patientId) {
      this.snackBar.open('ID de paciente no encontrado', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      this.router.navigate(['/dentist']);
      return;
    }

    if (!this.dentistId) {
      this.snackBar.open('ID de dentista no encontrado', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      this.router.navigate(['/dentist']);
      return;
    }

    this.initializeTeeth();
    this.loadPatientInfo();
    this.loadSavedOdontograms();
  }

  loadPatientInfo(): void {
    // TODO: Cargar info del paciente desde el backend
    // Por ahora usar datos mock basados en patientId
    this.patientInfo = {
      firstName: 'María',
      lastName: 'González',
      dni: '12345678',
      age: 30
    };
  }

  loadSavedOdontograms(): void {
    if (!this.dentistId || !this.patientId) return;

    this.isLoading = true;
    this.odontogramService.getOdontogramsByPatient(this.dentistId, this.patientId).subscribe({
      next: (odontograms: OdontogramResponseDto[]) => {
        // El backend ya devuelve ordenado por fecha DESC (más reciente primero)
        this.savedOdontograms = odontograms.map(dto => this.mapDtoToSavedOdontogram(dto));
        console.log('Odontogramas cargados (más reciente primero):', this.savedOdontograms);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar odontogramas:', error);
        this.isLoading = false;
        this.snackBar.open('Error al cargar odontogramas anteriores', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }

  private mapDtoToSavedOdontogram(dto: OdontogramResponseDto): SavedOdontogram {
    const teethData = JSON.parse(dto.teethData);
    return {
      id: dto.id,
      createdDatetime: dto.createdDatetime,
      dentitionType: dto.dentitionType,
      teeth: teethData
    };
  }

  initializeTeeth(): void {
    const allTeeth = this.dentitionType === 'adult'
      ? [...this.ADULT_UPPER_TEETH, ...this.ADULT_LOWER_TEETH]
      : [...this.CHILD_UPPER_TEETH, ...this.CHILD_LOWER_TEETH];

    this.teeth = {};
    allTeeth.forEach((number) => {
      this.teeth[number] = { number, statuses: ['healthy'] };
    });
  }

  get upperTeeth(): number[] {
    return this.dentitionType === 'adult' ? this.ADULT_UPPER_TEETH : this.CHILD_UPPER_TEETH;
  }

  get lowerTeeth(): number[] {
    return this.dentitionType === 'adult' ? this.ADULT_LOWER_TEETH : this.CHILD_LOWER_TEETH;
  }

  get statusKeys(): ToothStatus[] {
    return Object.keys(this.TOOTH_STATUS_CONFIG) as ToothStatus[];
  }

  changeDentitionType(type: DentitionType): void {
    this.dentitionType = type;
    this.selectedTooth = null;
    this.initializeTeeth();
  }

  selectTooth(toothNumber: number): void {
    this.selectedTooth = toothNumber;
  }

  toggleToothStatus(toothNumber: number, status: ToothStatus): void {
    const currentStatuses = this.teeth[toothNumber].statuses;
    let newStatuses: ToothStatus[];

    if (status === 'healthy') {
      // Si selecciona "sano", solo mantener ese estado
      newStatuses = ['healthy'];
    } else {
      // Remover "sano" si existe y toggle el estado seleccionado
      const filteredStatuses = currentStatuses.filter((s) => s !== 'healthy');

      if (filteredStatuses.includes(status)) {
        newStatuses = filteredStatuses.filter((s) => s !== status);
        // Si no queda ningún estado, volver a "sano"
        if (newStatuses.length === 0) {
          newStatuses = ['healthy'];
        }
      } else {
        newStatuses = [...filteredStatuses, status];
      }
    }

    this.teeth[toothNumber] = { ...this.teeth[toothNumber], statuses: newStatuses };
  }

  isStatusChecked(toothNumber: number, status: ToothStatus): boolean {
    return this.teeth[toothNumber]?.statuses.includes(status) || false;
  }

  getToothConfig(toothNumber: number): ToothStatusConfig {
    const primaryStatus = this.teeth[toothNumber].statuses[0];
    return this.TOOTH_STATUS_CONFIG[primaryStatus];
  }

  hasMultipleStatuses(toothNumber: number): boolean {
    return this.teeth[toothNumber].statuses.length > 1;
  }

  getStatusCount(status: ToothStatus): number {
    return Object.values(this.teeth).filter((tooth) =>
      tooth.statuses.includes(status)
    ).length;
  }

  getMultipleStatusCount(): number {
    return Object.values(this.teeth).filter((tooth) => tooth.statuses.length > 1).length;
  }

  clearAll(): void {
    this.initializeTeeth();
    this.selectedTooth = null;
    this.snackBar.open('Odontograma limpiado correctamente', 'Cerrar', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }

  saveOdontogram(): void {
    if (!this.dentistId || !this.patientId) {
      this.snackBar.open('Error: IDs no encontrados', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    this.isLoading = true;

    const requestDto = {
      patientId: this.patientId,
      dentitionType: this.dentitionType,
      teethData: JSON.stringify(this.teeth)
    };

    this.odontogramService.createOdontogram(this.dentistId, this.patientId, requestDto).subscribe({
      next: (response: OdontogramResponseDto) => {
        this.isLoading = false;
        this.snackBar.open('Odontograma guardado correctamente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });
        
        // Recargar la lista de odontogramas guardados
        this.loadSavedOdontograms();
        
        // Expandir el panel de historial para mostrar el nuevo odontograma
        this.historyPanelExpanded = true;
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al guardar odontograma:', error);
        this.snackBar.open('Error al guardar el odontograma', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  loadSavedOdontogram(saved: SavedOdontogram): void {
    this.dentitionType = saved.dentitionType;
    this.teeth = JSON.parse(JSON.stringify(saved.teeth)); // Deep copy
    this.selectedTooth = null;
    
    this.snackBar.open(
      `Odontograma del ${this.getLocalDateTime(saved.createdDatetime)} cargado`, 
      'Cerrar', 
      {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      }
    );
  }

  /**
   * Convierte una fecha del backend a formato local (solo fecha)
   */
  getLocalDateTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  /**
   * Confirma y elimina un odontograma
   */
  confirmDeleteOdontogram(saved: SavedOdontogram): void {
    const fecha = this.getLocalDateTime(saved.createdDatetime);
    const isRecent = this.savedOdontograms.length > 0 && this.savedOdontograms[0].id === saved.id;
    const patientFullName = `${this.patientInfo.firstName} ${this.patientInfo.lastName}`;

    const dialogData: ConfirmDeleteOdontogramData = {
      patientName: patientFullName,
      odontogramDate: fecha,
      isRecent: isRecent
    };

    const dialogRef = this.dialog.open(ConfirmDeleteOdontogramDialogComponent, {
      width: '500px',
      data: dialogData,
      disableClose: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteOdontogram(saved.id);
      }
    });
  }

  /**
   * Elimina un odontograma del backend
   */
  deleteOdontogram(odontogramId: number): void {
    if (!this.dentistId) {
      this.snackBar.open('Error: ID de dentista no encontrado', 'Cerrar', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
      return;
    }

    this.isLoading = true;

    this.odontogramService.deleteOdontogram(this.dentistId, odontogramId).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('Odontograma eliminado correctamente', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });
        
        // Recargar la lista de odontogramas
        this.loadSavedOdontograms();
        
        // Si el odontograma actual era el eliminado, limpiar todo
        this.clearAll();
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al eliminar odontograma:', error);
        this.snackBar.open('Error al eliminar el odontograma', 'Cerrar', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
