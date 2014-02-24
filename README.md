# AeroGear PushPlugin Cordova
> This plugin makes starting with AeroGear Unified Push simple.

## Getting Started 
If you haven't used [Cordova CLI](https://github.com/apache/cordova-cli) before, be sure to check out the [CLI Guide](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html), as it explains how to install the CLI tool. Once you're familiar with that process, you may proceed with the following commands:

### Create the App
> Create a new app by executing:

    cordova create <project-name> [app-id] [app-name]

### Add platform(s)
> Specify a set of target platforms by executing:

    cordova platform add <platform>

The available _platform_ values are _ios_ and _android_.

### Install the plugin
> Install the aerogear-pushplugin-cordova plugin by executing:

    cordova plugin add https://github.com/aerogear/aerogear-pushplugin-cordova.git

Done! Your project now contains the AeroGear PushPlugin. For an integration with the _UnifiedPush Server_ open the ```www``` folder in your text editor and apply the code from the example below. Afterwards you can execute the project with ```cordova run <platform>```.

### Sample Example
The below JavaScript code registers a device in the AeroGear Unified Push Server devices registry:

```js
var pushConfig = {
    // senderID is only used in the Android/GCM case
    senderID: "<senderID e.g Google Project ID only for android>",
    pushServerURL: "<pushServerURL e.g http(s)//host:port/context >",
    variantID: "<variantID e.g. 1234456-234320>",
    variantSecret: "<variantSecret e.g. 1234456-234320>",
    alias: "<alias e.g. a username or an email address optional>"
}

//badge and sound are iOS specific, and ignored on Android
push.register(successHandler, errorHandler, {"badge": "true", "sound": "true",
    "ecb": "onNotification", pushConfig: pushConfig});
```


Start receiving messages:

```js
function onNotification(e) {
    alert(e.alert);
}
```

To unregister:

```js
push.unregister(successHandler, errorHandler);

function successHandler() {
    console.log('success')
}

function errorHandler(message) {
    console.log('error ' + message);
}

```

## Documentation
* [AeroGear Cordova](http://aerogear.org/cordova/)
* [AeroGear Push plugin API doc](http://staging.aerogear.org/docs/specs/aerogear-cordova/index.html)
