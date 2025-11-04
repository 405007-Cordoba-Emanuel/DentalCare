import { Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { LoginComponent } from './components/auth/pacient-login/pacient-login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { PatientDashboardComponent } from './components/dashboard/patient-dashboard/patient-dashboard.component';
import { DentistDashboardComponent } from './components/dashboard/dentist-dashboard/dentist-dashboard.component';
import { LayoutComponent } from './layout/layout.component';

// Guards
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { roleRedirectGuard } from './core/guards/role-redirect.guard';
import { loginGuard } from './core/guards/login.guard';

// Dentist feature components
import { DentistPatientsComponent } from './features/dentists/components/dentist-patients/dentist-patients.component';
import { DentistTreatmentsComponent } from './features/dentists/components/dentist-treatments/dentist-treatments.component';
import { DentistProfileComponent } from './features/dentists/components/dentist-profile/dentist-profile.component';
import { DentistDetailComponent } from './features/dentists/components/dentist-detail/dentist-detail.component';

// Policy components
import { TermsConditionsComponent } from './features/policies/components/terms-conditions/terms-conditions.component';
import { PrivacyPolicyComponent } from './features/policies/components/privacy-policy/privacy-policy.component';
import { FaqComponent } from './features/policies/components/faq/faq.component';
import { AppointmentsComponent } from './features/appointments/appointments.component';
import { CreateAppointmentComponent } from './features/appointments/create-appointment/create-appointment.component';
import { PatientProfileComponent } from './features/patient-profile/patient-profile.component';
import { PrescriptionListComponent } from './features/prescriptions/prescription-list.component';
import { ClinicalHistoryListComponent } from './features/clinical-history/clinical-history-list.component';
import { PatientAppointmentsComponent } from './features/patient-appointments/patient-appointments.component';


export const routes: Routes = [
  // Páginas públicas sin layout
  { path: '', component: LandingPageComponent },
  
  // Login único para ambos roles (paciente y dentista)
  { 
    path: 'login', 
    component: LoginComponent,
    canActivate: [loginGuard]
  },
  // Rutas de registro separadas por rol
  { 
    path: 'patient-register', 
    component: RegisterComponent,
    canActivate: [loginGuard]
  },
  { 
    path: 'dentist-register', 
    component: RegisterComponent,
    canActivate: [loginGuard]
  },
  { 
    path: 'register', 
    redirectTo: 'patient-register', 
    pathMatch: 'full'
  }, // Por defecto paciente
  
  // Policy pages (public access)
  { path: 'terms-conditions', component: TermsConditionsComponent },
  { path: 'privacy-policy', component: PrivacyPolicyComponent },
  { path: 'faq', component: FaqComponent },
  
  // Ruta de redirección automática basada en rol
  { 
    path: 'dashboard', 
    canActivate: [roleRedirectGuard],
    redirectTo: '', // Temporal redirect, el guard manejará la redirección real
    pathMatch: 'full'
  },
  
  // Rutas con layout (páginas protegidas y features)
  {
    path: '',
    component: LayoutComponent,
    children: [
      // Dashboard de paciente con rutas hijas
      {
        path: 'patient',
        component: PatientDashboardComponent,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['PATIENT'] }
      },
      {
        path: 'patient/profile',
        component: PatientProfileComponent,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['PATIENT'] }
      },
      {
        path: 'patient/prescriptions',
        component: PrescriptionListComponent,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['PATIENT'] }
      },
      {
        path: 'patient/clinical-history',
        component: ClinicalHistoryListComponent,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['PATIENT'] }
      },
      {
        path: 'patient/appointments',
        component: PatientAppointmentsComponent,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['PATIENT'] }
      },
      
      // Rutas del dentista (todas empiezan con dentist/)
      {
        path: 'dentist',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['DENTIST'] },
        children: [
          // Dashboard principal (ruta vacía)
          { path: '', component: DentistDashboardComponent },
          { path: 'profile', component: DentistProfileComponent },
          { path: 'patients', component: DentistPatientsComponent },
          { path: 'treatments', component: DentistTreatmentsComponent },
          { path: 'appointments', component: AppointmentsComponent },
          { path: 'appointments/create', component: CreateAppointmentComponent },
          // Rutas de dentista por ID
          { path: ':id', component: DentistDetailComponent },
          { path: ':id/profile', component: DentistProfileComponent },
          { path: ':id/patients', component: DentistPatientsComponent },
          { path: ':id/treatments', component: DentistTreatmentsComponent },
          { path: ':id/patients/:patientId/treatments', component: DentistTreatmentsComponent },
        ]
      },
    ]
  },
  
  { path: '**', redirectTo: '' }
];
