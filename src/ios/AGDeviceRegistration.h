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
#import "AGClientDeviceInformation.h"

/**
 * Utility to register an iOS device with the AeroGear UnifiedPush Server.
 */
@interface AGDeviceRegistration : NSObject

/**
 * An initializer method to instantiate an AGDeviceRegistration object.
 *
 * @param url the URL of the AeroGear Push server.
 *
 * @return the AGDeviceRegistration object.
 */
- (id) initWithServerURL:(NSURL *)url;

/**
 * Registers your mobile device to the AeroGear Push server so it can
 * start receiving messages.
 *
 * @param clientInfo A block object which passes in an implementation of the AGClientDeviceInformation protocol that
 * holds configuration metadata that would be posted to the server during the registration process.
 *
 * @param success A block object to be executed when the registration operation finishes successfully.
 * This block has no return value.
 *
 * @param failure A block object to be executed when the registration operation finishes unsuccessfully.
 * This block has no return value and takes one argument: The `NSError` object describing
 * the error that occurred during the registration process.
 *
 */
-(void) registerWithClientInfo:(void (^)(id<AGClientDeviceInformation>)) clientInfo
                       success:(void (^)(void))success
                       failure:(void (^)(NSError *error))failure;

/**
 * Convenient method to access a shared instance of the AGDeviceRegistration object.
 * Note that this object is initialized only after the initializer initWithServerURL:url
 * has been called.
 *
 * @return the shared AGDeviceRegistration object or nil if not yet initialized.
 */
 + (AGDeviceRegistration*) sharedInstance;


@end
