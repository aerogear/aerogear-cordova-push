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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;

import java.util.Collection;

public class PushHandlerActivity extends Activity {
  private static String TAG = "PushHandlerActivity";

  /*
   * this activity will be started if the user touches a notification that we own.
   * We send it's data off to the push plugin for processing.
   * If needed, we boot up the main activity to kickstart the application.
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "onCreate");

    boolean isPushPluginActive = PushPlugin.isActive();
    processPushBundle(isPushPluginActive);

    finish();

    if (!isPushPluginActive) {
      forceMainActivityReload();
    }
  }

  /**
   * Takes the pushBundle extras from the intent,
   * and sends it through to the PushPlugin for processing.
   */
  private void processPushBundle(boolean isPushPluginActive) {
    Store<Message> store = DataManager.getStore("messageStore");

    // It may happen store is not initialize at this point.
    if (store == null) {
      store = (SQLStore<Message>) DataManager.config("messageStore", SQLStoreConfiguration.class)
              .withContext(getApplicationContext())
              .store(Message.class);
      ((SQLStore<Message>)store).openSync();
    }

    try {
      Collection<Message> collection = store.readAll();
      for (Message message : collection) {
        Bundle originalExtras = message.toBundle();
        if (!isPushPluginActive) {
          originalExtras.putBoolean("coldstart", true);
        }
        // Send metrics for opened notification.
        PushPlugin.sendMetricsForMessage(originalExtras);
      }

      //Send message (onNotification event) from notification.
      Intent intent = getIntent();
      Bundle extras = intent.getExtras();

      if(extras != null){
        if(extras.containsKey("message")) {
          // extract the data message in the Notification
          Bundle message = extras.getBundle("message");
          message.putBoolean("foreground", PushPlugin.isInForeground());

          if(isPushPluginActive) {
            PushPlugin.sendEvent(message);
          } else {
            //Send message to process later (onRegister)
            PushPlugin.sendMessage(message);
          }
        }
      }

      store.reset();
    }
    catch(Exception e) {
      Log.e(TAG, "processPushBundle: exception processing metrics for stored messages");
    }
  }

  /**
   * Forces the main activity to re-launch if it's unloaded.
   */
  private void forceMainActivityReload() {
    PackageManager pm = getPackageManager();
    Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
    startActivity(launchIntent);
  }
}
