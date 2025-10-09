import { Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { LoginComponent } from './components/auth/pacient-login/pacient-login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { PatientDashboardComponent } from './components/dashboard/patient-dashboard/patient-dashboard.component';
import { DentistDashboardComponent } from './components/dashboard/dentist-dashboard/dentist-dashboard.component';
import { LayoutComponent } from './layout/layout.component';

// Dentist feature components
import { DentistPatientsComponent } from './features/dentists/components/dentist-patients/dentist-patients.component';
import { DentistTreatmentsComponent } from './features/dentists/components/dentist-treatments/dentist-treatments.component';
import { DentistProfileComponent } from './features/dentists/components/dentist-profile/dentist-profile.component';
import { DentistDetailComponent } from './features/dentists/components/dentist-detail/dentist-detail.component';

// Policy components
import { TermsConditionsComponent } from './features/policies/components/terms-conditions/terms-conditions.component';
import { PrivacyPolicyComponent } from './features/policies/components/privacy-policy/privacy-policy.component';
import { FaqComponent } from './features/policies/components/faq/faq.component';

export const routes: Routes = [
  // Páginas públicas sin layout
  { path: '', component: LandingPageComponent },
  
  // Login único para ambos roles (paciente y dentista)
  { path: 'login', component: LoginComponent },
  { path: 'pacient-login', redirectTo: 'login', pathMatch: 'full' }, // Mantener compatibilidad
  { path: 'dentist-login', redirectTo: 'login', pathMatch: 'full' }, // Mantener compatibilidad
  
  // Rutas de registro separadas por rol
  { path: 'patient-register', component: RegisterComponent },
  { path: 'dentist-register', component: RegisterComponent },
  { path: 'register', redirectTo: 'patient-register', pathMatch: 'full' }, // Por defecto paciente
  
  // Policy pages (public access)
  { path: 'terms-conditions', component: TermsConditionsComponent },
  { path: 'privacy-policy', component: PrivacyPolicyComponent },
  { path: 'faq', component: FaqComponent },
  
  // Rutas con layout (páginas protegidas y features)
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: 'patient-dashboard', component: PatientDashboardComponent },
      { path: 'dentist-dashboard', component: DentistDashboardComponent },
      
      // Dentist feature routes anidadas
      { path: 'dentist/:id', component: DentistDetailComponent },
      { path: 'dentist/:id/profile', component: DentistProfileComponent },
      { path: 'dentist/:id/patients', component: DentistPatientsComponent },
      { path: 'dentist/:id/treatments', component: DentistTreatmentsComponent },
      { path: 'dentist/:id/patients/:patientId/treatments', component: DentistTreatmentsComponent },
    ]
  },
  
  { path: '**', redirectTo: '' }
];
