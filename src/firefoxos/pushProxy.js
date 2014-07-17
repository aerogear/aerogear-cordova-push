var cordova = require('cordova');

module.exports = {

  register: function (onNotification, success, fail, config) {
    var client = AeroGear.UnifiedPushClient(config.simplePush.variantID, config.simplePush.variantSecret, config.pushServerURL + '/rest/registry/device');

    var req = navigator.push.register();
    req.onsuccess = function (e) {
      config.deviceToken = req.result;
      var settings = {
        success: success,
        error: fail
      };

      settings.metadata = config;
      client.registerWithPushServer(settings);
    };

    navigator.mozSetMessageHandler('push', function (message) {
      onNotification(message);
    });
  }
};

require("cordova/exec/proxy").add("push", module.exports);