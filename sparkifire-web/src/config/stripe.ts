import { loadStripe, Stripe } from '@stripe/stripe-js';

const env = typeof import.meta !== 'undefined' ? (import.meta as any)?.env ?? {} : {};
const STRIPE_MODE = (env?.VITE_STRIPE_MODE || 'live').toLowerCase();

const parseNumber = (value: unknown, fallback: number) => {
  if (value === undefined || value === null) return fallback;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
};

const BASE_PRICE_USD = parseNumber(env?.VITE_STRIPE_BASE_PRICE_USD, 5);
const INTRO_PERCENT_OFF = parseNumber(env?.VITE_STRIPE_INTRO_PERCENT_OFF, 80);
const INTRO_DISCOUNT_ENABLED = INTRO_PERCENT_OFF > 0;

const effectivePrice = INTRO_DISCOUNT_ENABLED
  ? BASE_PRICE_USD * (1 - INTRO_PERCENT_OFF / 100)
  : BASE_PRICE_USD;

const roundedEffectivePrice = Math.round(effectivePrice * 100) / 100;
const formatUsd = (amount: number) =>
  amount % 1 === 0 ? `$${amount.toFixed(0)}` : `$${amount.toFixed(2)}`;

export const PREMIUM_PRICE_USD = roundedEffectivePrice;
export const PREMIUM_PRICE_LABEL = formatUsd(roundedEffectivePrice);
export const PREMIUM_BASE_PRICE_USD = BASE_PRICE_USD;

const LIVE_PUBLISHABLE_KEY =
  env?.VITE_STRIPE_LIVE_PUBLISHABLE_KEY ||
  'pk_live_51SgW4wIblGvDq8o6LfcLQvmg9xYUBzRvYsPBBmSM6GlgvoIfEnNZUcNIHfDhIawe4RK6t2aGIIGc9KKenMCTh5Ib002UqdosDt';
const TEST_PUBLISHABLE_KEY =
  env?.VITE_STRIPE_TEST_PUBLISHABLE_KEY || env?.VITE_STRIPE_LIVE_PUBLISHABLE_KEY || LIVE_PUBLISHABLE_KEY;

const STRIPE_PUBLISHABLE_KEY = STRIPE_MODE === 'test' ? TEST_PUBLISHABLE_KEY : LIVE_PUBLISHABLE_KEY;

let stripePromise: Promise<Stripe | null>;

export const getStripe = () => {
  if (!stripePromise) {
    stripePromise = loadStripe(STRIPE_PUBLISHABLE_KEY);
  }
  return stripePromise;
};

export const IS_STRIPE_TEST_MODE = STRIPE_MODE === 'test';

const LIVE_PRICE_ID = env?.VITE_STRIPE_LIVE_PRICE_ID || 'price_1SgWPiIblGvDq8o6nxsEjYpk';
const TEST_PRICE_ID = env?.VITE_STRIPE_TEST_PRICE_ID || LIVE_PRICE_ID;

export const PREMIUM_PRICE_ID = IS_STRIPE_TEST_MODE ? TEST_PRICE_ID : LIVE_PRICE_ID;

const LIVE_PAYMENT_LINK = env?.VITE_STRIPE_LIVE_PAYMENT_LINK || 'https://buy.stripe.com/3cIfZheRtbZKeAb4Mkc7u00';
const TEST_PAYMENT_LINK = env?.VITE_STRIPE_TEST_PAYMENT_LINK || env?.VITE_STRIPE_LIVE_PAYMENT_LINK || LIVE_PAYMENT_LINK;

export const PAYMENT_LINK_URL = IS_STRIPE_TEST_MODE ? TEST_PAYMENT_LINK : LIVE_PAYMENT_LINK;
