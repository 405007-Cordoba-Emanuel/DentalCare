import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { AsyncPipe, NgIf } from '@angular/common';
import { AuthService } from '../../core/services/auth/auth.service';
import { SidebarService } from '../../shared/services/sidebar.service';
import { ChatService } from '../../core/services/chat/chat.service';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Router } from '@angular/router';
import { Subject, interval, takeUntil, switchMap } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [MatIconModule, MatBadgeModule, AsyncPipe, NgIf, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private sidebarService = inject(SidebarService);
  private chatService = inject(ChatService);
  private router = inject(Router);
  private destroy$ = new Subject<void>();
  
  isSidebarOpen$ = this.sidebarService.isSidebarOpen$;
  currentUser$ = this.authService.currentUser$;
  unreadCount = 0;

  ngOnInit(): void {
    // Actualizar contador cada 30 segundos
    interval(30000).pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.chatService.getUnreadCount())
    ).subscribe({
      next: (response) => {
        this.unreadCount = response.unreadCount;
      },
      error: (err) => console.error('Error loading unread count:', err)
    });

    // Cargar inicialmente
    this.chatService.getUnreadCount().subscribe({
      next: (response) => {
        this.unreadCount = response.unreadCount;
      },
      error: (err) => console.error('Error loading unread count:', err)
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleSidebar() {
    this.sidebarService.toggleSidebar();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

}
