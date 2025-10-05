import { Injectable } from '@angular/core';
import { User } from '../../../interfaces/user/user.interface';

@Injectable({
  providedIn: 'root',
})
export class LocalStorageService {
  setAuthToken(token: string) {
    localStorage.setItem('authToken', token);
  }

  setUserData(user: User) {
    localStorage.setItem('user', JSON.stringify(user));
  }

  getAuthToken() {
    return localStorage.getItem('authToken');
  }

  getUserData() {
    return localStorage.getItem('user');
  }

  clearLocalStorage() {
    localStorage.clear();
  }
}
