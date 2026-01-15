const handler = require('../bluegenie-web/api/gemini.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}