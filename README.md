# AeroGear PushPlugin Cordova
> This plugin makes starting with AeroGear Unified Push simple.

## Getting Started 
If you haven't used [Cordova CLI](https://github.com/apache/cordova-cli) before, be sure to check out the [CLI Guide](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html), as it explains how to install the CLI tool. Once you're familiar with that process, you may proceed with the following commands:

### Create the App
> Create a new app by executing:

    cordova create <project-name>

### Add platform(s)
> Specify a set of target platforms by executing:

    cordova platform add <platform>

The available _platform_ values are _ios_ and _android_.

### Install the plugin
> Install the aerogear-pushplugin-cordova plugin by executing:

    cordova plugin add https://github.com/aerogear/aerogear-pushplugin-cordova.git

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

push.register(successHandler, errorHandler, {"badge": "true", "sound": "true",
    "alert": "true", "ecb": "onNotification", pushConfig: pushConfig});
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
* [AeroGear Push plugin API doc](http://aerogear.org/docs/specs/aerogear-cordova/index.html)
