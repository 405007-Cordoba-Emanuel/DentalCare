export interface ChatMessage {
  id?: number;
  conversationId: number;
  senderId: number;
  senderRole: 'DENTIST' | 'PATIENT';
  messageText?: string;
  fileUrl?: string;
  fileName?: string;
  fileType?: 'image' | 'pdf';
  isRead?: boolean;
  createdDatetime?: string;
}

export interface Conversation {
  id: number;
  dentistId: number;
  dentistName?: string;
  dentistLicenseNumber?: string;
  patientId: number;
  patientName?: string;
  patientDni?: string;
  lastMessageDatetime?: string;
  lastMessagePreview?: string;
  dentistUnreadCount?: number;
  patientUnreadCount?: number;
  createdDatetime?: string;
  messages?: ChatMessage[];
}

export interface ConversationSummary {
  id: number;
  conversationId: number;
  contactName: string;
  contactInitials: string;
  contactId: number;
  contactRole: 'DENTIST' | 'PATIENT';
  lastMessageDatetime?: string;
  lastMessagePreview?: string;
  unreadCount: number;
  lastMessageTime?: string;
}

export interface ChatMessageRequest {
  conversationId: number;
  messageText?: string;
  fileUrl?: string;
  fileName?: string;
  fileType?: 'image' | 'pdf';
}

export interface UnreadCountResponse {
  unreadCount: number;
}

