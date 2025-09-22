import { Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { LoginComponent } from './components/auth/pacient-login/pacient-login.component';
import { DentistLoginComponent } from './components/auth/dentist-login/dentist-login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { PatientDashboardComponent } from './components/dashboard/patient-dashboard/patient-dashboard.component';
import { DentistDashboardComponent } from './components/dashboard/dentist-dashboard/dentist-dashboard.component';

export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'pacient-login', component: LoginComponent },
  { path: 'dentist-login', component: DentistLoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'patient-dashboard', component: PatientDashboardComponent },
  { path: 'dentist-dashboard', component: DentistDashboardComponent },
  { path: '**', redirectTo: '' }
];
