/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.unifiedpush;

import android.content.Context;
import android.os.Bundle;

/**
 * For receiving GCM messages this interface needs to be implemented.
 */
public interface MessageHandler {

    /**
     * Invoked when the Google Cloud Messaging Server deleted some
     * pending messages because they were collapsible.
     * 
     * @param context The Context in which the AeroGear message receiver is running.
     * @param message A map of extended data from the intent, delivered to the AeroGear message receiver.
     */
    public void onDeleteMessage(Context context, Bundle message);

    /**
     * Invoked when the Google Cloud Messaging Server delivered a message to the device.
     * 
     * @param context The Context in which the AeroGear message receiver is running.
     * @param message A map containing the submitted key/value pairs
     */
    public void onMessage(Context context, Bundle message);

    /**
     * Invoked when the Google Cloud Messaging Server indicates a send error.
     */
    public void onError();
}
