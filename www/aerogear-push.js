/* AeroGear Cordova Plugin
* https://github.com/aerogear/aerogear-pushplugin-cordova
* JBoss, Home of Professional Open Source
* Copyright Red Hat, Inc., and individual contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

var exec = require("cordova/exec");
var ajax = require("./ajax");

/**
 * This is a global variable called push exposed by cordova
 * @status Stable
 * @constructs Push
 */
function Push(){
    /**
     * When using 'content-available' on iOS use this enum for the result see {@link Push#setContentAvailable}
     * @readonly
     * @enum {number}
     */
    this.FetchResult = {
        NewData: 0,
        NoData: 1,
        Failed: 2
    }
}

function notify() {
    if (push.successCallback) {
        push.successCallback();
    }
}

var channel = require('cordova/channel');

channel.onCordovaReady.subscribe(function() {
    exec(notify, undefined, 'PushPlugin', 'messageChannel', []);
});

/**
    Registers the device with the APNS (iOS) or GCM (Android) and the Unified Push server.
    @param {Function} onNotification - callback to be executed if a message arrives
    @param {Function} successCallback - callback to be executed when register is succesful
    @param {Function} [errorCallback] - callback to be executed if the request results in error
    @param {Object} [options] - A configuration for the Unified Push server, so that it can register this device. This is optional a when a file called push-config.json is put in the `www` folder this will be used. The object can have the following properties:
    @param {String} [options.senderId] - android specific - the id representing the Google project ID
    @param {String} options.variantID - the id representing the mobile application variant
    @param {String} options.variantSecret - the secret for the mobile application variant
    @param {String} options.pushServerURL - the location of the UnifiedPush server e.g. http(s)//host:port/context
    @param {String} [options.alias] - Application specific alias to identify users with the system. Common use case would be an email address or a username.
    @param {Object} [options.ios] - Holder of the ios specific values variantID and variantSecret
    @param {String} [options.ios.variantID] - the id representing the mobile application variant for iOS
    @param {String} [options.ios.variantSecret] - the secret for the mobile application variant for iOS
    @param {Object} [options.android] - Holder of the android specific values variantID and variantSecret
    @param {String} [options.android.variantID] - the id representing the mobile application variant for android
    @param {String} [options.android.variantSecret] - the secret for the mobile application variant for android
    @returns {void}
    @example

    var pushConfig = {
        pushServerURL: "<pushServerURL e.g http(s)//host:port/context >",
        senderID: "<senderID e.g Google Project ID>",
        variantID: "<variantID e.g. 1234456-234320>",
        variantSecret: "<variantSecret e.g. 1234456-234320>"
        alias: "<alias e.g. a username or an email address optional>",
    }

    push.register(onNotification, successHandler, errorHandler, pushConfig);

    @example
    //combined android and ios configuration
    var pushConfig = {
        pushServerURL: "<pushServerURL e.g http(s)//host:port/context >",
        alias: "<alias e.g. a username or an email address optional>",
        android: {
          senderID: "<senderID e.g Google Project ID>",
          variantID: "<variantID e.g. 1234456-234320>",
          variantSecret: "<variantSecret e.g. 1234456-234320>"
        },
        ios: {
          variantID: "<variantID e.g. 1234456-234320>",
          variantSecret: "<variantSecret e.g. 1234456-234320>"
        }
    };

    push.register(onNotification, successHandler, errorHandler, pushConfig);

    @example
    //push-config.json file in www folder:
    {
       "pushServerURL": "<pushServerURL e.g http(s)//host:port/context >",
       "android": {
          "senderID": "<senderID e.g Google Project ID only for android>",
          "variantID": "<variantID e.g. 1234456-234320>",
          "variantSecret": "<variantSecret e.g. 1234456-234320>"
       },
       "ios": {
          "variantID": "<variantID e.g. 1234456-234320>",
          "variantSecret": "<variantSecret e.g. 1234456-234320>"
       }
    }
    push.register(onNotification, successHandler);

    @example
    //push-config.json file in www folder and add categories and alias
    push.register(onNotification, successHandler, errorHandler, {alias: "me@me.com", categories: ["sport", "news"]});
*/
Push.prototype.register = function (onNotification, successCallback, errorCallback, options) {
    this.successCallback = successCallback;

    if (errorCallback == null) {
        errorCallback = function () {
        }
    }

    if (typeof onNotification != "function") {
        errorCallback("Push.register failure: onNotification callback parameter must be a function");
        return;
    }

    if (!options || !options.pushServerURL) {
        ajax({
            url: "push-config.json",
            dataType: "text"
        })
        .then( function( result ) {
            var data = JSON.parse(result.data);
            if (options) {
                for(var key in options) {
                    data[key] = options[key];
                }
            }
            exec(onNotification, errorCallback, "PushPlugin", "register", [data]);
        })
        .catch( function( error ) {
            errorCallback("Error reading config file " + error);
        });
    } else {
        exec(onNotification, errorCallback, "PushPlugin", "register", [options]);
    }

};

/**
    Unregisters the device with the APNS (iOS) or GCM (Android) and the Unified Push server.
    @status Stable
    @param {Function} success - callback to be executed if the request results in success
    @param {Function} [error] - callback to be executed if the request results in error
    @returns {void}
    @example

    push.unregister(successHandler, errorHandler);
*/
Push.prototype.unregister = function (successCallback, errorCallback) {
    if (errorCallback == null) {
        errorCallback = function () {
        }
    }

    if (typeof successCallback != "function") {
        console.log("Push.unregister failure: success callback parameter must be a function");
        return;
    }

    cordova.exec(successCallback, errorCallback, "PushPlugin", "unregister", []);
};


/**
    Call this to set the application icon badge -- ios specific
    @status Stable
    @param {Function} success - callback to be executed if the request results in success
    @param {String|Number} [badge] - the badge number to set on the application icon
    @returns {void}
    @example

    push.setApplicationIconBadgeNumber(successHandler, errorHandler);
*/
Push.prototype.setApplicationIconBadgeNumber = function (successCallback, badge) {
    if (typeof successCallback != "function") {
        console.log("Push.setApplicationIconBadgeNumber failure: success callback parameter must be a function");
        return;
    }

    exec(successCallback, successCallback, "PushPlugin", "setApplicationIconBadgeNumber", [
        {badge: badge}
    ]);
};

/**
 * Call this function to tell the OS if there was data or not so it can schedule the next fetch operation
 * @param {int} dataType - one of the BackgroundFetchResults or 0 new data 1 no data or 2 failed
 * @returns {void}
 */
Push.prototype.setContentAvailable = function(dataType) {
    return exec(null, null, "PushPlugin", "setContentAvailable", [{type: dataType}]);
};

/**
    Clear the OS notifications
    @param {Function} success - callback to be executed if the request results in success
    @param {Function} [error] - callback to be executed if the request results in error
    @returns {void}
    @example

    push.clearNotifications(successHandler, errorHandler);
*/
Push.prototype.clearNotifications = function (successCallback, errorCallback) {
    if (errorCallback == null) {
        errorCallback = function () {
        }
    }

    if (successCallback == null) {
        successCallback = function () {
        }
    }
    cordova.exec(successCallback, errorCallback, "PushPlugin", "clearNotifications", []);
};


var push = new Push();
if (Object.freeze) {
    Object.freeze(push.FetchResult);
}

module.exports = push;
