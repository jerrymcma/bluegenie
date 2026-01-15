const handler = require('../bluegenie-web/api/confirm-checkout.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}