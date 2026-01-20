import { create } from 'zustand';
import { Message, AIPersonality, MessageType, FavoriteSpark, GeneratedMusic } from '../types';
import { personalities } from '../data/personalities';
import { groqService } from '../services/groqService';
import { storageService } from '../services/storageService';
import { musicService } from '../services/musicService';

const ensureFavoriteFlag = (message: Message): Message => ({
  ...message,
  isFavorite: message.isFavorite ?? false,
});

export interface ChatState {
  messages: Message[];
  isLoading: boolean;
  currentPersonality: AIPersonality;
  isListening: boolean;
  isSpeaking: boolean;
  isGeneratingMusic: boolean;
  musicStatus: string | null;
  favoriteSparks: FavoriteSpark[];
  musicLibrary: GeneratedMusic[];
  
  sendMessage: (
    content: string,
    imagePreview?: string,
    imageFile?: File,
    messageType?: MessageType
  ) => Promise<void>;
  changePersonality: (personality: AIPersonality) => void;
  clearMessages: () => void;
  startFresh: () => void;
  setIsListening: (isListening: boolean) => void;
  setIsSpeaking: (isSpeaking: boolean) => void;
  setMusicStatus: (status: string | null) => void;
  generateMusic: (payload: string, prompt: string) => Promise<string | null>;
  loadMusicLibrary: () => void;
  addGeneratedMusic: (music: GeneratedMusic) => void;
  deleteGeneratedMusic: (musicId: string) => void;
  markMusicAsRead: (musicId: string) => void;
  initialize: () => void;
  toggleFavorite: (messageId: string) => void;
}

export const useChatStore = create<ChatState>((set, get) => ({
  messages: [],
  isLoading: false,
  currentPersonality: personalities.DEFAULT,
  isListening: false,
  isSpeaking: false,
  isGeneratingMusic: false,
  musicStatus: null,
  favoriteSparks: [],
  musicLibrary: [],

  initialize: () => {
    const { currentPersonality } = get();
    const savedMessages = storageService.loadMessages(currentPersonality.id).map(ensureFavoriteFlag);
    
    // Check for auto-reset
    if (storageService.shouldAutoReset(currentPersonality.id)) {
      storageService.clearMessages(currentPersonality.id);
      const autoResetMessage: Message = {
        id: crypto.randomUUID(),
        content: '🔄 Starting fresh! Previous conversation was automatically reset.',
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id,
        isFavorite: false
      };
      set({ messages: [autoResetMessage] });
      storageService.saveMessages(currentPersonality.id, [autoResetMessage]);
    } else if (savedMessages.length > 0) {
      set({ messages: savedMessages });
    } else {
      set({ messages: [] });
    }

    const favorites = storageService.loadFavorites();
    const library = storageService.loadMusicLibrary<GeneratedMusic>();
    set({ favoriteSparks: favorites, musicLibrary: library });
  },

  sendMessage: async (
    content: string,
    imagePreview?: string,
    imageFile?: File,
    messageType = MessageType.TEXT
  ) => {
    const { currentPersonality, isLoading } = get();
    
    if ((content.trim().length === 0 && !imagePreview) || isLoading) {
      return;
    }

    // Check for auto-reset before sending
    if (storageService.shouldAutoReset(currentPersonality.id)) {
      storageService.clearMessages(currentPersonality.id);
      const autoResetMessage: Message = {
        id: crypto.randomUUID(),
        content: '🔄 Starting fresh! Previous conversation was automatically reset.',
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id,
        isFavorite: false
      };
      set({ messages: [autoResetMessage] });
      storageService.saveMessages(currentPersonality.id, [autoResetMessage]);
    }

    // Add user message
    const userMessage: Message = {
      id: crypto.randomUUID(),
      content,
      isFromUser: true,
      timestamp: Date.now(),
      imageUri: imagePreview,
      messageType,
      personalityId: currentPersonality.id,
      isFavorite: false
    };

    const updatedMessages = [...get().messages, userMessage];
    set({ messages: updatedMessages, isLoading: true });
    storageService.saveMessages(currentPersonality.id, updatedMessages);

    try {
      // Get conversation context
      const conversationContext = storageService.getConversationContext(currentPersonality.id);

      // Get AI response using Groq service
      const aiResponse = await groqService.generateResponse(
        content,
        currentPersonality,
        conversationContext
      );

      // Add AI message
      const aiMessage: Message = {
        id: crypto.randomUUID(),
        content: aiResponse,
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id,
        isFavorite: false
      };

      const finalMessages = [...updatedMessages, aiMessage];
      set({ messages: finalMessages, isLoading: false });
      storageService.saveMessages(currentPersonality.id, finalMessages);

    } catch (error) {
      console.error('Error getting AI response:', error);
      const errorMessage: Message = {
        id: crypto.randomUUID(),
        content: 'Sorry, I encountered an error while processing your request. Please try again.',
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id,
        isFavorite: false
      };

      const finalMessages = [...updatedMessages, errorMessage];
      set({ messages: finalMessages, isLoading: false });
      storageService.saveMessages(currentPersonality.id, finalMessages);
    }
  },

  changePersonality: (personality: AIPersonality) => {
    const { currentPersonality } = get();
    const messages = get().messages;
    
    // Save current conversation
    storageService.saveMessages(currentPersonality.id, messages);

    // Load new personality's conversation
    const savedMessages = storageService.loadMessages(personality.id).map(ensureFavoriteFlag);
    
    if (savedMessages.length > 0) {
      set({ currentPersonality: personality, messages: savedMessages });
    } else {
      // Add greeting for new personality
      const greetingMessage: Message = {
        id: crypto.randomUUID(),
        content: personality.greeting,
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: personality.id,
        isFavorite: false
      };
      set({ currentPersonality: personality, messages: [greetingMessage] });
      storageService.saveMessages(personality.id, [greetingMessage]);
    }
  },

  clearMessages: () => {
    const { currentPersonality } = get();
    storageService.clearMessages(currentPersonality.id);
    set({ messages: [] });
  },

  startFresh: () => {
    const { currentPersonality } = get();
    storageService.clearMessages(currentPersonality.id);
    
    const greetingMessage: Message = {
      id: crypto.randomUUID(),
      content: currentPersonality.greeting,
      isFromUser: false,
      timestamp: Date.now(),
      messageType: MessageType.TEXT,
      personalityId: currentPersonality.id,
      isFavorite: false
    };
    
    set({ messages: [greetingMessage] });
    storageService.saveMessages(currentPersonality.id, [greetingMessage]);
  },

  setIsListening: (isListening: boolean) => set({ isListening }),
  setIsSpeaking: (isSpeaking: boolean) => set({ isSpeaking }),
  setMusicStatus: (status: string | null) => set({ musicStatus: status }),
  loadMusicLibrary: () => {
    const library = storageService.loadMusicLibrary<GeneratedMusic>();
    set({ musicLibrary: library });
  },
  addGeneratedMusic: (music: GeneratedMusic) => {
    const updatedLibrary = [music, ...get().musicLibrary];
    storageService.saveMusicLibrary(updatedLibrary);
    set({ musicLibrary: updatedLibrary });
  },
  deleteGeneratedMusic: (musicId: string) => {
    const updatedLibrary = get().musicLibrary.filter((track) => track.id !== musicId);
    storageService.saveMusicLibrary(updatedLibrary);
    set({ musicLibrary: updatedLibrary });
  },
  markMusicAsRead: (musicId: string) => {
    const updatedLibrary = get().musicLibrary.map((track) =>
      track.id === musicId ? { ...track, isRead: true } : track
    );
    storageService.saveMusicLibrary(updatedLibrary);
    set({ musicLibrary: updatedLibrary });
  },

  generateMusic: async (payload: string, prompt: string): Promise<string | null> => {
    set({ isGeneratingMusic: true, musicStatus: 'Starting music generation...' });

    try {
      const result = await musicService.generateClip(payload, 'free');
      const downloadPrefix = 'Download it here: ';
      const downloadIndex = result.indexOf(downloadPrefix);
      let url: string | null = null;

      if (downloadIndex !== -1) {
        url = result.substring(downloadIndex + downloadPrefix.length).trim();
      }

      if (url) {
        const newTrack: GeneratedMusic = {
          id: crypto.randomUUID(),
          prompt,
          url,
          durationSeconds: 0,
          timestamp: Date.now(),
          isFreeTier: true,
          costCents: 0,
          isRead: false,
        };
        const updatedLibrary = [newTrack, ...get().musicLibrary];
        storageService.saveMusicLibrary(updatedLibrary);
        set({ musicLibrary: updatedLibrary });
      }

      set({ isGeneratingMusic: false, musicStatus: result });

      return result;
    } catch (error) {
      console.error('[generateMusic] Error:', error);
      set({ isGeneratingMusic: false, musicStatus: null });
      
      return 'Sorry, music generation failed. Please try again.';
    }
  },

  toggleFavorite: (messageId: string) => {
    const messages = get().messages;
    const { currentPersonality } = get();
    const messageIndex = messages.findIndex(m => m.id === messageId);
    
    if (messageIndex === -1) return;
    
    const message = messages[messageIndex];
    const isFavorite = !message.isFavorite;
    
    const updatedMessages = [
      ...messages.slice(0, messageIndex),
      { ...message, isFavorite },
      ...messages.slice(messageIndex + 1)
    ];
    
    set({ messages: updatedMessages });
    storageService.saveMessages(currentPersonality.id, updatedMessages);
    
    const favorites = storageService.loadFavorites();
    
    if (isFavorite) {
      const newFavorite: FavoriteSpark = {
        id: messageId,
        content: message.content,
        timestamp: message.timestamp,
        personalityId: currentPersonality.id,
        personalityName: currentPersonality.name
      };
      const updatedFavorites = [...favorites, newFavorite];
      storageService.saveFavorites(updatedFavorites);
      set({ favoriteSparks: updatedFavorites });
    } else {
      const updatedFavorites = favorites.filter(f => f.id !== messageId);
      storageService.saveFavorites(updatedFavorites);
      set({ favoriteSparks: updatedFavorites });
    }
  }
}));
