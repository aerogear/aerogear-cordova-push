var unifiedPushPlugin = require("aerogear-cordova-push.AeroGear.UnifiedPush");

function parseQuery(qstr) {
  var query = {}, i, pair,
      pairs = qstr.substring(qstr.indexOf('?') + 1).split('&');
  for (i = 0; i < pairs.length; i++) {
    pair = pairs[i].split('=');
    query[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1] || '');
  }

  return query;
}

module.exports = {
  register: function(onNotification, fail, args) {
    var config = (args || [])[0];
    if (!config || !config.pushServerURL || !config.windows || !config.windows.variantID || !config.windows.variantSecret) {
      throw new Error("Incorrect push plugin configuration: " + JSON.stringify(config));
    }

    var client = AeroGear.UnifiedPushClient(config.windows.variantID, config.windows.variantSecret, config.pushServerURL),
        pushNotifications = Windows.Networking.PushNotifications,
        channelOperation = pushNotifications.PushNotificationChannelManager.createPushNotificationChannelForApplicationAsync(),
        channelUri;

    return channelOperation.then(function(newChannel) {
        channelUri = (newChannel || {}).uri;
        var token = config.windows.variantID + channelUri,
            localSettings = Windows.Storage.ApplicationData.current.localSettings;
        if (token !== localSettings.values['channel']) {

          var settings = {
            metadata: {
              alias: config.alias,
              categories: config.categories,
              deviceToken: channelUri
            }
          };

          client.registerWithPushServer(settings).then(unifiedPushPlugin.successCallback).catch(fail);
          localSettings.values['channel'] = token;
        } else {
            unifiedPushPlugin.successCallback();
        }

        newChannel.addEventListener("pushnotificationreceived", function(event) {
          onNotification({
            alert: event.toastNotification.content.innerText,
            data: parseQuery(event.toastNotification.content.getElementsByTagName('toast')[0].attributes.getNamedItem('launch').innerText)
          }, { keepCallback: true });
        });
      },
      function(error) {
        fail(error);
      }
    );
  }
};

require("cordova/exec/proxy").add("PushPlugin", module.exports);
