// Vercel Serverless Function for Stripe Webhook
// This activates premium status when a user successfully pays

const { createClient } = require('@supabase/supabase-js');
const { getStripeSecretKey, getStripeWebhookSecret, STRIPE_MODE } = require('./_lib/stripeConfig');
const { ensureUserProfile } = require('./_lib/profileHelpers');
const stripe = require('stripe')(getStripeSecretKey());

const SUPABASE_URL = process.env.SUPABASE_URL || 'https://dvrrgfrclkxoseioywek.supabase.co';
const SUPABASE_SERVICE_KEY = process.env.SUPABASE_SERVICE_KEY; // Need service key for admin access

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

// Webhook signing secret from Stripe Dashboard
const endpointSecret = getStripeWebhookSecret();

module.exports = async (req, res) => {
  // Only allow POST requests
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const sig = req.headers['stripe-signature'];

  let event;

  try {
    // Verify webhook signature
    event = stripe.webhooks.constructEvent(req.body, sig, endpointSecret);
  } catch (err) {
    console.error(`Webhook signature verification failed (${STRIPE_MODE} mode):`, err.message);
    return res.status(400).json({ error: `Webhook Error: ${err.message}` });
  }

  console.log('Received Stripe event:', event.type);

  // Handle different event types
  switch (event.type) {
    case 'checkout.session.completed':
      await handleCheckoutSessionCompleted(event.data.object);
      break;
    
    case 'customer.subscription.created':
    case 'customer.subscription.updated':
      await handleSubscriptionUpdate(event.data.object);
      break;
    
    case 'customer.subscription.deleted':
      await handleSubscriptionDeleted(event.data.object);
      break;

    default:
      console.log(`Unhandled event type: ${event.type}`);
  }

  return res.status(200).json({ received: true });
};

async function handleCheckoutSessionCompleted(session) {
  console.log('Checkout session completed:', session.id);
  
  const customerEmail = session.customer_email || session.customer_details?.email;
  const userId = session.metadata?.userId || session.client_reference_id;
  
  if (!customerEmail && !userId) {
    console.error('Unable to resolve customer identity from session payload');
    return;
  }

  const timestamp = new Date().toISOString();
  const { profile } = await ensureUserProfile(supabase, {
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
    console.error('Stripe webhook could not locate or create user profile', {
      userId,
      customerEmail,
    });
    return;
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
    console.error('Error activating premium from webhook:', updateError);
    return;
  }

  console.log('✅ Premium activated successfully for user:', profile.id);
}

async function handleSubscriptionUpdate(subscription) {
  console.log('Subscription updated:', subscription.id);
  // Handle subscription updates if needed
}

async function handleSubscriptionDeleted(subscription) {
  console.log('Subscription deleted:', subscription.id);
  // Handle subscription cancellation if needed
  // You might want to deactivate premium here
}
