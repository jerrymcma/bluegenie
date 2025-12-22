// Vercel Serverless Function for Stripe Checkout
const { STRIPE_MODE, getStripeSecretKey, getPriceId, getIntroDiscountConfig } = require('./_lib/stripeConfig');
const stripe = require('stripe')(getStripeSecretKey());

const introDiscount = getIntroDiscountConfig();

async function getIntroCoupon() {
  if (!introDiscount.enabled) {
    return null;
  }

  try {
    return await stripe.coupons.retrieve(introDiscount.couponId);
  } catch (error) {
    if (error?.code !== 'resource_missing') {
      console.error('Failed to retrieve intro coupon:', error);
      throw error;
    }
  }

  try {
    return await stripe.coupons.create({
      id: introDiscount.couponId,
      percent_off: introDiscount.percentOff,
      duration: 'forever',
      name: 'Sparki Intro Premium',
    });
  } catch (error) {
    if (error?.code === 'resource_already_exists') {
      return await stripe.coupons.retrieve(introDiscount.couponId);
    }
    console.error('Failed to create intro coupon:', error);
    throw error;
  }
}

module.exports = async (req, res) => {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const body = typeof req.body === 'string' ? JSON.parse(req.body || '{}') : (req.body || {});
    const { priceId: clientPriceId, customerEmail, userId } = body;
    const resolvedPriceId = getPriceId() || clientPriceId;

    if (!resolvedPriceId) {
      return res.status(400).json({ error: 'Price ID is not configured for this environment' });
    }

    let coupon = null;
    try {
      coupon = await getIntroCoupon();
    } catch (error) {
      console.error('Intro coupon error (continuing without discount):', error);
    }

    const sessionPayload = {
      mode: 'subscription',
      payment_method_types: ['card'],
      line_items: [
        {
          price: resolvedPriceId,
          quantity: 1,
        },
      ],
      customer_email: customerEmail,
      client_reference_id: userId, // Store user ID to activate premium after payment
      success_url: `${req.headers.origin || 'https://sparkiai.app'}?session_id={CHECKOUT_SESSION_ID}&success=true`,
      cancel_url: `${req.headers.origin || 'https://sparkiai.app'}?canceled=true`,
      metadata: {
        userId,
        stripeMode: STRIPE_MODE,
      },
    };

    if (coupon) {
      sessionPayload.discounts = [{ coupon: coupon.id }];
    }

    // Create Stripe Checkout Session
    const session = await stripe.checkout.sessions.create(sessionPayload);

    return res.status(200).json({
      sessionId: session.id,
      url: session.url,
      stripeMode: STRIPE_MODE,
    });
  } catch (error) {
    console.error('Error creating checkout session:', error);
    return res.status(500).json({ error: error.message });
  }
};
