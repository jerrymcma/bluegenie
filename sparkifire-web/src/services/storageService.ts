import { Message } from '../types';

const STORAGE_KEY_PREFIX = 'sparkifire_messages_';
const LAST_RESET_KEY_PREFIX = 'sparkifire_last_reset_';
const AUTO_RESET_HOURS = 24;

export class StorageService {
  saveMessages(personalityId: string, messages: Message[]): void {
    try {
      const key = STORAGE_KEY_PREFIX + personalityId;
      localStorage.setItem(key, JSON.stringify(messages));
    } catch (error) {
      console.error('Error saving messages:', error);
    }
  }

  loadMessages(personalityId: string): Message[] {
    try {
      const key = STORAGE_KEY_PREFIX + personalityId;
      const data = localStorage.getItem(key);
      if (data) {
        return JSON.parse(data);
      }
    } catch (error) {
      console.error('Error loading messages:', error);
    }
    return [];
  }

  clearMessages(personalityId: string): void {
    try {
      const key = STORAGE_KEY_PREFIX + personalityId;
      localStorage.removeItem(key);
      this.updateLastResetTime(personalityId);
    } catch (error) {
      console.error('Error clearing messages:', error);
    }
  }

  clearAllMessages(): void {
    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith(STORAGE_KEY_PREFIX)) {
          localStorage.removeItem(key);
        }
      });
    } catch (error) {
      console.error('Error clearing all messages:', error);
    }
  }

  hasHistory(personalityId: string): boolean {
    const messages = this.loadMessages(personalityId);
    return messages.length > 0;
  }

  getMessageCount(personalityId: string): number {
    const messages = this.loadMessages(personalityId);
    return messages.length;
  }

  shouldAutoReset(personalityId: string): boolean {
    try {
      const key = LAST_RESET_KEY_PREFIX + personalityId;
      const lastResetStr = localStorage.getItem(key);
      
      if (!lastResetStr) {
        return false;
      }

      const lastReset = new Date(lastResetStr);
      const now = new Date();
      const hoursSinceReset = (now.getTime() - lastReset.getTime()) / (1000 * 60 * 60);

      return hoursSinceReset >= AUTO_RESET_HOURS;
    } catch (error) {
      console.error('Error checking auto reset:', error);
      return false;
    }
  }

  private updateLastResetTime(personalityId: string): void {
    try {
      const key = LAST_RESET_KEY_PREFIX + personalityId;
      localStorage.setItem(key, new Date().toISOString());
    } catch (error) {
      console.error('Error updating last reset time:', error);
    }
  }

  getConversationContext(personalityId: string, limit: number = 10): Array<{ role: 'user' | 'assistant', content: string }> {
    const messages = this.loadMessages(personalityId);
    const recentMessages = messages.slice(-limit * 2); // Get last N pairs (user + assistant)
    
    return recentMessages.map(msg => ({
      role: msg.isFromUser ? 'user' as const : 'assistant' as const,
      content: msg.content
    }));
  }
}

export const storageService = new StorageService();
