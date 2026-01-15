import { createClient } from '@supabase/supabase-js';

const supabaseUrl = 'https://dvrrgfrclkxoseioywek.supabase.co';
const supabaseAnonKey = 'sb_publishable_TM7FF4cfIMhDnYgxUPEw_Q_h_03zBBs';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

// Database types
export interface UserProfile {
  id: string;
  email: string;
  message_count: number;
  song_count: number;
  is_premium: boolean;
  subscription_start_date: string | null;
  songs_this_period: number;
  period_start_date: string | null;
  created_at: string;
  updated_at: string;
}
