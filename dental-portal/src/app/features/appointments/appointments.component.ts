import { Component, inject } from '@angular/core';
import { DentistService } from '../dentists/services/dentist.service';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PatientSummary } from '../dentists/interfaces/patient.interface';
import { AppointmentRequest } from '../dentists/interfaces/appointment.interface';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-appointments',
  imports: [
    ReactiveFormsModule,
    CommonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './appointments.component.html',
})
export class AppointmentsComponent {

}
