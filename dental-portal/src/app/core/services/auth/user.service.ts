import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { UserProfileRequest } from '../../../interfaces/user/user.interface';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/users';

  getUser(): Observable<UserProfileRequest> {
    return this.http.get<UserProfileRequest>(`${this.apiUrl}`);
  }
}
