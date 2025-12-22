import { supabase } from '../config/supabase';
import type { GeneratedMusic } from '../types';

class GeneratedMusicService {
  // Get all music for the current user
  async getUserMusic(userId: string): Promise<GeneratedMusic[]> {
    try {
      const { data, error } = await supabase
        .from('generated_music')
        .select('*')
        .eq('user_id', userId)
        .order('timestamp', { ascending: false });

      if (error) {
        console.error('Error fetching user music:', error);
        return [];
      }

      return data.map(this.mapToGeneratedMusic);
    } catch (error) {
      console.error('Error in getUserMusic:', error);
      return [];
    }
  }

  // Add new music to user's library
  async addMusic(userId: string, music: Omit<GeneratedMusic, 'id'>): Promise<boolean> {
    try {
      const { error } = await supabase
        .from('generated_music')
        .insert([{
          user_id: userId,
          prompt: music.prompt,
          url: music.url,
          duration_seconds: music.durationSeconds,
          timestamp: music.timestamp,
          is_free_tier: music.isFreeTier,
          cost_cents: music.costCents,
          is_read: music.isRead,
        }]);

      if (error) {
        console.error('Error adding music:', error);
        return false;
      }

      return true;
    } catch (error) {
      console.error('Error in addMusic:', error);
      return false;
    }
  }

  // Mark music as read
  async markAsRead(userId: string, musicId: string): Promise<boolean> {
    try {
      const { error } = await supabase
        .from('generated_music')
        .update({ is_read: true })
        .eq('id', musicId)
        .eq('user_id', userId);

      if (error) {
        console.error('Error marking music as read:', error);
        return false;
      }

      return true;
    } catch (error) {
      console.error('Error in markAsRead:', error);
      return false;
    }
  }

  // Delete music from library
  async deleteMusic(userId: string, musicId: string): Promise<boolean> {
    try {
      const { error } = await supabase
        .from('generated_music')
        .delete()
        .eq('id', musicId)
        .eq('user_id', userId);

      if (error) {
        console.error('Error deleting music:', error);
        return false;
      }

      return true;
    } catch (error) {
      console.error('Error in deleteMusic:', error);
      return false;
    }
  }

  // Helper to map database row to GeneratedMusic type
  private mapToGeneratedMusic(row: any): GeneratedMusic {
    return {
      id: row.id,
      prompt: row.prompt,
      url: row.url,
      durationSeconds: row.duration_seconds,
      timestamp: row.timestamp,
      isFreeTier: row.is_free_tier,
      costCents: row.cost_cents,
      isRead: row.is_read,
    };
  }
}

export const generatedMusicService = new GeneratedMusicService();
