import { describe, expect, beforeEach, it } from 'vitest';
import type { StoreApi } from 'zustand';
import type { ChatState } from './chatStore';
import { createChatStore } from './chatStore';

const mockWindow = {
  location: {
    hostname: 'localhost',
    origin: 'http://localhost:5173',
  },
};

(globalThis as any).window = mockWindow;

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
      user: { id: 'user-123' } as any,
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
      user: { id: 'user-123' } as any,
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
      user: { id: 'premium-user' } as any,
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