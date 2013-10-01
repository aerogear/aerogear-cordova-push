
    var exec = require("cordova/exec");
    var UnifiedPushClient = function(){};


    // Call this to register for push notifications. Content of [options] depends on whether we are working with APNS (iOS) or GCM (Android)
    UnifiedPushClient.prototype.register = function (successCallback, errorCallback, options) {
        if (errorCallback == null) {
            errorCallback = function () {
            }
        }

        if (typeof errorCallback != "function") {
            console.log("UnifiedPushClient.register failure: failure parameter not a function");
            return;
        }

        if (typeof successCallback != "function") {
            console.log("UnifiedPushClient.register failure: success callback parameter must be a function");
            return;
        }

        cordova.exec(successCallback, errorCallback, "PushPlugin", "register", [options]);
    };

    // Call this to unregister for push notifications
    UnifiedPushClient.prototype.unregister = function (successCallback, errorCallback) {
        if (errorCallback == null) {
            errorCallback = function () {
            }
        }

        if (typeof errorCallback != "function") {
            console.log("UnifiedPushClient.unregister failure: failure parameter not a function");
            return;
        }

        if (typeof successCallback != "function") {
            console.log("UnifiedPushClient.unregister failure: success callback parameter must be a function");
            return;
        }

        cordova.exec(successCallback, errorCallback, "PushPlugin", "unregister", []);
    };


    // Call this to set the application icon badge
    UnifiedPushClient.prototype.setApplicationIconBadgeNumber = function (successCallback, badge) {
        if (errorCallback == null) {
            errorCallback = function () {
            }
        }

        if (typeof errorCallback != "function") {
            console.log("UnifiedPushClient.setApplicationIconBadgeNumber failure: failure parameter not a function");
            return;
        }

        if (typeof successCallback != "function") {
            console.log("UnifiedPushClient.setApplicationIconBadgeNumber failure: success callback parameter must be a function");
            return;
        }

        cordova.exec(successCallback, successCallback, "PushPlugin", "setApplicationIconBadgeNumber", [
            {badge: badge}
        ]);
    };

    module.exports = new UnifiedPushClient();
