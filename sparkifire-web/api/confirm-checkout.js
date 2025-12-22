const { createClient } = require('@supabase/supabase-js');
const { ensureUserProfile } = require('./_lib/profileHelpers');
const { STRIPE_MODE, getStripeSecretKey } = require('./_lib/stripeConfig');
const stripe = require('stripe')(getStripeSecretKey());

const SUPABASE_URL = process.env.SUPABASE_URL || 'https://dvrrgfrclkxoseioywek.supabase.co';
const SUPABASE_SERVICE_KEY = process.env.SUPABASE_SERVICE_KEY;

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version'
  );

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const body = typeof req.body === 'string' ? JSON.parse(req.body || '{}') : (req.body || {});
    const { sessionId } = body;
    if (!sessionId) {
      return res.status(400).json({ error: 'sessionId is required' });
    }

    const session = await stripe.checkout.sessions.retrieve(sessionId, {
      expand: ['customer', 'line_items'],
    });

    if (session.payment_status !== 'paid') {
      return res.status(400).json({ error: 'Payment not completed' });
    }

    const userId = session.metadata?.userId || session.client_reference_id;
    const customerEmail = session.customer_details?.email || session.customer_email;
    const timestamp = new Date().toISOString();

    const { profile, created } = await ensureUserProfile(supabase, {
      userId,
      email: customerEmail,
      createOverrides: {
        is_premium: true,
        subscription_start_date: timestamp,
        period_start_date: timestamp,
        songs_this_period: 0,
        song_count: 0,
        message_count: 0,
        updated_at: timestamp,
      },
    });

    if (!profile) {
      console.warn('Unable to resolve user profile for completed checkout', {
        userId,
        customerEmail,
      });
      return res.status(500).json({
        error:
          'Payment received but your user profile could not be located automatically. Please contact support so we can finish activating Premium.',
      });
    }

    const { error: updateError } = await supabase
      .from('user_profiles')
      .update({
        is_premium: true,
        subscription_start_date: timestamp,
        period_start_date: timestamp,
        songs_this_period: 0,
        updated_at: timestamp,
      })
      .eq('id', profile.id);

    if (updateError) {
      throw updateError;
    }

    return res.status(200).json({ success: true, profileCreated: created });
  } catch (error) {
    console.error(`Error confirming checkout (${STRIPE_MODE} mode):`, error);
    return res.status(500).json({ error: error.message || 'Internal Server Error' });
  }
};