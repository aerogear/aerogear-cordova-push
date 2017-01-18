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

import org.apache.cordova.*;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushRegistrar;
import org.jboss.aerogear.android.unifiedpush.metrics.UnifiedPushMetricsMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
public class PushPlugin extends CordovaPlugin {
  public static final String TAG = "PushPlugin";

  private static final String UNIFIED_PUSH_URL = "pushServerURL";
  private static final String FCM_SENDER_ID = "senderID";
  private static final String VARIANT_ID = "variantID";
  private static final String SECRET = "variantSecret";
  private static final String DEVICE_TOKEN = "deviceToken";
  private static final String CATEGORIES = "categories";
  private static final String ALIAS = "alias";
  private static final String SEND_METRICS = "sendMetricInfo";

  public static final String REGISTER = "register";
  public static final String MESSAGE_CHANNEL = "messageChannel";
  public static final String UNREGISTER = "unregister";

  private static final String REGISTRAR = "registrar";
  private static final String SETTINGS = "settings";

  private static CallbackContext context;
  private static CallbackContext channel;
  private static Bundle cachedMessage = null;
  private static boolean foreground = false;
  private static boolean sendMetrics;

  private SharedPreferences preferences;

  /**
   * Gets the application context from cordova's main activity.
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
    Log.v(TAG, "execute: action=" + action);
    foreground = true;

    if (REGISTER.equals(action)) {
      Log.v(TAG, "execute: data=" + data.toString());

      try {
        context = callbackContext;

        JSONObject pushConfig = parseConfig(data);
        saveConfig(pushConfig);
        sendMetrics = Boolean.parseBoolean(preferences.getString(SEND_METRICS, "false"));
        cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            register(callbackContext);
          }
        });
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
        return false;
      }

      return true;
    } else if (UNREGISTER.equals(action)) {

      unRegister(callbackContext);
      return true;
    } else if (MESSAGE_CHANNEL.equals(action)) {
      channel = callbackContext;
      PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
      result.setKeepCallback(true);
      callbackContext.sendPluginResult(result);
      return true;
    } else {
      callbackContext.error("Invalid action : " + action);
    }

    return false;
  }

  private JSONObject parseConfig(JSONArray data) throws JSONException {
    JSONObject pushConfig = data.getJSONObject(0);
    if (!pushConfig.isNull("android")) {
      final JSONObject android = pushConfig.getJSONObject("android");
      for (Iterator iterator = android.keys(); iterator.hasNext(); ) {
        String key = (String) iterator.next();
        pushConfig.put(key, android.get(key));
      }

      pushConfig.remove("android");
    }
    return pushConfig;
  }

  private void saveConfig(JSONObject config) throws JSONException {
    final SharedPreferences.Editor editor = preferences.edit();
    for (Iterator i = config.keys(); i.hasNext(); ) {
      final String key = String.valueOf(i.next());
      editor.putString(key, config.getString(key));
    }

    editor.commit();
  }

  private void register(final CallbackContext callbackContext) {
    try {
      PushRegistrar registrar = getPushRegistrar();
      registrar.register(getApplicationContext(), new Callback<Void>() {
        @Override
        public void onSuccess(Void data) {
          PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
          result.setKeepCallback(true);
          callbackContext.sendPluginResult(result);

          success();
        }

        @Override
        public void onFailure(Exception e) {
          callbackContext.error(e.getMessage());
        }
      });
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
    if (cachedMessage != null) {
      Log.v(TAG, "sending cached extras");
      sendMessage(cachedMessage);
      cachedMessage = null;
    }
  }

  private void success() {
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
    pluginResult.setKeepCallback(true);
    channel.sendPluginResult(pluginResult);
  }

  private void unRegister(CallbackContext callbackContext) {
    PushRegistrar registrar = RegistrarManager.getRegistrar(REGISTRAR);
    if (registrar != null) {
      registrar.unregister(getApplicationContext(), new VoidCallback(callbackContext));
    } else {
      callbackContext.error("You must register, before you can unregister!");
    }
  }

  private PushRegistrar getPushRegistrar() {
    try {
      RegistrarManager.config(REGISTRAR, AeroGearFCMPushConfiguration.class)
              .setPushServerURI(new URI(preferences.getString(UNIFIED_PUSH_URL, null)))
              .setSenderId(preferences.getString(FCM_SENDER_ID, null))
              .setVariantID(preferences.getString(VARIANT_ID, null))
              .setSecret(preferences.getString(SECRET, null))
              .setAlias(preferences.getString(ALIAS, null))
              .setCategories(convert(preferences.getString(CATEGORIES, null)))
              .asRegistrar();
      return RegistrarManager.getRegistrar(REGISTRAR);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private List<String> convert(String categories) throws JSONException {
    List<String> categoryList = new ArrayList<String>();
    if (categories != null) {
      categoryList = new ArrayList<String>();
      final JSONArray jsonArray = new JSONArray(categories);
      for (int i = 0; i < jsonArray.length(); i++) {
        categoryList.add(jsonArray.getString(i));
      }
    }
    return categoryList;
  }

  /**
   * Sends the message to the client application.
   * If the client application isn't currently active, it is cached for later processing.
   * @param message the message to be send to the client
   */
  public static void sendMessage(Bundle message) {
    if (message != null) {
      if (sendMetrics && (!foreground || cachedMessage != null)) {
        final UnifiedPushMetricsMessage metricsMessage = new UnifiedPushMetricsMessage(message);
        ((AeroGearFCMPushRegistrar)RegistrarManager.getRegistrar(REGISTRAR)).sendMetrics(metricsMessage, new Callback<UnifiedPushMetricsMessage>() {
          @Override
          public void onSuccess(UnifiedPushMetricsMessage unifiedPushMetricsMessage) {
            Log.i(TAG, String.format("The message '%s' was marked as opened", metricsMessage.getMessageId()));
          }

          @Override
          public void onFailure(Exception e) {
            Log.e(TAG, e.getMessage(), e);
          }
        });
      }

      message.putBoolean("foreground", foreground);
      if (context != null) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, convertBundleToJson(message));
        result.setKeepCallback(true);
        context.sendPluginResult(result);
      } else {
        Log.v(TAG, "sendMessage: caching message to send at a later time.");
        cachedMessage = message;
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

  /**
   * Serializes a bundle to JSON.
   * @param message to be serialized
   */
  private static JSONObject convertBundleToJson(Bundle message) {
    try {
      JSONObject json;
      json = new JSONObject();

      JSONObject jsondata = new JSONObject();
      for (String key : message.keySet()) {
        Object value = message.get(key);

        // System data from Android
        if (key.equals("from") || key.equals("collapse_key")) {
          json.put(key, value);
        } else if (key.equals("foreground")) {
          json.put(key, message.getBoolean("foreground"));
        } else if (key.equals("coldstart")) {
          json.put(key, message.getBoolean("coldstart"));
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
    return context != null;
  }

  public void onDestroy() {
    context = null;
    super.onDestroy();
  }

  private class VoidCallback implements Callback<Void> {
    private final CallbackContext callbackContext;

    public VoidCallback(CallbackContext callbackContext) {
      this.callbackContext = callbackContext;
    }

    @Override
    public void onSuccess(Void data) {
      callbackContext.success("OK");
    }

    @Override
    public void onFailure(Exception e) {
      callbackContext.error(e.getMessage());
    }
  }
}
