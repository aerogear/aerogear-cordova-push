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

import org.jboss.aerogear.android.unifiedpush.PushConfiguration;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * A Push Configuration which builds {@link AeroGearGCMPushRegistrar} instances.
 */
public class AeroGearGCMPushConfiguration extends PushConfiguration<AeroGearGCMPushConfiguration> {

    private final UnifiedPushConfig pushConfig = new UnifiedPushConfig();

    /**
     * RegistryURL is the URL of the 3rd party application server
     * 
     * @return the current pushServerURI
     */
    public URI getPushServerURI() {
        return this.pushConfig.getPushServerURI();
    }

    /**
     * RegistryURL is the URL of the 3rd party application server
     * 
     * @param pushServerURI a new URI
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration setPushServerURI(URI pushServerURI) {
        this.pushConfig.setPushServerURI(pushServerURI);
        return this;
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
     * @param senderIds the new sender Ids to set.
     * @return the current configuration.
     */
    public AeroGearGCMPushConfiguration setSenderIds(String... senderIds) {
        this.pushConfig.setSenderIds(senderIds);
        return this;
    }

    /**
     * SenderIds is a collection of all GCM sender Id elements registered for
     * this application.
     * 
     * @param senderId a new sender Id to add to the current set of senderIds.
     * @return the current configuration.
     */
    public AeroGearGCMPushConfiguration addSenderId(String senderId) {
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
     * ID of the Variant from the AeroGear UnifiedPush Server.
     * 
     * @param variantID the new variantID
     * @return the current configuration
     */
    public AeroGearGCMPushConfiguration setVariantID(String variantID) {
        this.pushConfig.setVariantID(variantID);
        return this;
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
     * Secret of the Variant from the AeroGear UnifiedPush Server.
     * 
     * @param secret the new secret
     * @return the current configuration
     */
    public AeroGearGCMPushConfiguration setSecret(String secret) {
        this.pushConfig.setSecret(secret);
        return this;
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
    public AeroGearGCMPushConfiguration setDeviceToken(String deviceToken) {
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
    public AeroGearGCMPushConfiguration setDeviceType(String deviceType) {
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
    public AeroGearGCMPushConfiguration setOperatingSystem(String operatingSystem) {
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
    public AeroGearGCMPushConfiguration setAlias(String alias) {
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
    public AeroGearGCMPushConfiguration setCategories(List<String> categories) {
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
    public AeroGearGCMPushConfiguration addCategories(List<String> categories) {
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
    public AeroGearGCMPushConfiguration setCategories(String... categories) {
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
    public AeroGearGCMPushConfiguration addCategory(String category) {
        this.pushConfig.addCategory(category);
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
