const handler = require('../bluegenie-web/api/stripe-webhook.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}