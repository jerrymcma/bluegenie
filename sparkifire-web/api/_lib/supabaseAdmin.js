const { createClient } = require('@supabase/supabase-js');

const SUPABASE_URL = process.env.SUPABASE_URL || 'https://dvrrgfrclkxoseioywek.supabase.co';

const serviceKey =
  process.env.SUPABASE_SERVICE_KEY ||
  process.env.SUPABASE_SERVICE_ROLE_KEY ||
  process.env.SUPABASE_SERVICE_ROLE ||
  process.env.SUPABASE_ADMIN_KEY ||
  process.env.SUPABASE_SERVICE_ROLE_SECRET ||
  null;

if (!serviceKey) {
  console.warn(
    '[supabaseAdmin] Missing Supabase service role key. Set SUPABASE_SERVICE_KEY (or SUPABASE_SERVICE_ROLE_KEY) in Vercel env to allow server functions to bypass RLS.'
  );
}

const supabaseAdmin = createClient(SUPABASE_URL, serviceKey, {
  auth: {
    autoRefreshToken: false,
    persistSession: false,
  },
});

module.exports = {
  supabaseAdmin,
  SUPABASE_URL,
  SUPABASE_SERVICE_KEY: serviceKey,
};