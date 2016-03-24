var cordova = require('cordova');
var UnifiedPushPlugin = require("org.jboss.aerogear.cordova.push.AeroGear.UnifiedPush");

module.exports = {
  register: function (onNotification, fail, args) {
    var config = args[0];
    if (!config || !config.pushServerURL || !config.firefoxos || !config.firefoxos.variantID || !config.firefoxos.variantSecret) {
      throw new Error('Incorrect push plugin configuration: ' + JSON.stringify( config ));
    }

    var client = AeroGear.UnifiedPushClient(config.firefoxos.variantID, config.firefoxos.variantSecret, config.pushServerURL);

    var registrations = navigator.push.registrations();
    registrations.onerror = fail;
    registrations.onsuccess = function(e) {
      if (registrations.result.length > 1) {
        // clean registrations if there are more than one
        for (var i = 1, l = registrations.result.length; i < l; i++) {
          var pushEndpoint = registrations.result[i].pushEndpoint;
          navigator.push.unregister(pushEndpoint);
          client.unregisterWithPushServer(pushEndpoint);
        }
      }

      var settings = {
        metadata: {
          categories: config.categories,
          alias: config.alias
        }
      };

      if (registrations.result.length > 0) {
        // use existing registration
        settings.metadata.deviceToken = registrations.result[0].pushEndpoint;
        client.registerWithPushServer(settings).then(UnifiedPushPlugin.successCallback).catch(fail);
      } else {
        // new registration
        var register = navigator.push.register();
        register.onerror = fail;
        register.onsuccess = function () {
          settings.metadata.deviceToken = register.result;
          client.registerWithPushServer(settings);
        };
      }
    };

    navigator.mozSetMessageHandler('push', function (message) {
      onNotification(message);
    });
  }
};

require("cordova/exec/proxy").add("PushPlugin", module.exports);