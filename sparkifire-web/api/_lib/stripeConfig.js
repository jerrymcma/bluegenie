const STRIPE_MODE = (process.env.STRIPE_MODE || process.env.VITE_STRIPE_MODE || 'live').toLowerCase();

const parsePercent = (value) => {
  if (value === undefined || value === null) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
};

const getStripeSecretKey = () => {
  if (STRIPE_MODE === 'test') {
    return process.env.STRIPE_TEST_SECRET_KEY || process.env.STRIPE_SECRET_KEY;
  }
  return process.env.STRIPE_SECRET_KEY || process.env.STRIPE_LIVE_SECRET_KEY;
};

const getStripeWebhookSecret = () => {
  if (STRIPE_MODE === 'test') {
    return process.env.STRIPE_TEST_WEBHOOK_SECRET || process.env.STRIPE_WEBHOOK_SECRET;
  }
  return process.env.STRIPE_WEBHOOK_SECRET || process.env.STRIPE_LIVE_WEBHOOK_SECRET;
};

const getPriceId = () => {
  if (STRIPE_MODE === 'test') {
    return process.env.STRIPE_TEST_PRICE_ID || process.env.STRIPE_PRICE_ID || null;
  }
  return process.env.STRIPE_PRICE_ID || process.env.STRIPE_LIVE_PRICE_ID || null;
};

const getIntroDiscountConfig = () => {
  const percent =
    parsePercent(process.env.STRIPE_INTRO_PERCENT_OFF) ??
    parsePercent(process.env.VITE_STRIPE_INTRO_PERCENT_OFF) ??
    parsePercent(process.env.INTRO_PERCENT_OFF) ??
    0;
  const enabled = percent > 0;
  const couponId = process.env.STRIPE_INTRO_COUPON_ID || 'sparki_intro_one_dollar';

  return {
    enabled,
    percentOff: enabled ? percent : 0,
    couponId,
  };
};

const getModeSummary = () => ({
  stripeMode: STRIPE_MODE,
  priceId: getPriceId(),
  hasWebhookSecret: !!getStripeWebhookSecret(),
});

module.exports = {
  STRIPE_MODE,
  getStripeSecretKey,
  getStripeWebhookSecret,
  getPriceId,
  getIntroDiscountConfig,
  getModeSummary,
};