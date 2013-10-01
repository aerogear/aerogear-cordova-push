AeroGear Pushplugin Cordova
===========================

This plugin makes starting with AeroGear Unified Push simple. First create a project:

	cordova create <project-name>

Then add a platform:

	cordova platform add android

install the plugin:
		
	cordova plugin add https://github.com/edewit/aerogear-pushplugin-cordova.git

in your javascript call register to register the device to the unified push server:

	var aeroConfig = {
	  senderID: "<senderID>",
	  pushServerURL: "<pushServerURL>",
	  variantID: "<variantID>",
	  variantSecret: "<variantSecret>"
	}

	push.register(successHandler, errorHandler, {"badge": "true", "sound": "true",
		"alert": "true", "ecb": "onNotification", aeroConfig: aeroConfig});

Start receiving messages in the passed ecb function:
	
	function onNotification(e) {
		alert(e.alert);
	}

Take a look at the documentation on [our website](http://aerogear.org/cordova/) for more information.