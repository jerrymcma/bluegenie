import { create } from 'zustand';
import { Message, AIPersonality, MessageType, GeneratedMusic, UserSubscription } from '../types';
import { personalities } from '../data/personalities';
import { geminiService } from '../services/geminiService';
import { storageService } from '../services/storageService';
import { musicService } from '../services/musicService';
import { supabaseService } from '../services/supabaseService';

interface ChatState {
  messages: Message[];
  isLoading: boolean;
  currentPersonality: AIPersonality;
  isListening: boolean;
  isSpeaking: boolean;
  isGeneratingMusic: boolean;
  musicStatus: string | null;
  musicCredits: number;
  musicLibrary: GeneratedMusic[];
  
  // Subscription state
  user: any | null;
  subscription: UserSubscription;
  showUpgradeModal: boolean;
  showSignInModal: boolean;
  
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
  decrementMusicCredits: () => void;
  initialize: () => void;
  addMusicToLibrary: (music: GeneratedMusic) => void;
  deleteMusicFromLibrary: (id: string) => void;
  loadMusicLibrary: () => void;
  markMusicAsRead: (id: string) => void;
  
  // Subscription actions
  signIn: () => Promise<void>;
  signOut: () => Promise<void>;
  checkUsageLimits: () => boolean;
  upgradeToPremium: () => void;
  setShowUpgradeModal: (show: boolean) => void;
  setShowSignInModal: (show: boolean) => void;
  loadUserProfile: () => Promise<void>;
  incrementMessageCount: () => Promise<void>;
  incrementSongCount: () => Promise<void>;
}

export const useChatStore = create<ChatState>((set, get) => ({
  messages: [],
  isLoading: false,
  currentPersonality: personalities.DEFAULT,
  isListening: false,
  isSpeaking: false,
  isGeneratingMusic: false,
  musicStatus: null,
  musicCredits: 5,
  musicLibrary: [],
  
  // Subscription state
  user: null,
  subscription: {
    isPremium: false,
    messageCount: 0,
    songCount: 0,
    songsThisPeriod: 0,
    subscriptionStartDate: null,
    periodStartDate: null,
    needsRenewal: false,
  },
  showUpgradeModal: false,
  showSignInModal: false,

  initialize: () => {
    get().loadMusicLibrary();
    
    // Set up auth listener
    supabaseService.onAuthStateChange((user) => {
      set({ user });
      if (user) {
        get().loadUserProfile();
      }
    });
    
    // Check if user is already signed in
    supabaseService.getCurrentUser().then((user) => {
      if (user) {
        set({ user });
        get().loadUserProfile();
      }
    });
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
      // If there are no saved messages, ensure the state is empty
      set({ messages: [] });
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

    // Check usage limits before sending
    if (!get().checkUsageLimits()) {
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

      // Increment message count (counts the user message)
      get().incrementMessageCount();

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

  decrementMusicCredits: () => {
    set((state) => ({ musicCredits: Math.max(0, state.musicCredits - 1) }));
  },

  generateMusic: async (payload: string) => {
    const { isGeneratingMusic, musicCredits, decrementMusicCredits, addMusicToLibrary } = get();

    if (isGeneratingMusic) {
      return null;
    }
    
    // Check usage limits before generating
    if (!get().checkUsageLimits()) {
      return null;
    }
    
    if (musicCredits <= 0) {
      set({ musicStatus: 'You have no more free songs to generate.' });
      return null;
    }

    set({ isGeneratingMusic: true, musicStatus: '✨ Generating your music... ✨' });

    try {
      const result = await musicService.generateClip(payload);
      set({ musicStatus: result, isGeneratingMusic: false });
      decrementMusicCredits();
      
      // Extract URL from result and add to library
      const downloadPrefix = 'Download it here: ';
      const downloadIndex = result.indexOf(downloadPrefix);
      if (downloadIndex !== -1) {
        const url = result.substring(downloadIndex + downloadPrefix.length);
        const newMusic: GeneratedMusic = {
          id: crypto.randomUUID(),
          prompt: payload,
          url: url,
          durationSeconds: 58, // Default duration, can be updated if we get it from the API
          timestamp: Date.now(),
          isFreeTier: musicCredits > 0,
          costCents: musicCredits > 0 ? 0 : 6,
          isRead: false,
        };
        addMusicToLibrary(newMusic);
        
        // Increment song count
        get().incrementSongCount();
      }
      
      return result;
    } catch (error) {
      console.error('generateMusic error', error);
      const fallback = 'Sorry, I could not reach the music service right now. Please try again later.';
      set({ musicStatus: fallback, isGeneratingMusic: false });
      return fallback;
    }
  },

  addMusicToLibrary: (music: GeneratedMusic) => {
    const updatedLibrary = [music, ...get().musicLibrary];
    set({ musicLibrary: updatedLibrary });
    localStorage.setItem('musicLibrary', JSON.stringify(updatedLibrary));
  },

  deleteMusicFromLibrary: (id: string) => {
    const updatedLibrary = get().musicLibrary.filter((m) => m.id !== id);
    set({ musicLibrary: updatedLibrary });
    localStorage.setItem('musicLibrary', JSON.stringify(updatedLibrary));
  },

  loadMusicLibrary: () => {
    try {
      const saved = localStorage.getItem('musicLibrary');
      if (saved) {
        const library = JSON.parse(saved);
        set({ musicLibrary: library });
      }
    } catch (error) {
      console.error('Error loading music library:', error);
    }
  },

  markMusicAsRead: (id: string) => {
    const updatedLibrary = get().musicLibrary.map((m) =>
      m.id === id ? { ...m, isRead: true } : m
    );
    set({ musicLibrary: updatedLibrary });
    localStorage.setItem('musicLibrary', JSON.stringify(updatedLibrary));
  },

  // Subscription functions
  signIn: async () => {
    try {
      await supabaseService.signInWithGoogle();
      set({ showSignInModal: false });
    } catch (error) {
      console.error('Sign in error:', error);
      alert('Failed to sign in. Please try again.');
    }
  },

  signOut: async () => {
    try {
      await supabaseService.signOut();
      set({ 
        user: null, 
        subscription: {
          isPremium: false,
          messageCount: 0,
          songCount: 0,
          songsThisPeriod: 0,
          subscriptionStartDate: null,
          periodStartDate: null,
          needsRenewal: false,
        }
      });
    } catch (error) {
      console.error('Sign out error:', error);
    }
  },

  loadUserProfile: async () => {
    const user = get().user;
    if (!user) return;

    const profile = await supabaseService.getUserProfile(user.id, user.email);
    if (profile) {
      const needsRenewal = await supabaseService.checkSubscriptionRenewal(profile);
      
      set({
        subscription: {
          isPremium: profile.is_premium,
          messageCount: profile.message_count,
          songCount: profile.song_count,
          songsThisPeriod: profile.songs_this_period,
          subscriptionStartDate: profile.subscription_start_date,
          periodStartDate: profile.period_start_date,
          needsRenewal,
        }
      });
    }
  },

  checkUsageLimits: () => {
    const { user, subscription } = get();
    
    // If premium and needs renewal, show upgrade
    if (subscription.isPremium && subscription.needsRenewal) {
      set({ showUpgradeModal: true });
      return false;
    }
    
    // If not signed in, require login for song generation (messages are free and unlimited)
    if (!user) {
      set({ showSignInModal: true });
      return false;
    }
    
    // If signed in but not premium, check if they've used their 5 free songs
    if (!subscription.isPremium) {
      if (subscription.songCount >= 5) {
        set({ showUpgradeModal: true });
        return false;
      }
    }
    
    return true;
  },

  incrementMessageCount: async () => {
    const { user } = get();
    
    if (user) {
      // Increment in database
      await supabaseService.incrementMessageCount(user.id);
      set((state) => ({
        subscription: {
          ...state.subscription,
          messageCount: state.subscription.messageCount + 1,
        }
      }));
    } else {
      // Increment in localStorage for anonymous users
      const count = parseInt(localStorage.getItem('anonymousMessageCount') || '0');
      localStorage.setItem('anonymousMessageCount', (count + 1).toString());
    }
  },

  incrementSongCount: async () => {
    const { user } = get();
    
    if (user) {
      // Increment in database
      await supabaseService.incrementSongCount(user.id);
      set((state) => ({
        subscription: {
          ...state.subscription,
          songCount: state.subscription.songCount + 1,
          songsThisPeriod: state.subscription.songsThisPeriod + 1,
        }
      }));
    } else {
      // Increment in localStorage for anonymous users
      const count = parseInt(localStorage.getItem('anonymousSongCount') || '0');
      localStorage.setItem('anonymousSongCount', (count + 1).toString());
    }
  },

  upgradeToPremium: () => {
    // This will be called when Stripe payment succeeds
    // For now, just open Stripe checkout (we'll implement this next)
    set({ showUpgradeModal: true });
  },

  setShowUpgradeModal: (show: boolean) => {
    set({ showUpgradeModal: show });
  },

  setShowSignInModal: (show: boolean) => {
    set({ showSignInModal: show });
  }
}));
