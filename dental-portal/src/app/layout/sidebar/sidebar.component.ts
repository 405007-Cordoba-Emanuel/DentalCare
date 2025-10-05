import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AsyncPipe } from '@angular/common';
import { AuthService } from '../../core/services/auth/auth.service';
import { SidebarService } from '../../shared/services/sidebar.service';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [MatIconModule, AsyncPipe, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private sidebarService = inject(SidebarService);

  isSidebarOpen$ = this.sidebarService.isSidebarOpen$;
  currentUser$ = this.authService.currentUser$;

  toggleSidebar() {
    this.sidebarService.toggleSidebar();
  }

  logout() {
    this.authService.logout();
  }

}
