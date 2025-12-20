import { loadStripe, Stripe } from '@stripe/stripe-js';

// Stripe publishable key
const STRIPE_PUBLISHABLE_KEY = 'pk_live_51SgW4wIblGvDq8o6LfcLQvmg9xYUBzRvYsPBBmSM6GlgvoIfEnNZUcNIHfDhIawe4RK6t2aGIIGc9KKenMCTh5Ib002UqdosDt';

let stripePromise: Promise<Stripe | null>;

export const getStripe = () => {
  if (!stripePromise) {
    stripePromise = loadStripe(STRIPE_PUBLISHABLE_KEY);
  }
  return stripePromise;
};

export const PREMIUM_PRICE_ID = 'price_1SgWPiIblGvDq8o6nxsEjYpk'; // Sparki AI Premium - $5/month
export const PAYMENT_LINK_URL = 'https://buy.stripe.com/3cIfZheRtbZKeAb4Mkc7u00'; // Sparki AI Premium payment link
