import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { SidebarComponent } from './sidebar/sidebar.component';
import { AuthService } from '../core/services/auth/auth.service';
import { SidebarService } from '../shared/services/sidebar.service';
import { FooterComponent } from "./footer/footer.component";
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    SidebarComponent,
    RouterLink,
    AsyncPipe,
    FooterComponent
],
  templateUrl: './layout.component.html',
})
export class LayoutComponent {
  private authService = inject(AuthService);
  private sidebarService = inject(SidebarService);
  
  // Usar observables para reactividad
  currentUser$ = this.authService.currentUser$;
  isSidebarOpen$ = this.sidebarService.isSidebarOpen$;
  
  toggleSidebar() {
    this.sidebarService.toggleSidebar();
  }
}
