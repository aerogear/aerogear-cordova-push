/*
 Copyright 2009-2011 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binaryform must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided withthe distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    NSMutableDictionary *options = [command.arguments objectAtIndex:0];

    UIRemoteNotificationType notificationTypes = UIRemoteNotificationTypeAlert;

    notificationTypes = [self parseFlag:notificationTypes option:[options objectForKey:@"badge"] flag:UIRemoteNotificationTypeBadge];
    notificationTypes = [self parseFlag:notificationTypes option:[options objectForKey:@"sound"] flag:UIRemoteNotificationTypeSound];

    isInline = NO;

    [self.commandDelegate runInBackground:^{
        [self saveConfig:[options objectForKey:@"pushConfig"]];
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:notificationTypes];

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

    if (notificationMessage)            // if there is a pending startup notification
        [self notificationReceived];    // go ahead and process it
}

- (UIRemoteNotificationType)parseFlag:(UIRemoteNotificationType)notificationTypes option:(id)option flag:(UIRemoteNotificationType)flag {
    if ([option isKindOfClass:[NSString class]]) {
        if ([option isEqualToString:@"true"]) {
            notificationTypes |= flag;
        }
    }
    else if ([option boolValue]) {
        notificationTypes |= flag;
    }
    return notificationTypes;
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {

    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSString *url = [userDefaults objectForKey:@"pushServerURL"];
    AGDeviceRegistration *registration = [[AGDeviceRegistration alloc] initWithServerURL:[NSURL URLWithString:url]];

    [registration registerWithClientInfo:[self pushConfig:deviceToken withDict:userDefaults] success:^() {

        // successfully registered!

    } failure:^(NSError *error) {
        NSLog(@"PushEE registration Error: %@", error);
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
