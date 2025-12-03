import { Component, OnInit, OnDestroy, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ChatService } from '../../core/services/chat/chat.service';
import { AuthService } from '../../core/services/auth/auth.service';
import { ConversationSummary, Conversation, ChatMessage } from '../../core/services/chat/chat.interfaces';
import { LocalStorageService } from '../../core/services/auth/local-storage.service';
import { DentistService } from '../../core/services/dentist.service';
import { PatientService } from '../../core/services/patient.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  private chatService = inject(ChatService);
  private authService = inject(AuthService);
  private localStorage = inject(LocalStorageService);
  private dentistService = inject(DentistService);
  private patientService = inject(PatientService);
  private dialog = inject(MatDialog);
  private destroy$ = new Subject<void>();

  conversations: ConversationSummary[] = [];
  selectedConversation: Conversation | null = null;
  messages: ChatMessage[] = [];
  searchTerm: string = '';
  messageText: string = '';
  selectedFile: File | null = null;
  isLoading = false;
  isSending = false;
  currentUser: any = null;
  userRole: string = '';
  userId: string = '';
  dentistId: number | null = null;
  patientId: number | null = null;
  
  @ViewChild('fileInput', { static: false }) fileInput!: ElementRef<HTMLInputElement>;

  ngOnInit(): void {
    this.loadCurrentUser();
    this.subscribeToMessages();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.chatService.disconnect();
  }

  private loadCurrentUser(): void {
    // Verificar primero si el usuario está autenticado
    if (!this.authService.isAuthenticated()) {
      console.error('User not authenticated');
      return;
    }

    // Primero intentar obtener el usuario del observable
    const currentUser = this.authService.currentUser;
    if (currentUser) {
      this.setupUser(currentUser);
    } else {
      // Si no hay usuario en el observable, intentar obtenerlo del localStorage
      const userDataString = this.localStorage.getUserData();
      if (userDataString) {
        try {
          const user = JSON.parse(userDataString);
          this.setupUser(user);
        } catch (error) {
          console.error('Error parsing user data:', error);
        }
      }
    }
    
    // También suscribirse a cambios
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      if (user) {
        this.setupUser(user);
      }
    });
  }

  private setupUser(user: any): void {
    this.currentUser = user;
    this.userRole = user.role;
    this.userId = user.id;
    this.loadUserIds();
  }

  private loadUserIds(): void {
    if (this.userRole === 'DENTIST') {
      this.dentistService.getDentistIdByUserId(this.userId.toString()).subscribe({
        next: (id) => {
          this.dentistId = id;
          // Esperar a que el WebSocket esté conectado antes de suscribirse
          this.chatService.connection$.pipe(takeUntil(this.destroy$)).subscribe(connected => {
            if (connected && id) {
              this.chatService.subscribeToUserQueue(id);
            }
          });
          // Cargar conversaciones después de obtener el ID
          this.loadConversations();
        },
        error: (err) => {
          console.error('Error loading dentist ID:', err);
          // Intentar cargar conversaciones de todas formas
          this.loadConversations();
        }
      });
    } else if (this.userRole === 'PATIENT') {
      this.patientService.getPatientIdByUserId(this.userId.toString()).subscribe({
        next: (id) => {
          this.patientId = id;
          // Esperar a que el WebSocket esté conectado antes de suscribirse
          this.chatService.connection$.pipe(takeUntil(this.destroy$)).subscribe(connected => {
            if (connected && id) {
              this.chatService.subscribeToUserQueue(id);
            }
          });
          // Cargar conversaciones después de obtener el ID
          this.loadConversations();
        },
        error: (err) => {
          console.error('Error loading patient ID:', err);
          // Intentar cargar conversaciones de todas formas
          this.loadConversations();
        }
      });
    } else {
      // Si no hay rol, intentar cargar de todas formas
      this.loadConversations();
    }
  }

  private subscribeToMessages(): void {
    this.chatService.message$.pipe(takeUntil(this.destroy$)).subscribe(message => {
      if (this.selectedConversation && message.conversationId === this.selectedConversation.id) {
        // Verificar si el mensaje ya existe para evitar duplicados
        const messageExists = this.messages.some(m => m.id === message.id);
        if (!messageExists) {
          this.messages.push(message);
          this.scrollToBottom();
        }
      }
      // Actualizar la lista de conversaciones para reflejar nuevos mensajes
      this.loadConversations();
    });
  }

  loadConversations(): void {
    // Solo cargar si tenemos el rol del usuario y está autenticado
    if (!this.userRole) {
      return;
    }

    // Verificar que el usuario esté autenticado
    if (!this.authService.isAuthenticated()) {
      console.error('User not authenticated, cannot load conversations');
      return;
    }

    this.isLoading = true;
    const search = this.userRole === 'DENTIST' ? this.searchTerm : undefined;
    
    this.chatService.getConversations(search).subscribe({
      next: (conversations) => {
        this.conversations = conversations || [];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading conversations:', err);
        this.conversations = [];
        this.isLoading = false;
        
        // Si es un error 401, el usuario no está autenticado
        if (err.status === 401) {
          console.error('User not authenticated. Please login again.');
        }
      }
    });
  }

  selectConversation(conversation: ConversationSummary): void {
    this.isLoading = true;
    
    // Si no hay conversationId, crear la conversación primero
    if (!conversation.conversationId) {
      if (this.userRole === 'DENTIST' && this.dentistId && conversation.contactId) {
        // Crear conversación para dentista
        this.chatService.getOrCreateConversation(this.dentistId, conversation.contactId).subscribe({
          next: (conv) => {
            this.selectedConversation = conv;
            this.messages = conv.messages || [];
            // Suscribirse a la conversación
            this.subscribeToConversationIfConnected(conv.id);
            this.isLoading = false;
            // Recargar la lista de conversaciones para actualizar el ID
            this.loadConversations();
            // Scroll después de que el DOM se actualice
            setTimeout(() => this.scrollToBottom(), 100);
          },
          error: (err) => {
            console.error('Error creating conversation:', err);
            this.isLoading = false;
          }
        });
      } else if (this.userRole === 'PATIENT' && this.patientId && conversation.contactId) {
        // Crear conversación para paciente
        this.chatService.getOrCreateConversation(conversation.contactId, this.patientId).subscribe({
          next: (conv) => {
            this.selectedConversation = conv;
            this.messages = conv.messages || [];
            // Suscribirse a la conversación
            this.subscribeToConversationIfConnected(conv.id);
            this.isLoading = false;
            // Recargar la lista de conversaciones para actualizar el ID
            this.loadConversations();
            // Scroll después de que el DOM se actualice
            setTimeout(() => this.scrollToBottom(), 100);
          },
          error: (err) => {
            console.error('Error creating conversation:', err);
            this.isLoading = false;
          }
        });
      } else {
        this.isLoading = false;
      }
      return;
    }
    
    // Si ya existe la conversación, cargarla normalmente
    this.chatService.getConversationById(conversation.conversationId).subscribe({
      next: (conv) => {
        this.selectedConversation = conv;
        this.messages = conv.messages || [];
        // Suscribirse a la conversación
        this.subscribeToConversationIfConnected(conv.id);
        // Marcar como leída y actualizar el contador localmente
        this.chatService.markAsRead(conv.id).subscribe({
          next: () => {
            // Actualizar el unreadCount en la lista de conversaciones
            const convIndex = this.conversations.findIndex(c => c.conversationId === conv.id);
            if (convIndex !== -1) {
              this.conversations[convIndex].unreadCount = 0;
            }
          }
        });
        this.isLoading = false;
        // Recargar conversaciones para actualizar la lista
        this.loadConversations();
        // Scroll después de que el DOM se actualice
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (err) => {
        console.error('Error loading conversation:', err);
        this.isLoading = false;
      }
    });
  }

  private subscribeToConversationIfConnected(conversationId: number): void {
    // Intentar suscribirse directamente (el servicio maneja si está conectado o no)
    this.chatService.subscribeToConversation(conversationId);
  }

  sendMessage(): void {
    if (!this.selectedConversation || (!this.messageText.trim() && !this.selectedFile)) {
      return;
    }

    // Prevenir múltiples envíos
    if (this.isSending) {
      return;
    }

    this.isSending = true;
    const request = {
      conversationId: this.selectedConversation.id,
      messageText: this.messageText.trim() || undefined
    };

    const messageTextToSend = this.messageText.trim();
    const fileToSend = this.selectedFile;

    // Limpiar el input inmediatamente para evitar envíos duplicados
    this.messageText = '';
    this.selectedFile = null;

    this.chatService.sendMessage(request, fileToSend || undefined).subscribe({
      next: (message) => {
        // Verificar si el mensaje ya existe (puede llegar por WebSocket antes)
        const messageExists = this.messages.some(m => m.id === message.id);
        if (!messageExists) {
          this.messages.push(message);
          this.scrollToBottom();
        }
        this.isSending = false;
        // Actualizar lista de conversaciones
        this.loadConversations();
      },
      error: (err) => {
        console.error('Error sending message:', err);
        // Restaurar el mensaje si hubo error
        this.messageText = messageTextToSend;
        this.selectedFile = fileToSend;
        this.isSending = false;
      }
    });
  }

  triggerFileInput(): void {
    if (this.fileInput?.nativeElement) {
      this.fileInput.nativeElement.click();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      // Validar tipo de archivo
      if (file.type.startsWith('image/') || file.type === 'application/pdf') {
        // Validar tamaño (10MB)
        if (file.size <= 10 * 1024 * 1024) {
          this.selectedFile = file;
        } else {
          alert('El archivo es demasiado grande. Máximo 10MB.');
        }
      } else {
        alert('Solo se permiten imágenes (JPG, PNG) y PDFs.');
      }
    }
  }

  removeFile(): void {
    this.selectedFile = null;
  }

  viewFile(fileUrl: string, fileName: string, fileType?: string): void {
    if (!fileUrl) {
      console.error('File URL is not available');
      return;
    }

    const isPdf = fileType === 'pdf' || fileName.toLowerCase().endsWith('.pdf') || fileUrl.toLowerCase().includes('.pdf');
    const isImage = fileType === 'image' || fileUrl.toLowerCase().match(/\.(jpg|jpeg|png|gif|webp)$/i);

    if (isPdf) {
      // Para PDFs, abrir directamente en nueva pestaña
      // Cloudinary debe tener habilitada la opción "PDF and ZIP files delivery"
      try {
        window.open(fileUrl, '_blank', 'noopener,noreferrer');
      } catch (error) {
        console.error('Error opening PDF:', error);
        // Si falla, intentar con enlace temporal como fallback
        const link = document.createElement('a');
        link.href = fileUrl;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        link.style.display = 'none';
        document.body.appendChild(link);
        link.click();
        setTimeout(() => {
          if (document.body.contains(link)) {
            document.body.removeChild(link);
          }
        }, 100);
      }
    } else {
      // Para imágenes y otros tipos, abrir directamente en nueva pestaña
      window.open(fileUrl, '_blank', 'noopener,noreferrer');
    }
  }

  private downloadFileDirectly(fileUrl: string, fileName: string): void {
    // Crear un enlace temporal para descargar el archivo
    // Usar un enfoque que evite problemas de CORS
    const link = document.createElement('a');
    link.href = fileUrl;
    link.download = fileName;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    
    // Agregar al DOM, hacer click y remover
    document.body.appendChild(link);
    link.click();
    
    // Remover después de un pequeño delay para asegurar que el click se procese
    setTimeout(() => {
      if (document.body.contains(link)) {
        document.body.removeChild(link);
      }
    }, 100);
  }

  getInitials(name: string): string {
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  formatTime(dateTime: string | undefined): string {
    if (!dateTime) return '';
    
    // El servidor envía fechas en formato "yyyy-MM-dd HH:mm:ss" sin zona horaria
    // El servidor Java usa LocalDateTime.now() que usa la zona horaria del sistema JVM
    // Si el servidor JVM está en UTC, LocalDateTime.now() guardará en UTC
    // Si el servidor JVM está en Buenos Aires, LocalDateTime.now() guardará en Buenos Aires
    
    // Solución más simple: extraer la hora directamente del string
    // Si el servidor guarda en hora local de Buenos Aires, esto mostrará la hora correcta
    // Si el servidor guarda en UTC, necesitaremos ajustar en el backend
    
    const normalizedDateTime = dateTime.replace('T', ' ').trim();
    const parts = normalizedDateTime.split(' ');
    
    if (parts.length >= 2) {
      const timePart = parts[1]; // "16:03:00"
      const timeOnly = timePart.substring(0, 5); // "16:03"
      return timeOnly;
    }
    
    // Fallback: parsear como fecha
    const date = new Date(dateTime.replace(' ', 'T'));
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  isMyMessage(message: ChatMessage): boolean {
    return message.senderId.toString() === this.userId && message.senderRole === this.userRole;
  }

  scrollToBottom(): void {
    // Usar múltiples intentos para asegurar que el scroll funcione después del renderizado
    // Esto es necesario porque Angular necesita tiempo para renderizar los mensajes en el DOM
    setTimeout(() => {
      const messagesContainer = document.querySelector('.messages-container') as HTMLElement;
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    }, 50);
    
    // Segundo intento después de un delay mayor para asegurar que el DOM esté completamente renderizado
    setTimeout(() => {
      const messagesContainer = document.querySelector('.messages-container') as HTMLElement;
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    }, 200);
    
    // Tercer intento para casos donde hay muchos mensajes o imágenes que tardan en cargar
    setTimeout(() => {
      const messagesContainer = document.querySelector('.messages-container') as HTMLElement;
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    }, 500);
  }

  onSearchChange(): void {
    if (this.userRole === 'DENTIST') {
      this.loadConversations();
    }
  }
}
