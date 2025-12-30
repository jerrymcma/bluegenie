const handler = require('../sparkifire-web/api/confirm-checkout.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}