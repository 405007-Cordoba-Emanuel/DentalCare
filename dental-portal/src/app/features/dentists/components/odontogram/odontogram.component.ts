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
import { FormsModule } from '@angular/forms';

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
  date: Date;
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
    MatFormFieldModule
  ],
  templateUrl: './odontogram.component.html',
  styleUrl: './odontogram.component.css'
})
export class OdontogramComponent implements OnInit {
  patientId: string | null = null;
  
  // Datos del paciente (mock por ahora)
  patientInfo = {
    firstName: 'Juan',
    lastName: 'Pérez',
    dni: '12345678',
    age: 35
  };

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
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.patientId = this.route.snapshot.paramMap.get('patientId');
    this.initializeTeeth();
    // TODO: Cargar datos del paciente y odontogramas guardados desde el backend
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
    // TODO: Implementar guardado en backend
    this.snackBar.open('Odontograma guardado correctamente', 'Cerrar', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }

  loadSavedOdontogram(saved: SavedOdontogram): void {
    this.dentitionType = saved.dentitionType;
    this.teeth = JSON.parse(JSON.stringify(saved.teeth)); // Deep copy
    this.selectedTooth = null;
    this.snackBar.open('Odontograma cargado correctamente', 'Cerrar', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }
}
