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
#import "PushPlugin.h"
#import "AGDeviceRegistration.h"

@implementation PushPlugin

@synthesize notificationMessage;
@synthesize isInline;

@synthesize callbackId;


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
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:UIRemoteNotificationTypeAlert
                | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound];

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

    if (notificationMessage)            // if there is a pending startup notification
        [self notificationReceived];    // go ahead and process it
}

- (NSMutableDictionary *)parseOptions:(CDVInvokedUrlCommand *)command {
    NSMutableDictionary *options = [[command.arguments objectAtIndex:0] mutableCopy];
    NSMutableDictionary *iosOptions = [options objectForKey:@"ios"];
    if (iosOptions) {
        for (NSString *key in iosOptions) {
            [options setObject:[iosOptions objectForKey:key] forKey:key];
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
        [self.commandDelegate evalJs:@"cordova.require('org.jboss.aerogear.cordova.push.AeroGear.UnifiedPush').successCallback()"];
    } failure:^(NSError *error) {
        NSString *errorMessage = [NSString stringWithFormat:@"Push registration Error: %@", error];
        NSLog(errorMessage);
        CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
        [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
    }];
}

- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [self failWithMessage:@"" withError:error];
}

- (void)notificationReceived {
    NSLog(@"Notification received");

    if (notificationMessage && self.callbackId) {
        isInline = NO;

        NSMutableDictionary *message = [[notificationMessage objectForKey:@"aps"] mutableCopy];
        NSMutableDictionary *extraPayload = [notificationMessage mutableCopy];
        [extraPayload removeObjectForKey:@"aps"];
        [message setObject:extraPayload forKey:@"payload"];
        [message setObject:[NSNumber numberWithBool:isInline] forKey:@"foreground"];
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
        [result setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        self.notificationMessage = nil;
    }
}

- (void)setApplicationIconBadgeNumber:(CDVInvokedUrlCommand *)command; {
    DLog(@"setApplicationIconBadgeNumber:%@\n withDict:%@", arguments, options);

    NSMutableDictionary *options = [command.arguments objectAtIndex:0];
    int badge = [[options objectForKey:@"badge"] intValue] ? : 0;
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badge];

    NSString *message = [NSString stringWithFormat:@"app badge count set to %d", badge];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)failWithMessage:(NSString *)message withError:(NSError *)error {
    NSString *errorMessage = (error) ? [NSString stringWithFormat:@"%@ - %@", message, [error localizedDescription]] : message;
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];

    [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
}

- (void) saveConfig:(NSMutableDictionary *)dictionary {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    for (NSString* key in dictionary) {
        id value = [dictionary objectForKey:key];
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
