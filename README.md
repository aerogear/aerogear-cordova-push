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

'ecb' (event callback) is a string that will get executed by the plugin when a new message arrives. Be sure to make this a
function that is reachable with the right context e.g define it in _global scope_ or have the right prefix
on it. In our example we define onNotification in global scope.

Start receiving messages:

```js
function onNotification(event) {
    alert(event.alert);
}
```

The passed in 'event' object contains:
* alert the alert message send 
* coldstart was the app running when the message was received
* badge the number to display on the icon ios specific
* any custom set properties are available under e.payload ( e.g alert(e.payload.foo); )
Also have a look here for [more information about the message](http://aerogear.org/docs/specs/aerogear-push-messages/) sending and properties

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

Full example ([index.html](example/index.html)):

```html
<!DOCTYPE HTML>
<html>
<head>
  <title>Demo</title>
</head>
<body>

<script type="text/javascript" charset="utf-8" src="cordova.js"></script>
<script type="text/javascript" charset="utf-8" src="jquery_1.5.2.min.js"></script>

<script type="text/javascript">

  function onDeviceReady() {
    var pushConfig = {
      senderID: "<senderID>",
      pushServerURL: "<pushServerURL>",
      variantID: "<variantID>",
      variantSecret: "<variantSecret>",
      alias: "<alias>"
    }

    var statusList = $("#app-status-ul");
    statusList.append('<li>deviceready event received</li>');

    try {
      statusList.append('<li>registering </li>');
      push.register(successHandler, errorHandler, {"badge": "true", "sound": "true",
        "ecb": "onNotification", pushConfig: pushConfig});
    } catch (err) {
      txt = "There was an error on this page.\n\n";
      txt += "Error description: " + err.message + "\n\n";
      alert(txt);
    }
  }

  function onNotification(e) {
    var statusList = $("#app-status-ul");

    // if the notification contains a sound, play it.
    if (e.sound) {
      //install the media plugin to use this
      var media = new Media("/android_asset/www/" + e.sound);
      media.play();
    }

    if (e.coldstart) {
      statusList.append('<li>--COLDSTART NOTIFICATION--' + '</li>');
    }

    statusList.append('<li>MESSAGE -> MSG: ' + e.alert + '</li>');
    if (e.msgcnt) {
      statusList.append('<li>MESSAGE -> MSGCNT: ' + e.msgcnt + '</li>');
    }

    //only on ios
    if (e.badge) {
      push.setApplicationIconBadgeNumber(successHandler, e.badge);
    }
  }

  function successHandler(result) {
    $("#app-status-ul").append('<li>success:' + result + '</li>');
  }

  function errorHandler(error) {
    $("#app-status-ul").append('<li>error:' + error + '</li>');
  }

  document.addEventListener('deviceready', onDeviceReady, true);

</script>
<div id="home">
  <div id="app-status-div">
    <ul id="app-status-ul">
      <li>AeroGear PushPlugin Unified Push Demo</li>
    </ul>
  </div>
</div>
</body>
</html>

```


## Documentation
* [AeroGear Cordova](http://aerogear.org/cordova/)
* [AeroGear Push plugin API doc](http://aerogear.org/docs/specs/aerogear-cordova/index.html)
