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

Note that after installing the plugin on an iOS Cordova project, you need to install the dependencies required by the plugin. This can be done by using [CocoaPods](http://cocoapods.org/), the Objective-C library dependency manager. Navigate to the Cordova iOS project's root folder and execute:

    pod install

### Sample Example
The below JavaScript code registers a device in the AeroGear Unified Push Server devices registry:

```js
var pushConfig = {
    // senderID is only used in the Android/GCM case
    senderID: "<senderID>",
    pushServerURL: "<pushServerURL>",
    variantID: "<variantID>",
    variantSecret: "<variantSecret>",
    alias: "<alias>"
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

## Documentation
* [AeroGear Cordova](http://aerogear.org/cordova/)
