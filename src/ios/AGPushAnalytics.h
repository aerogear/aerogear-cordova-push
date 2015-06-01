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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

// AeroGear Push error constants
extern NSString * const AGPushAnalyticsErrorDomain;
extern NSString * const AGPushAnalyticsNetworkingOperationFailingURLRequestErrorKey;
extern NSString * const AGPushAnalyticsNetworkingOperationFailingURLResponseErrorKey;

/**
 * Utility class used to send metrics information to the AeroGear UnifiedPush Server when the app is opened due to a Push notification.
 */
@interface AGPushAnalytics : NSObject

/**
 * Send metrics to the AeroGear Push server when the app is first launched or bring from background to
 * foreground due to a push notification.
 *
 * @param messageId The identifier of this push notification.
 *
 * @param completionHandler A block object to be executed when the send metrics operation finishes.
 */
+ (void) sendMetricsWhenAppLaunched:(NSDictionary *)launchOptions
               completionHandler:(void (^)(NSError *error))handler;

/**
 * Send metrics to the AeroGear Push server when the app is first launched or bring from background to
 * foreground due to a push notification.
 *
 * @param messageId The identifier of this push notification.
 */
+ (void) sendMetricsWhenAppLaunched:(NSDictionary *)launchOptions;

/**
 * Send metrics to the AeroGear Push server when the app is first launched or bring from background to
 * foreground due to a push notification.
 *
 * @param messageId The identifier of this push notification.
 *
 * @param completionHandler A block object to be executed when the send metrics operation finishes.
 */
+ (void) sendMetricsWhenAppAwoken:(UIApplicationState) applicationState
                        userInfo:(NSDictionary *)userInfo
               completionHandler:(void (^)(NSError *error))handler;

/**
 * Send metrics to the AeroGear Push server when the app is first launched or bring from background to
 * foreground due to a push notification.
 *
 * @param messageId The identifier of this push notification.
 */
+ (void) sendMetricsWhenAppAwoken:(UIApplicationState) applicationState
                        userInfo:(NSDictionary *)userInfo;

@end
