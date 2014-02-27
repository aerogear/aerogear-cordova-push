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
package org.jboss.aerogear.cordova.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;

public class NotificationMessageHandler implements MessageHandler {

  public static final int NOTIFICATION_ID = 237;
  private static final String TAG = "NotificationMessageHandler";

  @Override
  public void onMessage(Context context, Bundle message) {
    Log.d(TAG, "onMessage - context: " + context);

    if (message != null) {
      PushPlugin.sendMessage(message);

      // Send a notification if there is a message
      if (!PushPlugin.isInForeground() && message.getString("alert").length() != 0) {
        createNotification(context, message);
      }
    }
  }

  @Override
  public void onDeleteMessage(Context context, Bundle message) {
    Log.e(TAG, "onDeleteMessage: " + message);
  }

  @Override
  public void onError() {
    Log.e(TAG, "onError: ");
  }

  public void createNotification(Context context, Bundle extras) {
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String appName = getAppName(context);

    Intent notificationIntent = new Intent(context, PushHandlerActivity.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationIntent.putExtra("pushBundle", extras);

    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(context.getApplicationInfo().icon)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(appName)
            .setTicker(appName)
            .setAutoCancel(true)
            .setContentIntent(contentIntent);

    String message = extras.getString("alert");
    if (message != null) {
      builder.setContentText(message);
    } else {
      builder.setContentText("<missing message content>");
    }

    String msgcnt = extras.getString("msgcnt");
    if (msgcnt != null) {
      builder.setNumber(Integer.parseInt(msgcnt));
    }

    manager.notify(appName, NOTIFICATION_ID, builder.build());
  }

  private static String getAppName(Context context) {
    CharSequence appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
    return (String) appName;
  }
}
