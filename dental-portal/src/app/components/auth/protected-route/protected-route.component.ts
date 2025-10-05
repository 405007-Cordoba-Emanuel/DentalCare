import { Component, inject } from '@angular/core';
import { AuthService } from '../../../core/services/auth/auth.service';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-protected-route',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './protected-route.component.html'
})
export class ProtectedRouteComponent {

  authService = inject(AuthService);
}
