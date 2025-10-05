import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DentistService } from '../../services/dentist.service';
import { DentistResponse, DentistUpdateRequest } from '../../interfaces/dentist.interface';

@Component({
  selector: 'app-dentist-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dentist-profile.component.html',
  styleUrls: ['./dentist-profile.component.css']
})
export class DentistProfileComponent implements OnInit {
  dentistForm: FormGroup;
  dentist: DentistResponse | null = null;
  loading = false;
  saving = false;
  hasChanges = false;

  constructor(
    private fb: FormBuilder,
    private dentistService: DentistService,
    public route: ActivatedRoute,
    private router: Router
  ) {
    this.dentistForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      licenseNumber: ['', [Validators.required, Validators.maxLength(20), Validators.pattern(/^[A-Za-z0-9\-]+$/)]],
      specialty: ['', [Validators.required, Validators.maxLength(150)]],
      phone: ['', [Validators.pattern(/^[+]?[0-9\s\-()]{7,20}$/)]],
      email: ['', [Validators.email, Validators.maxLength(150)]],
      address: ['', [Validators.maxLength(255)]],
      active: [true]
    });

    // Detectar cambios en el formulario
    this.dentistForm.valueChanges.subscribe(() => {
      this.hasChanges = true;
    });
  }

  ngOnInit() {
    const dentistId = this.route.snapshot.params['id'];
    if (dentistId) {
      this.loadDentist(+dentistId);
    }
  }

  loadDentist(id: number) {
    this.loading = true;
    this.dentistService.getDentistById(id).subscribe({
      next: (dentist) => {
        this.dentist = dentist;
        this.dentistForm.patchValue(dentist);
        this.hasChanges = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading dentist:', error);
        this.loading = false;
      }
    });
  }

  onSubmit() {
    if (this.dentistForm.valid && this.dentist) {
      this.saving = true;
      const updateData: DentistUpdateRequest = this.dentistForm.value;
      
      this.dentistService.updateDentist(this.dentist.id, updateData).subscribe({
        next: (updatedDentist) => {
          this.dentist = updatedDentist;
          this.saving = false;
          this.hasChanges = false;
          // Mostrar mensaje de éxito
          alert('Datos actualizados correctamente');
        },
        error: (error) => {
          console.error('Error updating dentist:', error);
          this.saving = false;
          // Mostrar mensaje de error
          alert('Error al actualizar los datos: ' + (error.error?.message || error.message));
        }
      });
    }
  }

  onCancel() {
    if (this.dentist) {
      this.loadDentist(this.dentist.id);
    }
  }

  // Separar campos personales y profesionales
  get personalFields() {
    return ['firstName', 'lastName', 'phone', 'email', 'address'];
  }

  get professionalFields() {
    return ['licenseNumber', 'specialty', 'active'];
  }

  getFieldError(fieldName: string): string {
    const field = this.dentistForm.get(fieldName);
    if (field?.invalid && field?.touched) {
      if (field.errors?.['required']) {
        return 'Este campo es requerido';
      }
      if (field.errors?.['email']) {
        return 'Formato de email inválido';
      }
      if (field.errors?.['pattern']) {
        return 'Formato inválido';
      }
      if (field.errors?.['maxlength']) {
        return `Máximo ${field.errors['maxlength'].requiredLength} caracteres`;
      }
    }
    return '';
  }
}
