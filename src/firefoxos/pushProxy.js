var cordova = require('cordova');
var UnifiedPushPlugin = require("org.jboss.aerogear.cordova.push.AeroGear.UnifiedPush");

module.exports = {
  register: function (onNotification, fail, args) {
    var config = args[0];
    var pushConfig = config.firefoxos || config;
    if (!pushConfig || !pushConfig.variantID || !pushConfig.variantSecret || !config.pushServerURL) {
      throw new Error('Incorrect push plugin configuration: ' + JSON.stringify( pushConfig ));
    }

    var client = AeroGear.UnifiedPushClient(pushConfig.variantID, pushConfig.variantSecret, config.pushServerURL);

    var req = navigator.push.register();
    req.onsuccess = function (e) {
      var settings = {
        success: UnifiedPushPlugin.successCallback,
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

require("cordova/exec/proxy").add("PushPlugin", module.exports);