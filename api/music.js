const handler = require('../bluegenie-web/api/music.js');

module.exports = handler;

if (handler && handler.config) {
  module.exports.config = handler.config;
}