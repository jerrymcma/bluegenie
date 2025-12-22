const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);
const { createClient } = require('@supabase/supabase-js');

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

    let profile = null;

    if (userId) {
      const { data, error } = await supabase
        .from('user_profiles')
        .select('*')
        .eq('id', userId);
      if (error) {
        throw error;
      }
      profile = data?.[0] || null;
    }

    if (!profile && customerEmail) {
      const { data, error } = await supabase
        .from('user_profiles')
        .select('*')
        .eq('email', customerEmail);
      if (error) {
        throw error;
      }
      profile = data?.[0] || null;
    }

    if (!profile) {
      console.warn('No user profile found for checkout session', {
        userId,
        customerEmail,
      });
      return res.status(500).json({
        error:
          'Payment received but no matching user profile was found. Please contact support with your receipt while we investigate.',
      });
    }

    const now = new Date().toISOString();
    const { error: updateError } = await supabase
      .from('user_profiles')
      .update({
        is_premium: true,
        subscription_start_date: now,
        period_start_date: now,
        songs_this_period: 0,
        updated_at: now,
      })
      .eq('id', profile.id);

    if (updateError) {
      throw updateError;
    }

    return res.status(200).json({ success: true });
  } catch (error) {
    console.error('Error confirming checkout:', error);
    return res.status(500).json({ error: error.message || 'Internal Server Error' });
  }
};