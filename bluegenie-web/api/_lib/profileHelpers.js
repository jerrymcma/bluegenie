const normalizeEmail = (email) => (typeof email === 'string' ? email.trim().toLowerCase() : '');

async function fetchProfileById(supabase, userId) {
  if (!userId) return null;

  const { data, error } = await supabase
    .from('user_profiles')
    .select('*')
    .eq('id', userId)
    .maybeSingle();

  if (error && error.code !== 'PGRST116') {
    throw error;
  }

  return data || null;
}

async function fetchProfileByEmail(supabase, email) {
  const normalizedEmail = normalizeEmail(email);
  if (!normalizedEmail) return null;

  const { data, error } = await supabase
    .from('user_profiles')
    .select('*')
    .ilike('email', normalizedEmail)
    .maybeSingle();

  if (error && error.code !== 'PGRST116') {
    throw error;
  }

  return data || null;
}

async function getAuthUserIdFromEmail(supabase, email) {
  const normalizedEmail = normalizeEmail(email);
  if (!normalizedEmail) return null;

  try {
    const { data, error } = await supabase.auth.admin.getUserByEmail(normalizedEmail);
    if (error) {
      console.error('[profileHelpers] Failed to fetch auth user by email:', error);
      return null;
    }

    const supabaseUser = data?.user || data || null;
    return supabaseUser?.id || null;
  } catch (error) {
    console.error('[profileHelpers] Unexpected admin error while fetching user by email:', error);
    return null;
  }
}

async function createProfile(supabase, userId, email, overrides = {}) {
  if (!userId || !email) {
    return null;
  }

  const now = new Date().toISOString();
  const payload = {
    id: userId,
    email: normalizeEmail(email),
    is_premium: false,
    message_count: 0,
    song_count: 0,
    songs_this_period: 0,
    subscription_start_date: null,
    period_start_date: null,
    created_at: now,
    updated_at: now,
    ...overrides,
  };

  const { data, error } = await supabase
    .from('user_profiles')
    .insert([payload])
    .select()
    .single();

  if (error) {
    console.error('[profileHelpers] Failed to create user profile:', error);
    throw error;
  }

  return data;
}

/**
 * Ensures a user profile exists for the provided identifiers.
 * Attempts lookup by userId, then email (case-insensitive). If not found,
 * tries to resolve the Supabase Auth user via email and creates the profile.
 */
async function ensureUserProfile(supabase, { userId, email, createOverrides = {} }) {
  const normalizedEmail = normalizeEmail(email);

  let profile = await fetchProfileById(supabase, userId);
  if (profile) {
    return { profile, created: false };
  }

  profile = await fetchProfileByEmail(supabase, normalizedEmail);
  if (profile) {
    return { profile, created: false };
  }

  const resolvedUserId = userId || (await getAuthUserIdFromEmail(supabase, normalizedEmail));
  if (!resolvedUserId || !normalizedEmail) {
    return { profile: null, created: false };
  }

  const createdProfile = await createProfile(supabase, resolvedUserId, normalizedEmail, createOverrides);
  return { profile: createdProfile, created: true };
}

module.exports = {
  normalizeEmail,
  ensureUserProfile,
};