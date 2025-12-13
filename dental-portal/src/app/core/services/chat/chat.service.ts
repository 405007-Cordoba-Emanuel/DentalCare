import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ChatMessage, Conversation, ConversationSummary, ChatMessageRequest, UnreadCountResponse } from './chat.interfaces';
import { AuthService } from '../auth/auth.service';
import { ApiConfig } from '../../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiConfig = inject(ApiConfig);
  private apiUrl = this.apiConfig.coreChatUrl;
  private wsUrl = this.apiConfig.webSocketUrl;
  
  private stompClient: Client | null = null;
  private connected = false;
  private connectionSubject = new BehaviorSubject<boolean>(false);
  public connection$ = this.connectionSubject.asObservable();

  private messageSubject = new Subject<ChatMessage>();
  public message$ = this.messageSubject.asObservable();

  constructor() {
    this.initializeWebSocket();
  }

  private initializeWebSocket(): void {
    const socket = new SockJS(this.wsUrl);
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connected = true;
        this.connectionSubject.next(true);
        console.log('WebSocket connected');
      },
      onDisconnect: () => {
        this.connected = false;
        this.connectionSubject.next(false);
        console.log('WebSocket disconnected');
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      }
    });

    this.stompClient.activate();
  }

  subscribeToConversation(conversationId: number): void {
    if (!this.stompClient || !this.connected) {
      console.warn('WebSocket not connected');
      return;
    }

    this.stompClient.subscribe(`/topic/conversation/${conversationId}`, (message: IMessage) => {
      const chatMessage: ChatMessage = JSON.parse(message.body);
      this.messageSubject.next(chatMessage);
    });
  }

  subscribeToUserQueue(userId: number): void {
    if (!this.stompClient || !this.connected) {
      console.warn('WebSocket not connected');
      return;
    }

    this.stompClient.subscribe(`/queue/chat/${userId}`, (message: IMessage) => {
      const chatMessage: ChatMessage = JSON.parse(message.body);
      this.messageSubject.next(chatMessage);
    });
  }

  unsubscribeFromConversation(conversationId: number): void {
    if (!this.stompClient || !this.connected) {
      return;
    }
    // El cliente STOMP maneja la desuscripción automáticamente
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.connected = false;
      this.connectionSubject.next(false);
    }
  }

  // REST API Methods
  getConversations(searchTerm?: string): Observable<ConversationSummary[]> {
    let params = new HttpParams();
    if (searchTerm) {
      params = params.set('searchTerm', searchTerm);
    }
    return this.http.get<ConversationSummary[]>(`${this.apiUrl}/conversations`, { params });
  }

  getConversationById(conversationId: number): Observable<Conversation> {
    return this.http.get<Conversation>(`${this.apiUrl}/conversations/${conversationId}`);
  }

  getOrCreateConversation(dentistId: number, patientId: number): Observable<Conversation> {
    const params = new HttpParams()
      .set('dentistId', dentistId.toString())
      .set('patientId', patientId.toString());
    return this.http.post<Conversation>(`${this.apiUrl}/conversations`, null, { params });
  }

  sendMessage(request: ChatMessageRequest, file?: File): Observable<ChatMessage> {
    const formData = new FormData();
    formData.append('conversationId', request.conversationId.toString());
    if (request.messageText) {
      formData.append('messageText', request.messageText);
    }
    if (file) {
      formData.append('file', file);
    }

    return this.http.post<ChatMessage>(`${this.apiUrl}/messages`, formData);
  }

  markAsRead(conversationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/conversations/${conversationId}/read`, null);
  }

  getUnreadCount(): Observable<UnreadCountResponse> {
    return this.http.get<UnreadCountResponse>(`${this.apiUrl}/unread-count`);
  }
}

