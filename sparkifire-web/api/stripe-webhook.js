// Vercel Serverless Function for Stripe Webhook
// This activates premium status when a user successfully pays

const { getStripeSecretKey, getStripeWebhookSecret, STRIPE_MODE } = require('./_lib/stripeConfig');
const { ensureUserProfile } = require('./_lib/profileHelpers');
const { supabaseAdmin, SUPABASE_SERVICE_KEY } = require('./_lib/supabaseAdmin');
const stripe = require('stripe')(getStripeSecretKey());
const { buffer } = require('micro');

// Webhook signing secret from Stripe Dashboard
const endpointSecret = getStripeWebhookSecret();

const stripeWebhookHandler = async (req, res) => {
  // Only allow POST requests
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const sig = req.headers['stripe-signature'];

  if (!sig) {
    console.error('Missing stripe-signature header');
    return res.status(400).json({ error: 'Missing stripe-signature header' });
  }

  if (!endpointSecret) {
    console.error('Missing webhook secret configuration');
    return res.status(500).json({ error: 'Webhook not configured' });
  }

  let event;
  let rawBody;

  try {
    // Get raw body using micro buffer (Vercel's recommended approach)
    rawBody = await buffer(req);
    
    // Verify webhook signature
    event = stripe.webhooks.constructEvent(rawBody, sig, endpointSecret);
  } catch (err) {
    console.error(`Webhook signature verification failed (${STRIPE_MODE} mode):`, err.message);
    console.error('Raw body type:', typeof rawBody, 'Is Buffer:', Buffer.isBuffer(rawBody));
    console.error('Raw body length:', rawBody ? rawBody.length : 'N/A');
    console.error('Signature:', sig ? sig.substring(0, 50) + '...' : 'N/A');
    return res.status(400).json({ error: `Webhook Error: ${err.message}` });
  }

  console.log(`✅ Webhook verified successfully (${STRIPE_MODE} mode):`, event.type, 'Event ID:', event.id);

  // Handle different event types
  try {
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
        console.log(`ℹ️  Unhandled event type: ${event.type}`);
    }
    
    console.log(`✅ Event ${event.id} processed successfully`);
  } catch (handlerError) {
    console.error(`❌ Error processing event ${event.id}:`, handlerError);
    // Still return 200 to acknowledge receipt, but log the error
    return res.status(200).json({ 
      received: true, 
      warning: 'Event received but processing encountered an error' 
    });
  }

  return res.status(200).json({ received: true, eventId: event.id });
};

module.exports = stripeWebhookHandler;

// IMPORTANT: Disable body parsing for Stripe webhooks
// Vercel needs the raw body to verify signatures
module.exports.config = {
  api: {
    bodyParser: false,
  },
};

async function handleCheckoutSessionCompleted(session) {
  console.log('💳 Processing checkout.session.completed:', session.id);
  
  const customerEmail = session.customer_email || session.customer_details?.email;
  const userId = session.metadata?.userId || session.client_reference_id;
  
  console.log('Customer details:', { customerEmail, userId, sessionId: session.id });
  
  if (!customerEmail && !userId) {
    console.error('❌ Unable to resolve customer identity from session payload');
    throw new Error('Missing customer identification');
  }

  const timestamp = new Date().toISOString();
  if (!SUPABASE_SERVICE_KEY) {
    console.error('❌ Stripe webhook missing Supabase service key. User profile will not be auto-created.');
    throw new Error('Missing Supabase configuration');
  }

  try {
    const { profile } = await ensureUserProfile(supabaseAdmin, {
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
      console.error('❌ Stripe webhook could not locate or create user profile', {
        userId,
        customerEmail,
      });
      throw new Error('Failed to create/locate user profile');
    }

    const { error: updateError } = await supabaseAdmin
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
      console.error('❌ Error activating premium from webhook:', updateError);
      throw updateError;
    }

    console.log('✅ Premium activated successfully for user:', profile.id, 'Email:', customerEmail);
  } catch (error) {
    console.error('❌ Error in handleCheckoutSessionCompleted:', error);
    throw error;
  }
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
