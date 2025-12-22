// Quick script to activate premium for a user
import { createClient } from '@supabase/supabase-js';

const supabaseUrl = 'https://dvrrgfrclkxoseioywek.supabase.co';
const supabaseKey = 'sb_publishable_TM7FF4cfIMhDnYgxUPEw_Q_h_03zBBs';

const supabase = createClient(supabaseUrl, supabaseKey);

async function activatePremium(email) {
  console.log(`Activating premium for ${email}...`);
  
  // First, let's see all users
  const { data: allProfiles, error: allError } = await supabase
    .from('user_profiles')
    .select('*');
  
  if (allError) {
    console.error('Error fetching all users:', allError);
    return;
  }
  
  console.log('All users in database:', allProfiles);
  
  // Get user by email
  const { data: profiles, error: fetchError } = await supabase
    .from('user_profiles')
    .select('*')
    .eq('email', email);
  
  if (fetchError) {
    console.error('Error fetching user:', fetchError);
    return;
  }
  
  if (!profiles || profiles.length === 0) {
    console.error('User not found with email:', email);
    return;
  }
  
  const profile = profiles[0];
  console.log('Found user:', profile.id);
  
  // Activate premium
  const now = new Date().toISOString();
  const { data, error: updateError } = await supabase
    .from('user_profiles')
    .update({
      is_premium: true,
      subscription_start_date: now,
      period_start_date: now,
      songs_this_period: 0,
      updated_at: now,
    })
    .eq('id', profile.id)
    .select();
  
  if (updateError) {
    console.error('Error updating user:', updateError);
    return;
  }
  
  console.log('✅ Premium activated successfully!');
  console.log('User now has:');
  console.log('- is_premium: true');
  console.log('- 50 songs per month');
  console.log('- Period started:', now);
}

activatePremium('jerrymcma@gmail.com');
