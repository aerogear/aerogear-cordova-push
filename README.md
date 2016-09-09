# AeroGear PushPlugin Cordova

[![npm package](https://nodei.co/npm/aerogear-cordova-push.png?downloads=true&downloadRank=true&stars=true)](https://nodei.co/npm/aerogear-cordova-push/)

[![Dependency Status](https://img.shields.io/david/aerogear/aerogear-cordova-push.svg?style=flat-square)](https://david-dm.org/aerogear/aerogear-cordova-push)
[![Known Vulnerabilities](https://snyk.io/test/npm/aerogear-cordova-push/badge.svg?style=flat-square)](https://snyk.io/test/npm/aerogear-cordova-push)

> This plugin makes starting with AeroGear Unified Push simple.

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Cordova Plugin  |
| Documentation:  | https://aerogear.org/docs/specs/aerogear-cordova/  |
| Issue tracker:  | https://issues.jboss.org/browse/AGCORDOVA  |
| Mailing lists:  | [aerogear-users](http://aerogear-users.1116366.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-users))  |
|                 | [aerogear-dev](http://aerogear-dev.1069024.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-dev))  |

## Getting Started 
* [AeroGear Push Plugin Guide](http://aerogear.org/docs/guides/aerogear-cordova/AerogearCordovaPush/)

## Run tests

1. Use your existing cordova app, or create a new one.
1. Add the plugin and the tests:

  ```bash
  cordova plugin add https://github.com/aerogear/aerogear-cordova-push.git
  cordova plugin add https://github.com/aerogear/aerogear-cordova-push.git#:/tests
  ```

1. Add this plugin:
  ```bash
  cordova plugin add http://git-wip-us.apache.org/repos/asf/cordova-plugin-test-framework.git
  ```
1. Change the start page in `config.xml` with `<content src="cdvtests/index.html" />` or navigate to cdvtests/index.html from within your app.

## Documentation

For more details about the current release, please consult [our documentation](https://aerogear.org/docs/specs/aerogear-cordova/).

## Development

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGCORDOVA) with some steps to reproduce it.
