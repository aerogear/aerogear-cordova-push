module.exports = function(ctx) {

    var child_process = require('child_process');
    var path = require('path');
    var fs = require('fs');
    var Q = ctx.requireCordovaModule('q');
    var util = ctx.requireCordovaModule('cordova-lib/src/cordova/util');
    var xcode = ctx.requireCordovaModule('xcode');

    var deferral = Q.defer();

    var iosFolder;
    var projFolder;
    var projName;

    get_apple_xcode_version().then(function (version) {
    
        var versionParts = version.split('.');
        var mayorVersion = versionParts[0];
        if (mayorVersion >= "8") {
            return getInstalledPlatformsWithVersions(util.isCordova());
        } else {
            throw new Error(".entitlements file not needed in Xcode 7, skipping hook");
        }

    }).then(function(platforms){

        if (platforms.ios&&platforms.ios<"4.3.0") {

            iosFolder = ctx.opts.cordova.project ? ctx.opts.cordova.project.root : path.join(ctx.opts.projectRoot, 'platforms/ios/');

            return readiOSFolder(iosFolder);

        } else {
            throw new Error("No need to run entitlements hooks for cordova-ios 4.3.0+");
        }

    }).then(function(iosFolderData){

        if (iosFolderData && iosFolderData.length) {
            iosFolderData.forEach(function (folder) {
                if (folder.match(/\.xcodeproj$/)) {
                    projFolder = path.join(iosFolder, folder);
                    projName = path.basename(folder, '.xcodeproj');
                }
            });
        }

        if (!projFolder || !projName) {
            throw new Error("Could not find a .xcodeproj");
        }

        var platformRoot = path.join(ctx.opts.projectRoot, 'www');
        var pushFile = path.join(platformRoot, 'Push.entitlements');

        return checkEntitlementInWWW(pushFile);

    }).then(function(pushFilePath){

        return readPushFile(pushFilePath);

    }).then(function(pushFileData){

        var resourcesFolderPath = path.join(iosFolder, projName, 'Resources');
        fs.existsSync(resourcesFolderPath) || fs.mkdirSync(resourcesFolderPath);
        var destFile = path.join(iosFolder, projName, 'Resources', projName + '.entitlements');

        fs.writeFileSync(destFile, pushFileData);

        var projectPath = path.join(projFolder, 'project.pbxproj');

        var pbxProject;
        if (ctx.opts.cordova.project) {
            pbxProject = ctx.opts.cordova.project.parseProjectFile(ctx.opts.projectRoot).xcode;
        } else {
            pbxProject = xcode.project(projectPath);
            pbxProject.parseSync();
        }

        pbxProject.addResourceFile(projName + ".entitlements");

        var configGroups = pbxProject.hash.project.objects['XCBuildConfiguration'];
        for (var key in configGroups) {
            var config = configGroups[key];
            if (config.buildSettings !== undefined) {
                config.buildSettings.CODE_SIGN_ENTITLEMENTS = '"' + projName + '/Resources/' + projName + '.entitlements"';
            }
        }

        fs.writeFileSync(projectPath, pbxProject.writeSync());

        deferral.resolve();

    }).catch( function(error) {
        if (error.message !== "Could not find a .xcodeproj") {
            deferral.resolve();
        } else {
            deferral.reject(error);
        }
    });

    return deferral.promise;;

    function get_apple_xcode_version() {
        var d = Q.defer();
        child_process.exec('xcodebuild -version', function(error, stdout, stderr) {
            var versionMatch = /Xcode (.*)/.exec(stdout);
            if (error || !versionMatch) {
                d.reject(stderr);
            } else {
                d.resolve(versionMatch[1]);
            }
        });
        return d.promise;
    };

    function readiOSFolder(iosFolder){
        var d = Q.defer();
        fs.readdir(iosFolder, function (err, data) {

            if (err) {
                d.reject(err);
            } else {
                d.resolve(data);
            }

        });
        return d.promise;
    }

    function readPushFile(pushFile) {
        var d = Q.defer();
        fs.readFile(pushFile, 'utf8', function (err, data) {
            if (err) {
                d.reject(err);
            } else {
                d.resolve(data);
            }
        });
        return d.promise;
    }

    function checkEntitlementInWWW(pushFile){
        var d = Q.defer();
        var pushFilePath;
        fs.stat(pushFile, function(err,stats) {
            if (err) {
                pushFilePath = path.join(ctx.opts.plugin.pluginInfo.dir, 'src/ios/Push.entitlements');
            } else {
                pushFilePath = pushFile;
            }
            d.resolve(pushFilePath)
        });
        return d.promise;
    }

    function getInstalledPlatformsWithVersions(project_dir) {
        var result = {};
        var platforms_on_fs = listPlatforms(project_dir);

        return Q.all(platforms_on_fs.map(function(p) {
            return spawn(path.join(project_dir, 'platforms', p, 'cordova', 'version'), [], { chmod: true })
            .then(function(v) {
                result[p] = v || null;
            }, function(v) {
                result[p] = 'broken';
            });
        })).then(function() {
            return result;
        });
    }

    function listPlatforms(project_dir) {
        var core_platforms = ctx.requireCordovaModule('cordova-lib/src/platforms/platforms');
        var platforms_dir = path.join(project_dir, 'platforms');
        if ( !fs.existsSync(platforms_dir)) {
            return [];
        }
        var subdirs = fs.readdirSync(platforms_dir);
        return subdirs.filter(function(p) {
            return Object.keys(core_platforms).indexOf(p) > -1;
        });
    }

    function spawn(cmd, opts) {
        opts = opts || {};
        var d = Q.defer();

        if (opts.chmod) {
            try {
                // This fails when module is installed in a system directory (e.g. via sudo npm install)
                fs.chmodSync(cmd, '755');
            } catch (e) {
                // If the perms weren't set right, then this will come as an error upon execution.
            }
        }

        var child = child_process.spawn(cmd, [], opts);
        var capturedOut = '';
        var capturedErr = '';

        if (child.stdout) {
            child.stdout.setEncoding('utf8');
            child.stdout.on('data', function(data) {
                capturedOut += data;
                d.notify({'stdout': data});
            });
        }

        if (child.stderr) {
            child.stderr.setEncoding('utf8');
            child.stderr.on('data', function(data) {
                capturedErr += data;
                d.notify({'stderr': data});
            });
        }

        child.on('close', whenDone);
        child.on('error', whenDone);
        function whenDone(arg) {
            child.removeListener('close', whenDone);
            child.removeListener('error', whenDone);
            var code = typeof arg == 'number' ? arg : arg && arg.code;

            if (code === 0) {
                d.resolve(capturedOut.trim());
            } else {
                var errMsg = cmd + ': Command failed with exit code ' + code;
                if (capturedErr) {
                    errMsg += ' Error output:\n' + capturedErr.trim();
                }
                var err = new Error(errMsg);
                err.code = code;
                d.reject(err);
            }
        }

        return d.promise;
    };

    function maybeQuote(a) {
        if (/^[^"].*[ &].*[^"]/.test(a)) return '"' + a + '"';
        return a;
    }


};