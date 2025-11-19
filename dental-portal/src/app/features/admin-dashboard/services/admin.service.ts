import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDetailResponse } from '../interfaces/user-detail.interface';
import { CreateDentistRequest, CreateDentistResponse } from '../interfaces/create-dentist.interface';
import { PagedResponse } from '../interfaces/paginated-response.interface';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private usersApiUrl = 'http://localhost:8081/api/users';

  // Obtener todos los usuarios paginado (solo ADMIN)
  getAllUsers(page: number = 0, size: number = 10, sortBy: string = 'id', sortDir: string = 'ASC'): Observable<PagedResponse<UserDetailResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    
    return this.http.get<PagedResponse<UserDetailResponse>>(this.usersApiUrl, { params });
  }

  // Crear dentista directamente (crea usuario + dentista)
  createDentist(request: CreateDentistRequest): Observable<CreateDentistResponse> {
    return this.http.post<CreateDentistResponse>(`${this.usersApiUrl}/admin/create-dentist`, request);
  }

  deactivateUser(userId: number): Observable<void> {
    return this.http.put<void>(`${this.usersApiUrl}/${userId}/deactivate`, {});
  }

  activateUser(userId: number): Observable<void> {
    return this.http.put<void>(`${this.usersApiUrl}/${userId}/activate`, {});
  }
}

