import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserProfileRequest, UserProfileUpdateRequest } from '../../../interfaces/user/user.interface';
import { Observable } from 'rxjs';
import { ApiConfig } from '../../config/api.config';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfig);
  private apiUrl = this.apiConfig.usersApiUrl;

  getUser(): Observable<UserProfileRequest> {
    return this.http.get<UserProfileRequest>(`${this.apiUrl}/profile`);
  }

  updateUserProfile(updateData: UserProfileUpdateRequest): Observable<UserProfileUpdateRequest> {
    return this.http.put<UserProfileUpdateRequest>(`${this.apiUrl}/profile`, updateData);
  }
}
