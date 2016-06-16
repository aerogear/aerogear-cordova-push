/*
 * JBoss, Home of Professional Open Source.
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#import "AGPushPlugin.h"
#import "AGDeviceRegistration.h"

@implementation AGPushPlugin

@synthesize notificationMessage;
@synthesize isInline;

@synthesize callbackId;
@synthesize channelId;


- (void)unregister:(CDVInvokedUrlCommand *)command; {
    [[UIApplication sharedApplication] unregisterForRemoteNotifications];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)register:(CDVInvokedUrlCommand *)command; {
    NSLog(@"register");
    self.callbackId = command.callbackId;

    isInline = NO;

    [self.commandDelegate runInBackground:^{
        NSMutableDictionary *options = [self parseOptions:command];
        [self saveConfig:options];
        
        // when running under iOS 8 we will use the new API for APNS registration
        #if __IPHONE_OS_VERSION_MAX_ALLOWED >= 80000
            if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
                UIUserNotificationSettings* notificationSettings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound categories:nil];
                [[UIApplication sharedApplication] registerUserNotificationSettings:notificationSettings];
                [[UIApplication sharedApplication] registerForRemoteNotifications];
            } else {
                [[UIApplication sharedApplication] registerForRemoteNotificationTypes: (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
            }

        #else
            [[UIApplication sharedApplication] registerForRemoteNotificationTypes: (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
        #endif

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [pluginResult setKeepCallback:@YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

    if (notificationMessage)            // if there is a pending startup notification
        [self notificationReceived];    // go ahead and process it
}

- (void)messageChannel:(CDVInvokedUrlCommand *)command; {
    self.channelId = command.callbackId;
}

- (NSMutableDictionary *)parseOptions:(CDVInvokedUrlCommand *)command {
    NSMutableDictionary *options = [command.arguments[0] mutableCopy];
    NSMutableDictionary *iosOptions = options[@"ios"];
    if (iosOptions) {
        for (NSString *key in iosOptions) {
            options[key] = iosOptions[key];
        }
    }
    [options removeObjectForKey:@"ios"];
    return options;
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {

    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSString *url = [userDefaults objectForKey:@"pushServerURL"];
    AGDeviceRegistration *registration = [[AGDeviceRegistration alloc] initWithServerURL:[NSURL URLWithString:url]];

    [registration registerWithClientInfo:[self pushConfig:deviceToken withDict:userDefaults] success:^() {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:channelId];
    } failure:^(NSError *error) {
        [self failWithError:error];
    }];
}

- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [self failWithError:error];
}

- (void)notificationReceived {
    NSLog(@"Notification received");

    if (notificationMessage && self.callbackId) {
        NSMutableDictionary *message = [notificationMessage[@"aps"] mutableCopy];
        NSMutableDictionary *extraPayload = [notificationMessage mutableCopy];
        NSDictionary *alert = message[@"alert"];
        
        BOOL isColdStart =  [[[NSUserDefaults standardUserDefaults] objectForKey:@"AGPush_wasLaunchedWithOptions"] boolValue];
        [[NSUserDefaults standardUserDefaults] setBool:NO forKey:@"AGPush_wasLaunchedWithOptions"];
        
        [extraPayload removeObjectForKey:@"aps"];
        message[@"payload"] = extraPayload;
        message[@"foreground"] = @(isInline);
        message[@"coldstart"] = @(isColdStart);
        if ([alert isKindOfClass:[NSDictionary class]]) {
            message[@"alert"] = alert[@"body"];
            message[@"alert-extra"] = alert;
        }
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
        result.keepCallback = @YES;
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        self.notificationMessage = nil;
    }
}

- (void)setContentAvailable:(CDVInvokedUrlCommand*)command {
    NSLog(@"setContentAvailable");
    if ([self.completionHandlers count]) {
        NSMutableDictionary *options = command.arguments[0];
        NSString *type = options[@"type"];
        void (^handler)(UIBackgroundFetchResult) = self.completionHandlers[0];
        handler((UIBackgroundFetchResult) [type intValue]);
        [self.completionHandlers removeObject:handler];
    }
}

- (void)backgroundFetch:(void (^)(UIBackgroundFetchResult))handler userInfo:(NSDictionary *)userInfo {
    NSLog(@"Background Fetch");
    if (!self.completionHandlers) {
        self.completionHandlers = [[NSMutableArray alloc] init];
    }
    [self.completionHandlers addObject:handler];
}

- (void)setApplicationIconBadgeNumber:(CDVInvokedUrlCommand *)command; {
    NSLog(@"setApplicationIconBadgeNumber:%@", command.arguments);

    NSMutableDictionary *options = command.arguments[0];
    int badge = [options[@"badge"] intValue] ? : 0;
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badge];

    NSString *message = [NSString stringWithFormat:@"app badge count set to %d", badge];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)failWithError:(NSError *)error {
    NSString *errorMessage = [NSString stringWithFormat:@"- %@", [error localizedDescription]];
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
    [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
}

- (void) saveConfig:(NSMutableDictionary *)dictionary {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    for (NSString* key in dictionary) {
        id value = dictionary[key];
        [defaults setObject:value forKey:key];
    }
    [defaults synchronize];
}

- (void (^)(id <AGClientDeviceInformation>))pushConfig:(NSData *)deviceToken withDict:(NSUserDefaults *)options {
    return ^(id <AGClientDeviceInformation> clientInfo) {
        [clientInfo setDeviceToken:deviceToken];
        [clientInfo setAlias:[options objectForKey:@"alias"]];
        [clientInfo setCategories:[options objectForKey:@"categories"]];
        [clientInfo setVariantID:[options objectForKey:@"variantID"]];
        [clientInfo setVariantSecret:[options objectForKey:@"variantSecret"]];

        UIDevice *currentDevice = [UIDevice currentDevice];
        [clientInfo setOperatingSystem:[currentDevice systemName]];
        [clientInfo setOsVersion:[currentDevice systemVersion]];
        [clientInfo setDeviceType: [currentDevice model]];
    };
}

@end
