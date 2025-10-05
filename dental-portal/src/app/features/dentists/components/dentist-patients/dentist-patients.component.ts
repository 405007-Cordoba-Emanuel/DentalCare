import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DentistService } from '../../services/dentist.service';
import { PatientSummary } from '../../interfaces/patient.interface';
import { DentistInfo } from '../../interfaces/dentist.interface';
import { IconComponent } from '../../../../shared/icon/icon.component';

@Component({
  selector: 'app-dentist-patients',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent],
  templateUrl: './dentist-patients.component.html',
  styleUrls: ['./dentist-patients.component.css']
})
export class DentistPatientsComponent implements OnInit {
  patients: PatientSummary[] = [];
  dentistInfo: DentistInfo | null = null;
  loading = false;
  showActiveOnly = true;
  searchTerm = '';

  constructor(
    private dentistService: DentistService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    const dentistId = this.route.snapshot.params['id'];
    if (dentistId) {
      this.loadPatients(dentistId);
    }
  }

  loadPatients(dentistId: number) {
    this.loading = true;
    const request$ = this.showActiveOnly 
      ? this.dentistService.getActivePatientsByDentistId(dentistId)
      : this.dentistService.getPatientsByDentistId(dentistId);

    request$.subscribe({
      next: (response) => {
        this.patients = response.patients;
        this.dentistInfo = {
          id: response.dentistId,
          name: response.dentistName,
          licenseNumber: response.licenseNumber,
          specialty: response.specialty
        };
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading patients:', error);
        this.loading = false;
      }
    });
  }

  toggleActiveFilter() {
    this.showActiveOnly = !this.showActiveOnly;
    const dentistId = this.route.snapshot.params['id'];
    if (dentistId) {
      this.loadPatients(dentistId);
    }
  }

  onSearchChange() {
    // La búsqueda se maneja en el template con el pipe
  }

  viewPatientTreatments(patientId: number) {
    const dentistId = this.route.snapshot.params['id'];
    this.router.navigate(['/dentist', dentistId, 'patients', patientId, 'treatments']);
  }

  get filteredPatients(): PatientSummary[] {
    if (!this.searchTerm) {
      return this.patients;
    }
    return this.patients.filter(patient => 
      `${patient.firstName} ${patient.lastName}`.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      patient.dni.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      patient.email.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }
}
