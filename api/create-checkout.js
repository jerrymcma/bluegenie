const handler = require('../bluegenie-web/api/create-checkout.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}