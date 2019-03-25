module.exports = function(ctx) {
    var fs = ctx.requireCordovaModule('fs'),
        path = ctx.requireCordovaModule('path'),
        os = require("os"),
        readline = require("readline"),
        deferral = ctx.requireCordovaModule('q').defer();

    var platformRoot = path.join(ctx.opts.projectRoot, 'www');
    var settingsFile = path.join(platformRoot, 'google-services.json');

    var platformAndroid = 'platforms/android';

    if (fs.existsSync('platforms/android/app')) {
        // cordova-android >= 7.0.0
        platformAndroid = 'platforms/android/app';
    }

    fs.stat(settingsFile, function(err,stats) {
        if (err) {
            deferral.reject("To use this plugin on android you'll need to add a google-services.json file with the FCM project_info and place that into your www folder");
        } else {

            fs.createReadStream(settingsFile).pipe(fs.createWriteStream(platformAndroid + '/google-services.json'));

            var lineReader = readline.createInterface({
                terminal: false,
                input : fs.createReadStream(platformAndroid + '/build.gradle')
            });
            lineReader.on("line", function(line) {
                fs.appendFileSync('./build.gradle', line.toString() + os.EOL);
                if (/.*\ dependencies \{.*/.test(line)) {
                    fs.appendFileSync('./build.gradle', '\t\tclasspath "com.google.gms:google-services:3.0.0"' + os.EOL);
                    fs.appendFileSync('./build.gradle', '\t\tclasspath "com.android.tools.build:gradle:1.2.3+"' + os.EOL);
                }
            }).on("close", function () {
                fs.rename('./build.gradle', platformAndroid + '/build.gradle', deferral.resolve);
            });

        }
    });

    return deferral.promise;
};
