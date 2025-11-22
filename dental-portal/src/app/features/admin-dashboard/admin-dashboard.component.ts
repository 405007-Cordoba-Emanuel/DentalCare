import { Component, OnInit, ViewChild, inject, computed } from '@angular/core';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminService } from './services/admin.service';
import { UserDetailResponse } from './interfaces/user-detail.interface';
import { CreateDentistRequest } from './interfaces/create-dentist.interface';
import { PagedResponse } from './interfaces/paginated-response.interface';
import { FormField } from '../../shared/generic-form/generic-form.component';
import { ConfirmUserActionDialogComponent, ConfirmUserActionData } from './components/confirm-user-action-dialog.component';

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
    MatPaginatorModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatDialogModule,
    MatTooltipModule,
    GenericFormComponent
  ],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild('createDentistForm') createDentistFormRef!: GenericFormComponent;

  private adminService = inject(AdminService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  private dialog = inject(MatDialog);

  users: UserDetailResponse[] = [];
  filteredUsers: UserDetailResponse[] = [];
  dataSource = new MatTableDataSource<UserDetailResponse>([]);
  displayedColumns: string[] = ['name', 'email', 'role', 'isActive', 'actions'];
  isLoading = false;
  searchTerm = '';
  selectedTabIndex = 0;
  
  // Propiedades de paginación
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Formulario para crear dentista
  isCreatingDentist = false;
  createDentistForm: FormGroup;
  createDentistError = '';
  preservedFormData: any = {};

  // Configuración del formulario genérico
  createDentistFields = computed<FormField[]>(() => [
    {
      name: 'email',
      label: 'Email',
      type: 'text',
      placeholder: 'ejemplo@email.com',
      validators: [
        Validators.required,
        Validators.email
      ],
      fullWidth: true
    },
    {
      name: 'firstName',
      label: 'Nombre',
      type: 'text',
      placeholder: 'Nombre del dentista',
      validators: [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50)
      ],
      fullWidth: true
    },
    {
      name: 'lastName',
      label: 'Apellido',
      type: 'text',
      placeholder: 'Apellido del dentista',
      validators: [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50)
      ],
      fullWidth: true
    },
    {
      name: 'licenseNumber',
      label: 'Número de Matrícula',
      type: 'text',
      placeholder: 'Ej: DENT-12345',
      validators: [
        Validators.required,
        Validators.maxLength(20),
        Validators.pattern(/^[A-Za-z0-9\-]+$/)
      ],
      fullWidth: true
    },
    {
      name: 'specialty',
      label: 'Especialidad',
      type: 'text',
      placeholder: 'Ej: Odontología General',
      validators: [
        Validators.required,
        Validators.maxLength(150)
      ],
      fullWidth: true
    }
  ]);

  // Propiedades calculadas para estadísticas
  get totalUsers(): number {
    return this.totalElements; // Total de usuarios desde el backend
  }

  get activeUsers(): number {
    // Nota: Esta estadística se calcula solo sobre los usuarios de la página actual
    return this.users.filter(u => u.isActive).length;
  }

  get totalDentists(): number {
    // Nota: Esta estadística se calcula solo sobre los usuarios de la página actual
    return this.users.filter(u => u.role === 'DENTIST').length;
  }

  constructor() {
    // El formulario ahora se maneja con generic-form
    this.createDentistForm = this.fb.group({});
  }

  ngOnInit() {
    this.loadUsers();
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading = true;
    this.adminService.getAllUsers(this.currentPage, this.pageSize, 'id', 'ASC').subscribe({
      next: (response: PagedResponse<UserDetailResponse>) => {
        this.users = response.content;
        this.filteredUsers = response.content;
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        
        // Actualizar el paginator con la información del servidor (sin resetear el pageIndex actual)
        if (this.paginator) {
          this.paginator.length = this.totalElements;
        }
        
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
    // Filtrar solo los usuarios de la página actual
    if (!this.searchTerm.trim()) {
      this.filteredUsers = this.users;
      this.dataSource.data = this.users;
    } else {
      const search = this.searchTerm.toLowerCase().trim();
      this.filteredUsers = this.users.filter(user =>
        user.firstName?.toLowerCase().includes(search) ||
        user.lastName?.toLowerCase().includes(search) ||
        user.email?.toLowerCase().includes(search) ||
        user.role?.toLowerCase().includes(search)
      );
      this.dataSource.data = this.filteredUsers;
    }
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

  getUserDisplayName(user: UserDetailResponse): string {
    return `${user.firstName} ${user.lastName} (${user.email})`;
  }

  onGenericFormSubmit(formValue: any) {
    if (this.isCreatingDentist) return;

    this.isCreatingDentist = true;
    this.createDentistError = '';
    
    const request: CreateDentistRequest = {
      email: formValue.email,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      licenseNumber: formValue.licenseNumber,
      specialty: formValue.specialty
    };

    this.adminService.createDentist(request).subscribe({
      next: (response) => {
        const message = response.userAlreadyExisted 
          ? `Dentista creado exitosamente. El usuario ${response.email} fue actualizado a dentista.`
          : `Dentista creado exitosamente. Usuario ${response.email} creado con contraseña: 123456`;
        
        this.snackBar.open(message, 'Cerrar', {
          duration: 5000
        });
        
        // Limpiar datos preservados y error al tener éxito
        this.preservedFormData = {};
        this.createDentistError = '';
        
        // Resetear el formulario
        if (this.createDentistFormRef) {
          this.createDentistFormRef.resetForm();
        }
        
        // Volver a la primera página y recargar usuarios
        this.currentPage = 0;
        if (this.paginator) {
          this.paginator.firstPage();
        }
        this.loadUsers();
        this.isCreatingDentist = false;
      },
      error: (error) => {
        console.error('Error al crear dentista:', error);
        
        // Preservar los datos del formulario
        this.preservedFormData = { ...formValue };
        
        // Extraer el mensaje de error
        let errorMessage = 'Error al crear dentista';
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.status === 400) {
          errorMessage = 'Datos inválidos. Por favor, revisa la información ingresada.';
        } else if (error.status === 409) {
          errorMessage = 'El email ya está registrado en el sistema.';
        } else if (error.status === 403) {
          errorMessage = 'No tienes permisos para realizar esta acción.';
        } else if (error.status === 0) {
          errorMessage = 'Error de conexión con el servidor. Por favor, intenta nuevamente.';
        }
        
        // Mostrar error en snackbar y en el componente
        this.createDentistError = errorMessage;
        this.snackBar.open(errorMessage, 'Cerrar', {
          duration: 5000
        });
        this.isCreatingDentist = false;
      }
    });
  }

  openConfirmDialog(user: UserDetailResponse, action: 'activate' | 'deactivate'): void {
    const dialogData: ConfirmUserActionData = {
      userName: this.getFullName(user),
      userEmail: user.email,
      isActive: user.isActive,
      action: action
    };

    const dialogRef = this.dialog.open(ConfirmUserActionDialogComponent, {
      width: '500px',
      maxWidth: '90vw',
      data: dialogData,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        if (action === 'activate') {
          this.activateUser(user);
        } else {
          this.deactivateUser(user);
        }
      }
    });
  }

  activateUser(user: UserDetailResponse): void {
    this.isLoading = true;
    this.adminService.activateUser(user.id).subscribe({
      next: () => {
        this.snackBar.open(
          `Usuario ${this.getFullName(user)} activado exitosamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.loadUsers();
      },
      error: (error) => {
        console.error('Error al activar usuario:', error);
        const errorMessage = error.error?.message || 'Error al activar usuario';
        this.snackBar.open(errorMessage, 'Cerrar', {
          duration: 5000
        });
        this.isLoading = false;
      }
    });
  }

  deactivateUser(user: UserDetailResponse): void {
    this.isLoading = true;
    this.adminService.deactivateUser(user.id).subscribe({
      next: () => {
        this.snackBar.open(
          `Usuario ${this.getFullName(user)} desactivado exitosamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.loadUsers();
      },
      error: (error) => {
        console.error('Error al desactivar usuario:', error);
        const errorMessage = error.error?.message || 'Error al desactivar usuario';
        this.snackBar.open(errorMessage, 'Cerrar', {
          duration: 5000
        });
        this.isLoading = false;
      }
    });
  }
}
