import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  private _isSidebarOpen = new BehaviorSubject<boolean>(true);
  
  get isSidebarOpen$() {
    return this._isSidebarOpen.asObservable();
  }
  
  get isSidebarOpen() {
    return this._isSidebarOpen.value;
  }
  
  toggleSidebar() {
    this._isSidebarOpen.next(!this._isSidebarOpen.value);
  }
  
  openSidebar() {
    this._isSidebarOpen.next(true);
  }
  
  closeSidebar() {
    this._isSidebarOpen.next(false);
  }
} 