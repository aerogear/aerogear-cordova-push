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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * @author edewit@redhat.com
 */
public class PushPlugin extends CordovaPlugin {
  public static final String TAG = "PushPlugin";

  private static final String UNIFIED_PUSH_URL = "pushServerURL";
  private static final String GCM_SENDER_ID = "senderID";
  private static final String VARIANT_ID = "variantID";
  private static final String SECRET = "variantSecret";
  private static final String ALIAS = "alias";

  public static final String REGISTER = "register";
  public static final String UNREGISTER = "unregister";

  private static final String SETTINGS = "settings";

  private static CordovaWebView webViewReference;
  private static String javascriptCallback;
  private static Bundle cachedExtras = null;
  private static boolean foreground = true;


  private SharedPreferences preferences;

  /**
   * Gets the application context from cordova's main activity.
   *
   * @return the application context
   */
  private Context getApplicationContext() {
    return this.cordova.getActivity().getApplicationContext();
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    preferences = cordova.getActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
  }

  @Override
  public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) {

    boolean result = false;

    Log.v(TAG, "execute: action=" + action);

    if (REGISTER.equals(action)) {

      Log.v(TAG, "execute: data=" + data.toString());

      try {
        JSONObject jo = data.getJSONObject(0);

        webViewReference = this.webView;
        Log.v(TAG, "execute: jo=" + jo.toString());

        javascriptCallback = (String) jo.get("ecb");
        Log.v(TAG, "execute: ECB=" + javascriptCallback);

        JSONObject pushConfig = jo.getJSONObject("pushConfig");

        saveConfig(pushConfig);
        cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            register(callbackContext);
          }
        });

        result = true;
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }

      if (cachedExtras != null) {
        Log.v(TAG, "sending cached extras");
        sendExtras(cachedExtras);
        cachedExtras = null;
      }

    } else if (UNREGISTER.equals(action)) {

      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          unRegister(callbackContext);
        }
      });

      result = true;
    } else {
      callbackContext.error("Invalid action : " + action);
    }

    return result;
  }

  private void saveConfig(JSONObject config) throws JSONException {
    final SharedPreferences.Editor editor = preferences.edit();
    for (Iterator i = config.keys(); i.hasNext(); ) {
      final String key = String.valueOf(i.next());
      editor.putString(key, config.getString(key));
    }

    editor.commit();
  }

  private void register(CallbackContext callbackContext) {
    PushRegistrar registrar = getPushRegistrar();
    registrar.register(getApplicationContext(), new VoidCallback(callbackContext));
  }

  private void unRegister(CallbackContext callbackContext) {
    PushRegistrar registrar = getPushRegistrar();
    registrar.unregister(getApplicationContext(), new VoidCallback(callbackContext));
  }

  private PushRegistrar getPushRegistrar() {
    Registrations registrations = new Registrations();
    return registrations.push("registrar", getPushConfig());
  }

  private PushConfig getPushConfig() {
    try {
      final URI pushServerURI = new URI(preferences.getString(UNIFIED_PUSH_URL, null));
      PushConfig config = new PushConfig(pushServerURI, preferences.getString(GCM_SENDER_ID, null));
      config.setVariantID(preferences.getString(VARIANT_ID, null));
      config.setSecret(preferences.getString(SECRET, null));
      config.setAlias(preferences.getString(ALIAS, "message"));
      return config;
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * Sends a json object to the client as parameter to a method which is defined in javascriptCallback.
   */
  public static void sendJavascript(JSONObject json) {
    String _d = "javascript:" + javascriptCallback + "(" + json.toString() + ")";
    Log.v(TAG, "sendJavascript: " + _d);

    if (javascriptCallback != null && webViewReference != null) {
      webViewReference.sendJavascript(_d);
    }
  }

  /*
   * Sends the pushbundle extras to the client application.
   * If the client application isn't currently active, it is cached for later processing.
   */
  public static void sendExtras(Bundle extras) {
    if (extras != null) {
      extras.putBoolean("foreground", foreground);
      if (javascriptCallback != null && webViewReference != null) {
        sendJavascript(convertBundleToJson(extras));
      } else {
        Log.v(TAG, "sendExtras: caching extras to send at a later time.");
        cachedExtras = extras;
      }
    }
  }

  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);
    foreground = false;
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    foreground = true;
  }  

  /*
   * serializes a bundle to JSON.
   */
  private static JSONObject convertBundleToJson(Bundle extras) {
    try {
      JSONObject json;
      json = new JSONObject();

      JSONObject jsondata = new JSONObject();
      for (String key : extras.keySet()) {
        Object value = extras.get(key);

        // System data from Android
        if (key.equals("from") || key.equals("collapse_key")) {
          json.put(key, value);
        } else if (key.equals("foreground")) {
          json.put(key, extras.getBoolean("foreground"));
        } else if (key.equals("coldstart")) {
          json.put(key, extras.getBoolean("coldstart"));
        } else {
          // Maintain backwards compatibility
          if (key.equals("message") || key.equals("msgcnt") || key.equals("sound") || key.equals("alert")) {
            json.put(key, value);
          }

          if (value instanceof String) {
            // Try to figure out if the value is another JSON object

            String strValue = (String) value;
            if (strValue.startsWith("{")) {
              try {
                JSONObject json2 = new JSONObject(strValue);
                jsondata.put(key, json2);
              } catch (Exception e) {
                jsondata.put(key, value);
              }
              // Try to figure out if the value is another JSON array
            } else if (strValue.startsWith("[")) {
              try {
                JSONArray json2 = new JSONArray(strValue);
                jsondata.put(key, json2);
              } catch (Exception e) {
                jsondata.put(key, value);
              }
            } else {
              jsondata.put(key, value);
            }
          }
        }
      } // while
      json.put("payload", jsondata);

      Log.v(TAG, "extrasToJSON: " + json.toString());

      return json;
    } catch (JSONException e) {
      Log.e(TAG, "extrasToJSON: JSON exception");
    }
    return null;
  }

  public static boolean isInForeground() {
    return foreground;
  }

  public static void setForeground(boolean foreground) {
    PushPlugin.foreground = foreground;
  }

  public static boolean isActive() {
    return webViewReference != null;
  }

  public void onDestroy() {
    GCMRegistrar.onDestroy(getApplicationContext());
    webViewReference = null;
    javascriptCallback = null;
    super.onDestroy();
  }

  private class VoidCallback implements Callback<Void> {
    private final CallbackContext callbackContext;

    public VoidCallback(CallbackContext callbackContext) {
      this.callbackContext = callbackContext;
    }

    @Override
    public void onSuccess(Void data) {
      callbackContext.success("success");
    }

    @Override
    public void onFailure(Exception e) {
      callbackContext.error(e.getMessage());
    }
  }
}
