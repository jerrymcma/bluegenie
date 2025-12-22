import { create } from 'zustand';
import { Message, AIPersonality, MessageType, GeneratedMusic, UserSubscription } from '../types';
import { personalities } from '../data/personalities';
import { geminiService } from '../services/geminiService';
import { storageService } from '../services/storageService';
import { musicService } from '../services/musicService';
import { supabaseService } from '../services/supabaseService';
import { generatedMusicService } from '../services/generatedMusicService';

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
  addMusicToLibrary: (music: GeneratedMusic) => Promise<void>;
  deleteMusicFromLibrary: (id: string) => Promise<void>;
  loadMusicLibrary: () => Promise<void>;
  markMusicAsRead: (id: string) => Promise<void>;
  
  // Subscription actions
  signIn: () => Promise<void>;
  signOut: () => Promise<void>;
  checkUsageLimits: (checkingSongGeneration?: boolean) => boolean;
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
      console.log('[chatStore.initialize] Auth state changed:', { hasUser: !!user, userId: user?.id });
      set({ user });
      if (user) {
        get().loadUserProfile();
        get().loadMusicLibrary(); // Reload music library when user signs in
      }
    });
    
    // Check if user is already signed in
    supabaseService.getCurrentUser().then((user) => {
      console.log('[chatStore.initialize] Checked current user:', { hasUser: !!user, userId: user?.id });
      if (user) {
        set({ user });
        get().loadUserProfile();
        get().loadMusicLibrary(); // Load music library for already signed-in users
      }
    }).catch((error) => {
      console.error('[chatStore.initialize] Error checking current user:', error);
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
    const { isGeneratingMusic, decrementMusicCredits, addMusicToLibrary, user, subscription } = get();

    if (isGeneratingMusic) {
      return null;
    }
    
    // CRITICAL: Check for valid access token FIRST before anything else
    // This is the ultimate gate - if there's no token, nothing happens
    const accessToken = await supabaseService.getAccessToken();
    if (!accessToken) {
      console.warn('[generateMusic] No access token - user must sign in');
      set({
        showSignInModal: true,
      });
      return null;
    }
    
    // CRITICAL: Verify user state is in sync with session
    if (!user) {
      console.warn('[generateMusic] User state not in sync - reloading from session');
      const currentUser = await supabaseService.getCurrentUser();
      if (!currentUser) {
        console.error('[generateMusic] No user found in session');
        set({
          showSignInModal: true,
        });
        return null;
      }
      // User exists, update state
      set({ user: currentUser });
      await get().loadUserProfile();
    }
    
    // Check usage limits for authenticated users
    const { isPremium, songCount } = subscription;
    
    // Free tier users (not premium) can only generate 5 songs
    if (!isPremium && songCount >= 5) {
      console.log('[generateMusic] Free tier user has hit 5 song limit');
      set({
        showUpgradeModal: true,
      });
      return null;
    }
    
    // Premium users need to check renewal
    if (isPremium && subscription.needsRenewal) {
      console.log('[generateMusic] Premium user subscription needs renewal');
      set({
        showUpgradeModal: true,
      });
      return null;
    }

    set({ isGeneratingMusic: true, musicStatus: '✨ Generating your music... ✨' });

    try {
      const result = await musicService.generateClip(payload, accessToken);
      set({ musicStatus: result, isGeneratingMusic: false });
      
      // Extract URL from result and add to library
      const downloadPrefix = 'Download it here: ';
      const downloadIndex = result.indexOf(downloadPrefix);
      if (downloadIndex !== -1) {
        const url = result.substring(downloadIndex + downloadPrefix.length).trim();
        const newMusic: GeneratedMusic = {
          id: crypto.randomUUID(),
          prompt: payload,
          url: url,
          durationSeconds: 58,
          timestamp: Date.now(),
          isFreeTier: true,
          costCents: 0,
          isRead: false,
        };
        addMusicToLibrary(newMusic);
        decrementMusicCredits();
        
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

  addMusicToLibrary: async (music: GeneratedMusic) => {
    const { user } = get();
    if (!user) {
      console.error('[addMusicToLibrary] No user signed in');
      return;
    }

    // Save to database
    const success = await generatedMusicService.addMusic(user.id, music);
    if (success) {
      // Update local state
      const updatedLibrary = [music, ...get().musicLibrary];
      set({ musicLibrary: updatedLibrary });
    } else {
      console.error('[addMusicToLibrary] Failed to save music to database');
    }
  },

  deleteMusicFromLibrary: async (id: string) => {
    const { user } = get();
    if (!user) {
      console.error('[deleteMusicFromLibrary] No user signed in');
      return;
    }

    // Delete from database
    const success = await generatedMusicService.deleteMusic(user.id, id);
    if (success) {
      // Update local state
      const updatedLibrary = get().musicLibrary.filter((m) => m.id !== id);
      set({ musicLibrary: updatedLibrary });
    } else {
      console.error('[deleteMusicFromLibrary] Failed to delete music from database');
    }
  },

  loadMusicLibrary: async () => {
    const { user } = get();
    if (!user) {
      // No user signed in, load from localStorage as fallback for backward compatibility
      try {
        const saved = localStorage.getItem('musicLibrary');
        if (saved) {
          const library = JSON.parse(saved);
          set({ musicLibrary: library });
        }
      } catch (error) {
        console.error('Error loading music library from localStorage:', error);
      }
      return;
    }

    // Load from database for authenticated users
    try {
      const library = await generatedMusicService.getUserMusic(user.id);
      set({ musicLibrary: library });
    } catch (error) {
      console.error('Error loading music library from database:', error);
    }
  },

  markMusicAsRead: async (id: string) => {
    const { user } = get();
    if (!user) {
      console.error('[markMusicAsRead] No user signed in');
      return;
    }

    // Mark as read in database
    const success = await generatedMusicService.markAsRead(user.id, id);
    if (success) {
      // Update local state
      const updatedLibrary = get().musicLibrary.map((m) =>
        m.id === id ? { ...m, isRead: true } : m
      );
      set({ musicLibrary: updatedLibrary });
    } else {
      console.error('[markMusicAsRead] Failed to mark music as read in database');
    }
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
        musicLibrary: [], // Clear music library on sign out
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

  checkUsageLimits: (checkingSongGeneration: boolean = false) => {
    const { user, subscription } = get();
    
    console.log('[checkUsageLimits] Called with:', { checkingSongGeneration, hasUser: !!user, subscription });
    
    // If premium and needs renewal, show upgrade
    if (subscription.isPremium && subscription.needsRenewal) {
      console.log('[checkUsageLimits] Premium needs renewal');
      set({ showUpgradeModal: true });
      return false;
    }
    
    // For song generation specifically, require login
    if (checkingSongGeneration && !user) {
      console.log('[checkUsageLimits] Song generation requires login - showing sign in modal');
      set({ showSignInModal: true });
      return false;
    }
    
    // If signed in but not premium, check if they've used their 5 free songs
    if (checkingSongGeneration && !subscription.isPremium && user) {
      console.log('[checkUsageLimits] Checking song count:', subscription.songCount);
      if (subscription.songCount >= 5) {
        console.log('[checkUsageLimits] Hit 5 song limit - showing upgrade modal');
        set({ showUpgradeModal: true });
        return false;
      }
    }
    
    console.log('[checkUsageLimits] Passed all checks, allowing action');
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
