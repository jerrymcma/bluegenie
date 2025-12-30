const handler = require('../sparkifire-web/api/stripe-webhook.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}