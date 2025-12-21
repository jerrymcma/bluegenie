import { supabase } from '../config/supabase';
import type { UserProfile } from '../config/supabase';

class SupabaseService {
  // Sign in with Google
  async signInWithGoogle() {
    // Ensure we have a valid redirect URL
    const redirectUrl = window.location.origin;
    console.log('[signInWithGoogle] Using redirect URL:', redirectUrl);
    
    const { data, error } = await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: redirectUrl,
        skipBrowserRedirect: false, // Explicitly redirect after successful auth
      },
    });
    
    if (error) {
      console.error('Error signing in with Google:', error);
      throw error;
    }
    
    console.log('[signInWithGoogle] OAuth initiated successfully');
    return data;
  }

  // Sign out
  async signOut() {
    const { error } = await supabase.auth.signOut();
    if (error) {
      console.error('Error signing out:', error);
      throw error;
    }
  }

  // Get current user
  async getCurrentUser() {
    const { data: { user } } = await supabase.auth.getUser();
    return user;
  }

  // Get access token for authenticated requests
  async getAccessToken(): Promise<string | null> {
    try {
      const { data, error } = await supabase.auth.getSession();
      if (error) {
        console.error('Error getting Supabase session:', error);
        return null;
      }
      return data.session?.access_token ?? null;
    } catch (error) {
      console.error('Unexpected error getting Supabase access token:', error);
      return null;
    }
  }

  // Get or create user profile
  async getUserProfile(userId: string, email: string): Promise<UserProfile | null> {
    // Try to get existing profile
    const { data: existingProfile, error: fetchError } = await supabase
      .from('user_profiles')
      .select('*')
      .eq('id', userId)
      .single();

    if (existingProfile) {
      return existingProfile as UserProfile;
    }

    // Create new profile if doesn't exist
    if (fetchError && fetchError.code === 'PGRST116') {
      const newProfile: Partial<UserProfile> = {
        id: userId,
        email: email,
        message_count: 0,
        song_count: 0,
        is_premium: false,
        subscription_start_date: null,
        songs_this_period: 0,
        period_start_date: null,
      };

      const { data: createdProfile, error: createError } = await supabase
        .from('user_profiles')
        .insert([newProfile])
        .select()
        .single();

      if (createError) {
        console.error('Error creating user profile:', createError);
        return null;
      }

      return createdProfile as UserProfile;
    }

    console.error('Error fetching user profile:', fetchError);
    return null;
  }

  // Increment message count
  async incrementMessageCount(userId: string) {
    const { error } = await supabase.rpc('increment_message_count', {
      user_id: userId,
    });

    if (error) {
      console.error('Error incrementing message count:', error);
    }
  }

  // Increment song count
  async incrementSongCount(userId: string) {
    const { error } = await supabase.rpc('increment_song_count', {
      user_id: userId,
    });

    if (error) {
      console.error('Error incrementing song count:', error);
    }
  }

  // Activate premium subscription
  async activatePremium(userId: string) {
    const now = new Date().toISOString();
    
    const { error } = await supabase
      .from('user_profiles')
      .update({
        is_premium: true,
        subscription_start_date: now,
        period_start_date: now,
        songs_this_period: 0,
        updated_at: now,
      })
      .eq('id', userId);

    if (error) {
      console.error('Error activating premium:', error);
      throw error;
    }
  }

  // Check if subscription needs renewal
  async checkSubscriptionRenewal(profile: UserProfile): Promise<boolean> {
    if (!profile.is_premium) return false;

    const periodStart = profile.period_start_date ? new Date(profile.period_start_date) : null;
    if (!periodStart) return true; // Needs renewal if no period start

    const now = new Date();
    const daysSinceStart = (now.getTime() - periodStart.getTime()) / (1000 * 60 * 60 * 24);

    // Renewal needed if: 30 days passed OR 50 songs used
    return daysSinceStart >= 30 || profile.songs_this_period >= 50;
  }

  // Renew subscription
  async renewSubscription(userId: string) {
    const now = new Date().toISOString();
    
    const { error } = await supabase
      .from('user_profiles')
      .update({
        period_start_date: now,
        songs_this_period: 0,
        updated_at: now,
      })
      .eq('id', userId);

    if (error) {
      console.error('Error renewing subscription:', error);
      throw error;
    }
  }

  // Listen to auth state changes
  onAuthStateChange(callback: (user: any) => void) {
    return supabase.auth.onAuthStateChange((event, session) => {
      callback(session?.user || null);
    });
  }
}

export const supabaseService = new SupabaseService();
