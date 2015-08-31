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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpRestProvider;
import org.jboss.aerogear.android.pipe.util.UrlUtils;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.metrics.MetricsSender;
import org.jboss.aerogear.android.unifiedpush.metrics.UnifiedPushMetricsMessage;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AeroGearGCMPushRegistrar implements PushRegistrar, MetricsSender<UnifiedPushMetricsMessage> {

    private final static String BASIC_HEADER = "Authorization";
    private final static String AUTHORIZATION_METHOD = "Basic";

    private static final Integer TIMEOUT = 30000;// 30 seconds
    /**
     * Default lifespan (7 days) of a registration until it is considered
     * expired.
     */
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

    private static final String TAG = AeroGearGCMPushRegistrar.class.getSimpleName();
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
    private static final String registryDeviceEndpoint = "/rest/registry/device";
    private static final String metricsEndpoint = "/rest/registry/device/pushMessage";

    private static final String DEVICE_ALREADY_UNREGISTERED = "Seems this device was already unregistered";

    private final Set<String> senderIds;

    private GoogleCloudMessaging gcm;
    private URL deviceRegistryURL;
    private URL metricsURL;
    private String deviceToken = "";
    private final String secret;
    private final String variantId;
    private final String deviceType;
    private final String alias;
    private final String operatingSystem;
    private final String osVersion;
    private final List<String> categories;

    private Provider<HttpProvider> httpProviderProvider = new Provider<HttpProvider>() {

        @Override
        public HttpProvider get(Object... in) {
            return new HttpRestProvider((URL) in[0], (Integer) in[1]);
        }
    };

    private Provider<GoogleCloudMessaging> gcmProvider = new Provider<GoogleCloudMessaging>() {

        @Override
        public GoogleCloudMessaging get(Object... context) {
            return GoogleCloudMessaging.getInstance((Context) context[0]);
        }
    };

    public AeroGearGCMPushRegistrar(UnifiedPushConfig config) {
        this.senderIds = config.getSenderIds();
        this.deviceToken = config.getDeviceToken();
        this.variantId = config.getVariantID();
        this.secret = config.getSecret();
        this.deviceType = config.getDeviceType();
        this.alias = config.getAlias();
        this.operatingSystem = config.getOperatingSystem();
        this.osVersion = config.getOsVersion();
        this.categories = new ArrayList<String>(config.getCategories());
        try {
            this.deviceRegistryURL = UrlUtils.appendToBaseURL(config.getPushServerURI().toURL(), registryDeviceEndpoint);
            this.metricsURL = UrlUtils.appendToBaseURL(config.getPushServerURI().toURL(), metricsEndpoint);
        } catch (MalformedURLException ex) {
            Log.e(TAG, ex.getMessage());
            throw new IllegalStateException("pushserverUrl was not a valid URL");
        }
    }

    @Override
    public void register(final Context context, final Callback<Void> callback) {
        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {

                try {

                    if (gcm == null) {
                        gcm = gcmProvider.get(context);
                    }
                    String regid = getRegistrationId(context);

                    if (regid.length() == 0) {
                        regid = gcm.register(senderIds
                                .toArray(new String[] {}));
                        AeroGearGCMPushRegistrar.this.setRegistrationId(context, regid);
                    }

                    deviceToken = regid;

                    HttpProvider httpProvider = httpProviderProvider.get(deviceRegistryURL, TIMEOUT);
                    setPasswordAuthentication(variantId, secret, httpProvider);

                    try {
                        JsonObject postData = new JsonObject();
                        postData.addProperty("deviceType", deviceType);
                        postData.addProperty("deviceToken", deviceToken);
                        postData.addProperty("alias", alias);
                        postData.addProperty("operatingSystem", operatingSystem);
                        postData.addProperty("osVersion", osVersion);
                        if (categories != null && !categories.isEmpty()) {
                            JsonArray jsonCategories = new JsonArray();
                            for (String category : categories) {
                                jsonCategories.add(new JsonPrimitive(category));
                            }
                            postData.add("categories", jsonCategories);
                        }

                        httpProvider.post(postData.toString());
                        return null;
                    } catch (HttpException ex) {
                        return ex;
                    }

                } catch (Exception ex) {
                    return ex;
                }

            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    callback.onSuccess(null);
                } else {
                    if (result instanceof HttpException) {
                        HttpException httpException = (HttpException) result;
                        switch (httpException.getStatusCode()) {
                        case HttpStatus.SC_MOVED_PERMANENTLY:
                        case HttpStatus.SC_MOVED_TEMPORARILY:
                        case HttpStatus.SC_TEMPORARY_REDIRECT:
                            Log.w(TAG, httpException.getMessage());
                            try {
                                URL redirectURL = new URL(httpException.getHeaders().get("Location"));
                                AeroGearGCMPushRegistrar.this.deviceRegistryURL = redirectURL;
                                register(context, callback);
                            } catch (MalformedURLException e) {
                                callback.onFailure(e);
                            }
                            break;
                        default:
                            callback.onFailure(result);
                        }
                    } else {
                        callback.onFailure(result);
                    }

                }
            }

        }.execute((Void) null);

    }

    /**
     * Unregister device from Unified Push Server.
     * 
     * if the device isn't registered onFailure will be called
     * 
     * @param context Android application context
     * @param callback a callback.
     */
    @Override
    public void unregister(final Context context, final Callback<Void> callback) {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {

                try {

                    if ((deviceToken == null) || (deviceToken.trim().equals(""))) {
                        throw new IllegalStateException(DEVICE_ALREADY_UNREGISTERED);
                    }

                    if (gcm == null) {
                        gcm = gcmProvider.get(context);
                    }

                    gcm.unregister();
                    
                    HttpProvider provider = httpProviderProvider.get(deviceRegistryURL, TIMEOUT);
                    setPasswordAuthentication(variantId, secret, provider);

                    try {
                        provider.delete(deviceToken);
                        deviceToken = "";
                        Log.i(TAG, "unsetting device token");
                        setRegistrationId(context, deviceToken);
                        return null;
                    } catch (HttpException ex) {
                        return ex;
                    }

                } catch (Exception ex) {
                    return ex;
                }

            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(result);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    /**
     * Gets the current registration id for application on GCM service.
     * <p>
     * If result is empty, the registration has failed.
     * 
     * @param context the application context
     * 
     * @return registration id, or empty string if the registration is not
     *         complete.
     */
    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion
                || isRegistrationExpired(context)) {
            Log.v(TAG, "App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }

    /**
     * Send a confirmation the message was opened
     * 
     * @param metricsMessage The id of the message received
     * @param callback a callback.
     */
    public void sendMetrics(final UnifiedPushMetricsMessage metricsMessage,
            final Callback<UnifiedPushMetricsMessage> callback) {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {

                try {

                    if ((metricsMessage.getMessageId() == null) || (metricsMessage.getMessageId().trim().equals(""))) {
                        throw new IllegalStateException("Message ID cannot be null or blank");
                    }

                    HttpProvider provider = httpProviderProvider.get(metricsURL, TIMEOUT);
                    setPasswordAuthentication(variantId, secret, provider);

                    try {
                        provider.put(metricsMessage.getMessageId(), "");
                        return null;
                    } catch (HttpException ex) {
                        return ex;
                    }

                } catch (Exception ex) {
                    return ex;
                }

            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    callback.onSuccess(metricsMessage);
                } else {
                    callback.onFailure(result);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(AeroGearGCMPushRegistrar.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Checks if the registration has expired.
     * 
     * To avoid the scenario where the device sends the registration to the
     * server but the server loses it, the app developer may choose to
     * re-register after REGISTRATION_EXPIRY_TIME_MS.
     * 
     * @return true if the registration has expired.
     */
    private boolean isRegistrationExpired(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Stores the registration id, app versionCode, and expiration time in the
     * application's {@code SharedPreferences}.
     * 
     * @param context application's context.
     * @param regId registration id
     */
    private void setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis()
                + REGISTRATION_EXPIRY_TIME_MS;

        Log.v(TAG, "Setting registration expiry time to "
                + new Timestamp(expirationTime));
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }

    public void setPasswordAuthentication(final String username, final String password, final HttpProvider provider) {
        provider.setDefaultHeader(BASIC_HEADER, getHashedAuth(username, password.toCharArray()));
    }

    private String getHashedAuth(String username, char[] password) {
        StringBuilder headerValueBuilder = new StringBuilder(AUTHORIZATION_METHOD).append(" ");
        String unhashedCredentials = new StringBuilder(username).append(":").append(password).toString();
        String hashedCrentials = Base64.encodeToString(unhashedCredentials.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);
        return headerValueBuilder.append(hashedCrentials).toString();
    }

}
