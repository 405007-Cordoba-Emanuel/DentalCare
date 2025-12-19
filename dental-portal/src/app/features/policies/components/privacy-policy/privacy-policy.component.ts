import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LocalStorageService } from '../../../../core/services/auth/local-storage.service';
import { User } from '../../../../interfaces/user/user.interface';

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './privacy-policy.component.html',
  styleUrls: ['./privacy-policy.component.css']
})
export class PrivacyPolicyComponent {
  private router = inject(Router);
  private localStorage = inject(LocalStorageService);

  lastUpdated = 'Noviembre 18, 2025';

  getDashboardRoute(): string {
    const userDataString = this.localStorage.getUserData();
    if (!userDataString) {
      return '/dashboard'; // Fallback a la ruta genérica
    }

    try {
      const userData = JSON.parse(userDataString) as User;
      const role = userData.role;

      if (role === 'DENTIST') {
        return '/dentist';
      } else if (role === 'PATIENT') {
        return '/patient';
      } else {
        return '/dashboard'; // Fallback
      }
    } catch (error) {
      console.error('Error parsing user data:', error);
      return '/dashboard'; // Fallback
    }
  }

  navigateToDashboard(): void {
    this.router.navigate([this.getDashboardRoute()]);
  }
}
