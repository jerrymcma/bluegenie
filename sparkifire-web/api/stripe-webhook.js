// Vercel Serverless Function for Stripe Webhook
// This activates premium status when a user successfully pays

const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);
const { createClient } = require('@supabase/supabase-js');

const SUPABASE_URL = process.env.SUPABASE_URL || 'https://dvrrgfrclkxoseioywek.supabase.co';
const SUPABASE_SERVICE_KEY = process.env.SUPABASE_SERVICE_KEY; // Need service key for admin access

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

// Webhook signing secret from Stripe Dashboard
const endpointSecret = process.env.STRIPE_WEBHOOK_SECRET;

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
    console.error('Webhook signature verification failed:', err.message);
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
  
  if (!customerEmail) {
    console.error('No customer email found in session');
    return;
  }

  console.log('Activating premium for email:', customerEmail);

  // Find user by email
  const { data: profiles, error: fetchError } = await supabase
    .from('user_profiles')
    .select('*')
    .eq('email', customerEmail)
    .single();

  if (fetchError || !profiles) {
    console.error('User not found for email:', customerEmail, fetchError);
    return;
  }

  // Activate premium
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
    .eq('id', profiles.id);

  if (updateError) {
    console.error('Error activating premium:', updateError);
    return;
  }

  console.log('✅ Premium activated successfully for user:', profiles.id);
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
