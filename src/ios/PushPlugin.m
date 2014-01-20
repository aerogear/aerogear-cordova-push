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
@synthesize notificationCallbackId;
@synthesize callback;


- (void)unregister:(CDVInvokedUrlCommand *)command; {
    self.callbackId = command.callbackId;

    [[UIApplication sharedApplication] unregisterForRemoteNotifications];
    [self successWithMessage:@"unregistered"];
}

- (void)register:(CDVInvokedUrlCommand *)command; {
    self.callbackId = command.callbackId;

    NSMutableDictionary *options = [command.arguments objectAtIndex:0];

    UIRemoteNotificationType notificationTypes = UIRemoteNotificationTypeNone;
    id badgeArg = [options objectForKey:@"badge"];
    id soundArg = [options objectForKey:@"sound"];
    id alertArg = [options objectForKey:@"alert"];

    if ([badgeArg isKindOfClass:[NSString class]]) {
        if ([badgeArg isEqualToString:@"true"])
            notificationTypes |= UIRemoteNotificationTypeBadge;
    }
    else if ([badgeArg boolValue])
        notificationTypes |= UIRemoteNotificationTypeBadge;

    if ([soundArg isKindOfClass:[NSString class]]) {
        if ([soundArg isEqualToString:@"true"])
            notificationTypes |= UIRemoteNotificationTypeSound;
    }
    else if ([soundArg boolValue])
        notificationTypes |= UIRemoteNotificationTypeSound;

    if ([alertArg isKindOfClass:[NSString class]]) {
        if ([alertArg isEqualToString:@"true"])
            notificationTypes |= UIRemoteNotificationTypeAlert;
    }
    else if ([alertArg boolValue])
        notificationTypes |= UIRemoteNotificationTypeAlert;

    self.callback = [options objectForKey:@"ecb"];

    if (notificationTypes == UIRemoteNotificationTypeNone)
        NSLog(@"PushPlugin.register: Push notification type is set to none");

    isInline = NO;

    [self.commandDelegate runInBackground:^{
        [self saveConfig:[options objectForKey:@"pushConfig"]];
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:notificationTypes];

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

    if (notificationMessage)            // if there is a pending startup notification
        [self notificationReceived];    // go ahead and process it
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {

    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSString *url = [userDefaults objectForKey:@"pushServerURL"];
    AGDeviceRegistration *registration =
            [[AGDeviceRegistration alloc] initWithServerURL:[NSURL URLWithString:url]];

    
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

    if (notificationMessage && self.callback) {
        NSMutableString *jsonStr = [NSMutableString stringWithString:@"{"];

        [self parseDictionary:notificationMessage intoJSON:jsonStr];

        if (isInline) {
            [jsonStr appendFormat:@"foreground:'%d',", 1];
            isInline = NO;
        }
        else
            [jsonStr appendFormat:@"foreground:'%d',", 0];

        [jsonStr appendString:@"}"];

        NSLog(@"Msg: %@", jsonStr);

        NSString *jsCallBack = [NSString stringWithFormat:@"%@(%@);", self.callback, jsonStr];
        [self.webView stringByEvaluatingJavaScriptFromString:jsCallBack];

        self.notificationMessage = nil;
    }
}

// reentrant method to drill down and surface all sub-dictionaries' key/value pairs into the top level json
- (void)parseDictionary:(NSDictionary *)inDictionary intoJSON:(NSMutableString *)jsonString {
    NSArray *keys = [inDictionary allKeys];
    NSString *key;

    for (key in keys) {
        id thisObject = [inDictionary objectForKey:key];

        if ([thisObject isKindOfClass:[NSDictionary class]])
            [self parseDictionary:thisObject intoJSON:jsonString];
        else
            [jsonString appendFormat:@"%@:'%@',", key, [inDictionary objectForKey:key]];
    }
}

- (void)setApplicationIconBadgeNumber:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options {
    DLog(@"setApplicationIconBadgeNumber:%@\n withDict:%@", arguments, options);

    self.callbackId = [arguments pop];

    int badge = [[options objectForKey:@"badge"] intValue] ? : 0;
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badge];

    [self successWithMessage:[NSString stringWithFormat:@"app badge count set to %d", badge]];
}

- (void)successWithMessage:(NSString *)message {
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];

    [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
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

- (void (^)(id <AGClientDeviceInformation>))pushConfig:(NSData *)deviceToken withDict:(NSDictionary *)options {
    return ^(id <AGClientDeviceInformation> clientInfo) {
        [clientInfo setDeviceToken:deviceToken];
        [clientInfo setAlias:[options objectForKey:@"alias"]];
        [clientInfo setCategories:[options objectForKey:@"category"]];
        [clientInfo setVariantID:[options objectForKey:@"variantID"]];
        [clientInfo setVariantSecret:[options objectForKey:@"variantSecret"]];

        UIDevice *currentDevice = [UIDevice currentDevice];
        [clientInfo setOperatingSystem:[currentDevice systemName]];  
        [clientInfo setOsVersion:[currentDevice systemVersion]];     
        [clientInfo setDeviceType: [currentDevice model]];                   
    };
}

@end
