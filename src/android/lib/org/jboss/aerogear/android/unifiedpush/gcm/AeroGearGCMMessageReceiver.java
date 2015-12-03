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
package org.jboss.aerogear.android.unifiedpush.gcm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;

import static org.jboss.aerogear.android.unifiedpush.PushConstants.*;

/**
 * <p>
 * AeroGear specific <code>BroadcastReceiver</code> implementation for Google Cloud Messaging.
 * 
 * <p>
 * Internally received messages are delivered to attached implementations of our <code>MessageHandler</code> interface.
 */
public class AeroGearGCMMessageReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 1;

    private static MessageHandler defaultHandler;
    private static boolean checkDefaultHandler = true;
    private static final String TAG = AeroGearGCMMessageReceiver.class.getSimpleName();
    public static final String DEFAULT_MESSAGE_HANDLER_KEY = "DEFAULT_MESSAGE_HANDLER_KEY";

    /**
     * When a GCM message is received, the attached implementations of our <code>MessageHandler</code> interface
     * are being notified.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // Ignore register ACK
        if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            return;
        }

        if (checkDefaultHandler) {
            checkDefaultHandler = false;
            Bundle metaData = getMetadata(context);
            if (metaData != null) {

                String defaultHandlerClassName = metaData.getString(DEFAULT_MESSAGE_HANDLER_KEY);
                if (defaultHandlerClassName != null) {
                    try {
                        Class<? extends MessageHandler> defaultHandlerClass = (Class<? extends MessageHandler>) Class.forName(defaultHandlerClassName);
                        defaultHandler = defaultHandlerClass.newInstance();
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage(), ex);
                    }

                }
            }
        }

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            intent.putExtra(ERROR, true);
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            intent.putExtra(DELETED, true);
        } else {
            intent.putExtra(MESSAGE, true);
        }

        // notity all attached MessageHandler implementations:
        RegistrarManager.notifyHandlers(context, intent, defaultHandler);
    }

    private Bundle getMetadata(Context context) {
        final ComponentName componentName = new ComponentName(context, AeroGearGCMMessageReceiver.class);
        try {
            ActivityInfo ai = context.getPackageManager().getReceiverInfo(componentName, PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            Bundle metaData = ai.metaData;
            if (metaData == null) {
                Log.d(TAG, "metaData is null. Unable to get meta data for " + componentName);
            } else {
                return metaData;
            }
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return null;

    }

}
