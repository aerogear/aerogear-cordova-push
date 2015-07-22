var unifiedPushPlugin = require("org.jboss.aerogear.cordova.push.AeroGear.UnifiedPush");

function parseQuery(qstr) {
  var query = {};
  var part = qstr.substring(qstr.indexOf('?') + 1).split('&');
  for (var i = 0; i < part.length; i++) {
    var b = part[i].split('=');
    query[decodeURIComponent(b[0])] = decodeURIComponent(b[1]);
  }

  return query;
}

module.exports = {
  register: function(onNotification, fail, args) {
    var config = args[0];
    if (!config || !config.pushServerURL || !config.windows || !config.windows.variantID || !config.windows.variantSecret) {
      throw new Error("Incorrect push plugin configuration: " + JSON.stringify( config ));
    }

    var client = AeroGear.UnifiedPushClient(config.windows.variantID, config.windows.variantSecret, config.pushServerURL);

    var channelUri;
    var pushNotifications = Windows.Networking.PushNotifications;
    var channelOperation = pushNotifications.PushNotificationChannelManager.createPushNotificationChannelForApplicationAsync();

    return channelOperation.then(function(newChannel) {
        channelUri = newChannel.uri;
        var token = config.windows.variantID + channelUri;
        var localSettings = Windows.Storage.ApplicationData.current.localSettings;
        if (token !== localSettings.values['channel']) {

          var settings = {
            success: unifiedPushPlugin.successCallback,
            error: function (response, error) {
              fail(error);
            },
            metadata: {
              alias: config.alias,
              deviceToken: channelUri
            }
          };

          client.registerWithPushServer(settings);
          localSettings.values['channel'] = token;
        }

        newChannel.addEventListener("pushnotificationreceived", function(event) {
          onNotification({
            alert: event.toastNotification.content.innerText,
            data: parseQuery(event.toastNotification.content.getElementsByTagName('toast')[0].attributes.getNamedItem('launch').innerText)
          });
        });
      },
      function(error) {
        fail(error);
      }
    );
  }
};

require("cordova/exec/proxy").add("PushPlugin", module.exports);