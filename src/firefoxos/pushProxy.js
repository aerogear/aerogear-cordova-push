var cordova = require('cordova');

module.exports = {
  register: function (onNotification, success, fail, config) {
    var pushConfig = config.firefoxos || config;
    var client = AeroGear.UnifiedPushClient(pushConfig.variantID, pushConfig.variantSecret, config.pushServerURL);

    var req = navigator.push.register();
    req.onsuccess = function (e) {
      var settings = {
        success: success,
        error: function (response, error) {
          fail(error);
        }
      };

      var metadata = {
        deviceToken: req.result,
        alias: pushConfig.alias
      };
      settings.metadata = metadata;
      client.registerWithPushServer(settings);
    };

    navigator.mozSetMessageHandler('push', function (message) {
      onNotification(message);
    });
  }
};

require("cordova/exec/proxy").add("push", module.exports);