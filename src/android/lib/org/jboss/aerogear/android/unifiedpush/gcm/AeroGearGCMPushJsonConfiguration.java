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
import android.util.Log;
import org.jboss.aerogear.android.unifiedpush.PushConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

/**
 * A Push Configuration which builds {@link org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar} instances.
 */
public class AeroGearGCMPushJsonConfiguration
        extends PushConfiguration<AeroGearGCMPushJsonConfiguration> {

    private static final String TAG = AeroGearGCMPushJsonConfiguration.class.getName();
    private static final String JSON_OBJECT = "android";
    private static final String JSON_URL = "pushServerURL";
    private static final String JSON_SENDER_ID = "senderID";
    private static final String JSON_VARIANT_ID = "variantID";
    private static final String JSON_VARIANT_SECRET = "variantSecret";

    private final UnifiedPushConfig pushConfig = new UnifiedPushConfig();
    private String fileName = "push-config.json";

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * RegistryURL is the URL of the 3rd party application server
     * 
     * @return the current pushServerURI
     */
    public URI getPushServerURI() {
        return this.pushConfig.getPushServerURI();
    }

    /**
     * SenderIds is a collection of all GCM sender Id elements registered for
     * this application.
     * 
     * @return a copy of the current set of senderIds.
     * 
     */
    public Set<String> getSenderIds() {
        return pushConfig.getSenderIds();
    }

    /**
     * SenderIds is a collection of all GCM sender Id elements registered for
     * this application.
     * 
     * @param senderId a new sender Id to add to the current set of senderIds.
     * @return the current configuration.
     */
    public AeroGearGCMPushJsonConfiguration addSenderId(String senderId) {
        this.pushConfig.addSenderId(senderId);
        return this;
    }

    /**
     * ID of the Variant from the AeroGear UnifiedPush Server.
     * 
     * @return the current variant id
     */
    public String getVariantID() {
        return pushConfig.getVariantID();
    }

    /**
     * Secret of the Variant from the AeroGear UnifiedPush Server.
     * 
     * @return the current Secret
     * 
     */
    public String getSecret() {
        return pushConfig.getSecret();
    }

    /**
     * The device token Identifies the device within its Push Network. It is the
     * value = GoogleCloudMessaging.getInstance(context).register(SENDER_ID);
     * 
     * @return the current device token
     * 
     */
    public String getDeviceToken() {
        return pushConfig.getDeviceToken();
    }

    /**
     * The device token Identifies the device within its Push Network. It is the
     * value = GoogleCloudMessaging.getInstance(context).register(SENDER_ID);
     * 
     * @param deviceToken the new device token
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushJsonConfiguration setDeviceToken(String deviceToken) {
        this.pushConfig.setDeviceToken(deviceToken);
        return this;
    }

    /**
     * Device type determines which cloud messaging system will be used by the
     * AeroGear Unified Push Server
     * 
     * Defaults to ANDROID
     * 
     * @return the device type
     */
    public String getDeviceType() {
        return pushConfig.getDeviceType();
    }

    /**
     * Device type determines which cloud messaging system will be used by the
     * AeroGear Unified Push Server.
     * 
     * Defaults to ANDROID
     * 
     * @param deviceType a new device type
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushJsonConfiguration setDeviceType(String deviceType) {
        this.pushConfig.setDeviceType(deviceType);
        return this;
    }

    /**
     * The name of the operating system. Defaults to Android
     * 
     * @return the operating system
     */
    public String getOperatingSystem() {
        return pushConfig.getOperatingSystem();
    }

    /**
     * The name of the operating system. Defaults to Android
     * 
     * @param operatingSystem the new operating system
     * @return the current configuration
     */
    public AeroGearGCMPushJsonConfiguration setOperatingSystem(String operatingSystem) {
        this.pushConfig.setOperatingSystem(operatingSystem);
        return this;
    }

    /**
     * The version of the operating system running.
     * 
     * Defaults to the value provided by android.os.Build.VERSION.RELEASE
     * 
     * @return the current OSversion
     * 
     */
    public String getOsVersion() {
        return pushConfig.getOsVersion();
    }

    /**
     * The Alias is an identifier of the user of the system.
     * 
     * Examples are an email address or a username
     * 
     * @return alias
     * 
     */
    public String getAlias() {
        return pushConfig.getAlias();
    }

    /**
     * The Alias is an identifier of the user of the system.
     * 
     * Examples are an email address or a username
     * 
     * @param alias the new alias
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushJsonConfiguration setAlias(String alias) {
        this.pushConfig.setAlias(alias);
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @return the current categories
     * 
     */
    public List<String> getCategories() {
        return pushConfig.getCategories();
    }

    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @param categories the new categories
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushJsonConfiguration setCategories(List<String> categories) {
        this.pushConfig.setCategories(categories);
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     *
     * @param categories add categories
     * @return the current configuration
     *
     */
    public AeroGearGCMPushJsonConfiguration addCategories(List<String> categories) {
        this.pushConfig.addCategories(categories);
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @param categories the new categories
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushJsonConfiguration setCategories(String... categories) {
        this.pushConfig.setCategories(categories);
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @param category a new category to be added to the current list.
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushJsonConfiguration addCategory(String category) {
        this.pushConfig.addCategory(category);
        return this;
    }

    /**
     *
     * Load push unified push informations from assets/push-config.json
     *
     * <pre>
     * {
     *   "pushServerURL": "pushServerURL (e.g http(s)//host:port/context)",
     *   "android": {
     *     "senderID": "senderID (e.g Google Project ID only for android)",
     *     "variantID": "variantID (e.g. 1234456-234320)",
     *     "variantSecret": "variantSecret (e.g. 1234456-234320)"
     * }
     * </pre>
     *
     * @param context your application's context
     *
     * @return the current configuration
     */
    public AeroGearGCMPushJsonConfiguration loadConfigJson(Context context) {
        InputStream fileStream = null;
        try {
            fileStream = context.getResources().getAssets().open(getFileName());
            int size = fileStream.available();
            byte[] buffer = new byte[size];
            fileStream.read(buffer);
            fileStream.close();
            String json = new String(buffer);

            JSONObject pushConfig = new JSONObject(json);
            JSONObject pushAndroidConfig = pushConfig.getJSONObject(JSON_OBJECT);

            this.pushConfig.setPushServerURI(new URI(pushConfig.getString(JSON_URL)));
            this.pushConfig.addSenderId(pushAndroidConfig.getString(JSON_SENDER_ID));
            this.pushConfig.setVariantID(pushAndroidConfig.getString(JSON_VARIANT_ID));
            this.pushConfig.setSecret(pushAndroidConfig.getString(JSON_VARIANT_SECRET));
        } catch (URISyntaxException e) {
            // It will never happen
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException("An error occurred while parsing the " + getFileName() + ". Please check the file format");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException("An error occurred while parsing the " + getFileName() + ". Please check if the file exists");
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    // Ignore IOException
                }
            }
        }

        return this;
    }

    /**
     * 
     * Protected builder method.
     * 
     * @return A configured AeroGearGCMPushRegistrar
     * 
     * @throws IllegalStateException if pushServerURI, SenderID, Variant or VariantSecret is null or empty.
     */
    @Override
    protected final AeroGearGCMPushRegistrar buildRegistrar() {
        pushConfig.checkRequiredFields();
        return new AeroGearGCMPushRegistrar(pushConfig);
    }

}
