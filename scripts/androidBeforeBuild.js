module.exports = function(ctx) {
    var fs = ctx.requireCordovaModule('fs'),
        path = ctx.requireCordovaModule('path'),
        gradle = ctx.requireCordovaModule(path.join(ctx.opts.projectRoot, 'platforms/android/cordova/lib/builders/builders')).getBuilder('gradle'),
        spawn = ctx.requireCordovaModule('cordova-common').superspawn.spawn,
        deferral = ctx.requireCordovaModule('q').defer();

    var platformRoot = path.join(ctx.opts.projectRoot, 'www');
    var settingsFile = path.join(platformRoot, 'google-services.json');

    fs.stat(settingsFile, function(err,stats) {
        if (err) {
            deferral.reject("To use this plugin on android you'll need to add a google-services.json file with the FCM project_info and place that into your www folder");
        } else {
            fs.createReadStream(settingsFile).pipe(fs.createWriteStream('platforms/android/google-services.json'));
            fs.createReadStream(path.join(ctx.opts.plugin.pluginInfo.dir, '/scripts/process-google-services.gradle')).pipe(fs.createWriteStream('platforms/android/process-google-services.gradle'));
            gradle.prepEnv();

            var wrapper = path.join(ctx.opts.projectRoot, 'platforms/android/gradlew');
            spawn(wrapper, ['-b', 'platforms/android/process-google-services.gradle', 'processDebugGoogleServices', 'processReleaseGoogleServices'], {stdio: 'inherit'}).then(deferral.resolve);
        }
    });

    return deferral.promise;
};