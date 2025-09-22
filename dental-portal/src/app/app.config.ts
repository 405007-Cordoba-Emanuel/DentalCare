import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { ChevronLeft, LucideAngularModule } from 'lucide-angular';
import { Shield, Bell, Settings, LogOut, Calendar, Clock, Users, MessageCircle, Camera, FileText, ChevronDown, ChevronRight, MoreHorizontal, Search, PlusIcon, Mail, Phone } from 'lucide-angular/src/icons';

export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({ eventCoalescing: true }), provideRouter(routes), provideClientHydration(withEventReplay()), importProvidersFrom(
    LucideAngularModule.pick({ Shield, Bell, Settings, LogOut, Calendar, Clock, Users, MessageCircle, Camera, FileText, ChevronDown, ChevronRight, MoreHorizontal, ChevronLeft, Search, PlusIcon,
      Mail, Phone})
  )]
};
