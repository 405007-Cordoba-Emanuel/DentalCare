import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-protected-route',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './protected-route.component.html',
  styleUrl: './protected-route.component.css'
})
export class ProtectedRouteComponent {

  constructor(public authService: AuthService) {}
}
