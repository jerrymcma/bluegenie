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
