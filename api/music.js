const handler = require('../sparkifire-web/api/music.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}