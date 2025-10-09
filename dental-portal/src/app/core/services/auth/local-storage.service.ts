import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { User } from '../../../interfaces/user/user.interface';

@Injectable({
  providedIn: 'root',
})
export class LocalStorageService {

  private platformId = inject(PLATFORM_ID);

  setAuthToken(token: string) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('authToken', token);
    }
  }

  setUserData(user: User) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('user', JSON.stringify(user));
    }
  }

  getAuthToken() {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('authToken');
    }
    return null;
  }

  getUserData() {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('user');
    }
    return null;
  }

  clearLocalStorage() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.clear();
    }
  }
}
