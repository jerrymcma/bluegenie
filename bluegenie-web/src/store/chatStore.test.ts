import { describe, expect, beforeEach, it } from 'vitest';
import type { StoreApi } from 'zustand';
import type { User } from '@supabase/supabase-js';
import type { ChatState } from './chatStore';
import { createChatStore } from './chatStore';

const mockWindow = {
  location: {
    hostname: 'localhost',
    origin: 'http://localhost:5173',
  },
};

const globalWithWindow = globalThis as typeof globalThis & { window: Window & typeof globalThis };
globalWithWindow.window = mockWindow as unknown as Window & typeof globalThis;

const createMockUser = (id: string): User => ({ id } as unknown as User);

describe('chatStore usage limits', () => {
  let store: StoreApi<ChatState>;

  beforeEach(() => {
    store = createChatStore();
  });

  it('requires sign in before generating songs', () => {
    const allowed = store.getState().checkUsageLimits(true);

    expect(allowed).toBe(false);
    expect(store.getState().showSignInModal).toBe(true);
  });

  it('allows non-premium users below 5 songs to generate music', () => {
    const baseSubscription = store.getState().subscription;
    store.setState({
      user: createMockUser('user-123'),
      showSignInModal: false,
      subscription: {
        ...baseSubscription,
        isPremium: false,
        songCount: 4,
      },
    });

    const allowed = store.getState().checkUsageLimits(true);

    expect(allowed).toBe(true);
    expect(store.getState().showUpgradeModal).toBe(false);
  });

  it('blocks non-premium users once they reach five songs', () => {
    const baseSubscription = store.getState().subscription;
    store.setState({
      user: createMockUser('user-123'),
      showSignInModal: false,
      showUpgradeModal: false,
      subscription: {
        ...baseSubscription,
        isPremium: false,
        songCount: '5' as unknown as number,
      },
    });

    const allowed = store.getState().checkUsageLimits(true);

    expect(allowed).toBe(false);
    expect(store.getState().showUpgradeModal).toBe(true);
  });

  it('blocks premium users whose subscription needs renewal', () => {
    const baseSubscription = store.getState().subscription;
    store.setState({
      user: createMockUser('premium-user'),
      showUpgradeModal: false,
      subscription: {
        ...baseSubscription,
        isPremium: true,
        needsRenewal: true,
      },
    });

    const allowed = store.getState().checkUsageLimits(true);

    expect(allowed).toBe(false);
    expect(store.getState().showUpgradeModal).toBe(true);
  });
});