import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { AdminService } from './services/admin.service';
import { UserDetailResponse } from './interfaces/user-detail.interface';
import { CreateDentistRequest } from './interfaces/create-dentist.interface';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatTableModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTabsModule
  ],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  users: UserDetailResponse[] = [];
  filteredUsers: UserDetailResponse[] = [];
  displayedColumns: string[] = ['id', 'name', 'email', 'role', 'isActive', 'lastLogin'];
  isLoading = false;
  searchTerm = '';
  selectedTabIndex = 0;

  // Formulario para crear dentista
  createDentistForm: FormGroup;
  availableUsers: UserDetailResponse[] = [];
  isCreatingDentist = false;

  constructor() {
    this.createDentistForm = this.fb.group({
      userId: ['', [Validators.required]],
      licenseNumber: ['', [Validators.required, Validators.maxLength(20), Validators.pattern(/^[A-Za-z0-9\-]+$/)]],
      specialty: ['', [Validators.required, Validators.maxLength(150)]]
    });
  }

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.filteredUsers = users;
        // Filtrar usuarios que no sean dentistas para el formulario
        this.availableUsers = users.filter(u => u.role !== 'DENTIST' && u.isActive);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar usuarios:', error);
        this.snackBar.open('Error al cargar usuarios', 'Cerrar', {
          duration: 3000
        });
        this.isLoading = false;
      }
    });
  }

  onSearchChange() {
    if (!this.searchTerm.trim()) {
      this.filteredUsers = this.users;
      return;
    }

    const search = this.searchTerm.toLowerCase().trim();
    this.filteredUsers = this.users.filter(user =>
      user.firstName?.toLowerCase().includes(search) ||
      user.lastName?.toLowerCase().includes(search) ||
      user.email?.toLowerCase().includes(search) ||
      user.role?.toLowerCase().includes(search)
    );
  }

  getFullName(user: UserDetailResponse): string {
    return `${user.firstName} ${user.lastName}`;
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return 'Nunca';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getRoleLabel(role: string): string {
    const roleMap: { [key: string]: string } = {
      'ADMIN': 'Administrador',
      'DENTIST': 'Dentista',
      'PATIENT': 'Paciente'
    };
    return roleMap[role] || role;
  }

  onCreateDentistSubmit() {
    if (this.createDentistForm.invalid) {
      this.snackBar.open('Por favor, completa todos los campos correctamente', 'Cerrar', {
        duration: 3000
      });
      return;
    }

    this.isCreatingDentist = true;
    const formValue = this.createDentistForm.value;
    const request: CreateDentistRequest = {
      userId: Number(formValue.userId),
      licenseNumber: formValue.licenseNumber,
      specialty: formValue.specialty
    };

    this.adminService.createDentistFromUser(request).subscribe({
      next: (response) => {
        this.snackBar.open('Dentista creado exitosamente', 'Cerrar', {
          duration: 3000
        });
        this.createDentistForm.reset();
        this.loadUsers(); // Recargar usuarios para actualizar roles
        this.isCreatingDentist = false;
      },
      error: (error) => {
        console.error('Error al crear dentista:', error);
        const errorMessage = error.error?.message || 'Error al crear dentista';
        this.snackBar.open(errorMessage, 'Cerrar', {
          duration: 5000
        });
        this.isCreatingDentist = false;
      }
    });
  }

  getUserDisplayName(user: UserDetailResponse): string {
    return `${user.firstName} ${user.lastName} (${user.email})`;
  }
}
