import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DentistService } from '../../../../core/services/dentist.service';
import { PatientService } from '../../../../core/services/patient.service';
import { PatientResponse } from '../../interfaces/patient.interface';
import { IconComponent } from '../../../../shared/icon/icon.component';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';
import { GenericModalService } from '../../../../shared/generic-modal/generic-modal.service';
import { PagedResponse } from '../../interfaces/paged-response.interface';

@Component({
  selector: 'app-dentist-patients',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    IconComponent,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dentist-patients.component.html',
  styleUrls: ['./dentist-patients.component.css']
})
export class DentistPatientsComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  // Tabla y datos
  dataSource = new MatTableDataSource<PatientResponse>([]);
  displayedColumns: string[] = ['firstName', 'lastName', 'dni', 'email', 'phone', 'active', 'actions'];
  
  // Pacientes disponibles
  availablePatients: PatientResponse[] = [];
  loading = false;
  sortBy: 'firstName' | 'lastName' | 'dni' | 'email' = 'lastName';
  sortDirection: 'asc' | 'desc' = 'asc';
  
  // Paginación
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  constructor(
    private dentistService: DentistService,
    private patientService: PatientService,
    private route: ActivatedRoute,
    private router: Router,
    private localStorageService: LocalStorageService,
    private modalService: GenericModalService
  ) {}

  ngOnInit() {
    this.loadAvailablePatients();
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAvailablePatients();
  }

  loadAvailablePatients() {
    this.loading = true;
    this.dentistService.getAvailablePatientsPaged(
      this.currentPage, 
      this.pageSize, 
      this.sortBy, 
      this.sortDirection
    ).subscribe({
      next: (response: PagedResponse<PatientResponse>) => {
        this.availablePatients = response.content;
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        
        // Actualizar el paginator
        if (this.paginator) {
          this.paginator.length = this.totalElements;
        }
        
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading available patients:', error);
        this.loading = false;
        this.modalService.error(
          'Error al Cargar Pacientes',
          'No se pudieron cargar los pacientes disponibles.',
          ['Verifique su conexión', 'Intente nuevamente']
        );
      }
    });
  }

  changeSortField(field: 'firstName' | 'lastName' | 'dni' | 'email') {
    if (this.sortBy === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0; // Reset to first page when sorting
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadAvailablePatients();
  }

  assignPatient(patient: PatientResponse) {
    const dentistId = this.localStorageService.getDentistId();

    // Mostrar modal de confirmación
    this.modalService.confirm(
      'Asignar Paciente',
      `¿Está seguro que desea asignar a ${patient.firstName} ${patient.lastName} como su paciente?`,
      [
        `DNI: ${patient.dni}`,
        `Email: ${patient.email}`,
        `Teléfono: ${patient.phone}`
      ],
      'Asignar',
      'Cancelar'
    ).subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.loading = true;
      
      this.patientService.assignDentistToPatient(patient.id, dentistId).subscribe({
        next: (assignedPatient) => {
          // Recargar la lista de pacientes disponibles
          this.loadAvailablePatients();
          
          // Mostrar modal de éxito
          this.modalService.success(
            'Paciente Asignado',
            `${assignedPatient.firstName} ${assignedPatient.lastName} ha sido asignado exitosamente como su paciente.`,
            [
              'El paciente ya no aparecerá en la lista de disponibles',
              'Puede comenzar a gestionar sus tratamientos y citas'
            ]
          );
        },
        error: (error) => {
          console.error('Error assigning patient:', error);
          this.loading = false;
          
          const errorMessage = error.error?.message || 'No se pudo completar la operación.';
          
          // Mostrar modal de error
          this.modalService.error(
            'Error al Asignar Paciente',
            errorMessage,
            [
              'Por favor, verifique su conexión',
              'Intente nuevamente en unos momentos',
              'Si el problema persiste, contacte al administrador'
            ]
          );
        }
      });
    });
  }

}
