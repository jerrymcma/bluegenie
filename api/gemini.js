const handler = require('../sparkifire-web/api/gemini.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}