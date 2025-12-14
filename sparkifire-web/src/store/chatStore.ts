import { create } from 'zustand';
import { Message, AIPersonality, MessageType } from '../types';
import { personalities } from '../data/personalities';
import { geminiService } from '../services/geminiService';
import { storageService } from '../services/storageService';
import { musicService } from '../services/musicService'; // Added import for musicService

interface ChatState {
  messages: Message[];
  isLoading: boolean;
  currentPersonality: AIPersonality;
  isListening: boolean;
  isSpeaking: boolean;
  isGeneratingMusic: boolean;
  musicStatus: string | null;
  
  // Actions
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
  generateMusic: (payload: string) => Promise<string | null>;
  initialize: () => void;
}

export const useChatStore = create<ChatState>((set, get) => ({
  messages: [],
  isLoading: false,
  currentPersonality: personalities.DEFAULT,
  isListening: false,
  isSpeaking: false,
  isGeneratingMusic: false,
  musicStatus: null,

  initialize: () => {
    const { currentPersonality } = get();
    const savedMessages = storageService.loadMessages(currentPersonality.id);
    
    // Check for auto-reset
    if (storageService.shouldAutoReset(currentPersonality.id)) {
      storageService.clearMessages(currentPersonality.id);
      const autoResetMessage: Message = {
        id: crypto.randomUUID(),
        content: '🔄 Starting fresh! Previous conversation was automatically reset.',
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id
      };
      set({ messages: [autoResetMessage] });
      storageService.saveMessages(currentPersonality.id, [autoResetMessage]);
    } else if (savedMessages.length > 0) {
      set({ messages: savedMessages });
    } else {
      // Add greeting if no messages
      const greetingMessage: Message = {
        id: crypto.randomUUID(),
        content: currentPersonality.greeting,
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id
      };
      set({ messages: [greetingMessage] });
      storageService.saveMessages(currentPersonality.id, [greetingMessage]);
    }
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
        personalityId: currentPersonality.id
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
      personalityId: currentPersonality.id
    };

    const updatedMessages = [...get().messages, userMessage];
    set({ messages: updatedMessages, isLoading: true });
    storageService.saveMessages(currentPersonality.id, updatedMessages);

    try {
      // Get conversation context
      const conversationContext = storageService.getConversationContext(currentPersonality.id);

      // Get AI response
      let aiResponse: string;
      if (messageType === MessageType.IMAGE || messageType === MessageType.TEXT_WITH_IMAGE) {
        if (!imageFile) {
          aiResponse = 'I could not access the photo you attached. Please try sending the image again.';
        } else {
          aiResponse = await geminiService.analyzeImage(
            content,
            imageFile,
            currentPersonality,
            conversationContext
          );
        }
      } else {
        aiResponse = await geminiService.generateResponse(
          content,
          currentPersonality,
          conversationContext
        );
      }

      // Add AI message
      const aiMessage: Message = {
        id: crypto.randomUUID(),
        content: aiResponse,
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id
      };

      const finalMessages = [...updatedMessages, aiMessage];
      set({ messages: finalMessages, isLoading: false });
      storageService.saveMessages(currentPersonality.id, finalMessages);

    } catch (error) {
      console.error('Error getting AI response:', error);
      const errorMessage: Message = {
        id: crypto.randomUUID(),
        content: error instanceof Error && error.message === 'Missing image data for analysis'
          ? 'I could not access the photo you attached. Please try sending the image again.'
          : 'Sorry, I encountered an error while processing your request. Please try again.',
        isFromUser: false,
        timestamp: Date.now(),
        messageType: MessageType.TEXT,
        personalityId: currentPersonality.id
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
    const savedMessages = storageService.loadMessages(personality.id);
    
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
        personalityId: personality.id
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
      personalityId: currentPersonality.id
    };
    
    set({ messages: [greetingMessage] });
    storageService.saveMessages(currentPersonality.id, [greetingMessage]);
  },

  setIsListening: (isListening: boolean) => {
    set({ isListening });
  },

  setIsSpeaking: (isSpeaking: boolean) => {
    set({ isSpeaking });
  },

  setMusicStatus: (status: string | null) => {
    set({ musicStatus: status });
  },

  generateMusic: async (payload: string) => {
    if (get().isGeneratingMusic) {
      return null;
    }

    set({ isGeneratingMusic: true, musicStatus: '✨ Generating your song...' });

    try {
      const result = await musicService.generateClip(payload);
      set({ musicStatus: result, isGeneratingMusic: false });
      return result;
    } catch (error) {
      console.error('generateMusic error', error);
      const fallback = 'Sorry, I could not reach the music service right now. Please try again later.';
      set({ musicStatus: fallback, isGeneratingMusic: false });
      return fallback;
    }
  }
}));
