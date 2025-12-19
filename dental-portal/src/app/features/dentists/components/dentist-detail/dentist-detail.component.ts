import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DentistService } from '../../../../core/services/dentist.service';
import { DentistResponse } from '../../interfaces/dentist.interface';
import { IconComponent } from '../../../../shared/icon/icon.component';

@Component({
  selector: 'app-dentist-detail',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './dentist-detail.component.html'
})
export class DentistDetailComponent implements OnInit {
  dentist: DentistResponse | null = null;
  loading = false;
  router = inject(Router);
  dentistService = inject(DentistService);
  route = inject(ActivatedRoute);

  ngOnInit() {
    const dentistId = this.route.snapshot.params['id'];
    if (dentistId) {
      this.loadDentist(dentistId);
    }
  }

  loadDentist(id: number) {
    this.loading = true;
    this.dentistService.getDentistById(id).subscribe({
      next: (dentist) => {
        this.dentist = dentist;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading dentist:', error);
        this.loading = false;
      }
    });
  }

  editProfile() {
    if (this.dentist) {
      this.router.navigate(['/dentist', this.dentist.id, 'profile']);
    }
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  getStatusColor(active: boolean): string {
    return active ? 'dental-badge-accent' : 'dental-badge-warn';
  }

  getStatusText(active: boolean): string {
    return active ? 'Activo' : 'Inactivo';
  }
}
