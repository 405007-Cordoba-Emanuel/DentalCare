import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDetailResponse } from '../interfaces/user-detail.interface';
import { CreateDentistRequest } from '../interfaces/create-dentist.interface';
import { DentistResponse } from '../../dentists/interfaces/dentist.interface';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private usersApiUrl = 'http://localhost:8081/api/users/advanced';
  private dentistApiUrl = 'http://localhost:8082/api/core/dentist';

  // Obtener todos los usuarios (solo ADMIN)
  getAllUsers(): Observable<UserDetailResponse[]> {
    return this.http.get<UserDetailResponse[]>(this.usersApiUrl);
  }

  // Crear dentista desde usuario existente
  createDentistFromUser(request: CreateDentistRequest): Observable<DentistResponse> {
    return this.http.post<DentistResponse>(`${this.dentistApiUrl}/create-from-user`, request);
  }
}

