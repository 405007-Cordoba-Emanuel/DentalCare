import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TreatmentService } from '../../services/treatment.service';
import { DentistService } from '../../../../core/services/dentist.service';
import { TreatmentResponse, TreatmentDetailResponse } from '../../interfaces/treatment.interface';
import { PatientSummary } from '../../interfaces/patient.interface';
import { IconComponent } from '../../../../shared/icon/icon.component';

@Component({
  selector: 'app-dentist-treatments',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './dentist-treatments.component.html',
  styleUrls: ['./dentist-treatments.component.css']
})
export class DentistTreatmentsComponent implements OnInit {
  treatments: TreatmentResponse[] = [];
  patients: PatientSummary[] = [];
  selectedPatientId: number | null = null;
  selectedTreatment: TreatmentDetailResponse | null = null;
  loading = false;
  showTreatmentDetail = false;

  constructor(
    private treatmentService: TreatmentService,
    private dentistService: DentistService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    const dentistId = this.route.snapshot.params['id'];
    const patientId = this.route.snapshot.params['patientId'];
    
    if (dentistId) {
      this.loadDentistPatients(dentistId);
      
      if (patientId) {
        this.selectedPatientId = +patientId;
        this.loadTreatmentsForPatient(+patientId);
      }
    }
  }

  loadDentistPatients(dentistId: number) {
    this.loading = true;
    this.dentistService.getActivePatientsByDentistId(dentistId).subscribe({
      next: (response) => {
        this.patients = response.patients;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading patients:', error);
        this.loading = false;
      }
    });
  }

  loadTreatmentsForPatient(patientId: number) {
    const dentistId = this.route.snapshot.params['id'];
    this.loading = true;
    this.selectedPatientId = patientId;
    
    this.treatmentService.getTreatmentsByPatient(dentistId, patientId).subscribe({
      next: (treatments) => {
        this.treatments = treatments;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading treatments:', error);
        this.loading = false;
      }
    });
  }

  viewTreatmentDetail(treatmentId: number) {
    const dentistId = this.route.snapshot.params['id'];
    this.treatmentService.getTreatmentDetail(dentistId, treatmentId).subscribe({
      next: (detail) => {
        this.selectedTreatment = detail;
        this.showTreatmentDetail = true;
      },
      error: (error) => {
        console.error('Error loading treatment detail:', error);
      }
    });
  }

  closeTreatmentDetail() {
    this.showTreatmentDetail = false;
    this.selectedTreatment = null;
  }

  getStatusColor(status: string): string {
    switch (status.toLowerCase()) {
      case 'en_progreso':
      case 'in_progress':
        return 'dental-badge-primary';
      case 'completado':
      case 'completed':
        return 'dental-badge-accent';
      case 'pendiente':
      case 'pending':
        return 'dental-badge-warn';
      default:
        return 'dental-badge-primary';
    }
  }

  getProgressPercentage(treatment: TreatmentResponse): number {
    if (treatment.totalSessions === 0) return 0;
    return Math.round((treatment.completedSessions / treatment.totalSessions) * 100);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('es-ES');
  }
}
