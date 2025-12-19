// Message Types
export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  TEXT_WITH_IMAGE = 'TEXT_WITH_IMAGE'
}

export interface Message {
  id: string;
  content: string;
  isFromUser: boolean;
  timestamp: number;
  imageUri?: string;
  messageType: MessageType;
  personalityId?: string;
}

// AI Personality Types
export enum ResponseStyle {
  FRIENDLY = 'FRIENDLY',
  PROFESSIONAL = 'PROFESSIONAL',
  CASUAL = 'CASUAL',
  CREATIVE = 'CREATIVE',
  TECHNICAL = 'TECHNICAL',
  FUNNY = 'FUNNY',
  LOVING = 'LOVING',
  GENIUS = 'GENIUS',
  ULTIMATE = 'ULTIMATE',
  SPORTS = 'SPORTS',
  MUSIC = 'MUSIC'
}

export interface AIPersonality {
  id: string;
  name: string;
  description: string;
  icon: string;
  greeting: string;
  responseStyle: ResponseStyle;
  color: string;
}

// Conversation Context
export interface ConversationPair {
  role: 'user' | 'assistant';
  content: string;
}

// Generated Music
export interface GeneratedMusic {
  id: string;
  prompt: string;
  url: string;
  durationSeconds: number;
  timestamp: number;
  isFreeTier: boolean;
  costCents: number;
}
