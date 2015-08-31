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
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import org.jboss.aerogear.android.core.ConfigurationProvider;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushJsonConfiguration;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushJsonConfigurationProvider;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushConfigurationProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the factory and accessors for PushRegistrars
 */
public class RegistrarManager {

    private static final Map<String, PushRegistrar> registrars = new HashMap<String, PushRegistrar>();

    private static final List<MessageHandler> mainThreadHandlers = new ArrayList<MessageHandler>();
    private static final List<MessageHandler> backgroundThreadHandlers = new ArrayList<MessageHandler>();

    private static Map<Class<? extends PushConfiguration<?>>, ConfigurationProvider<?>> configurationProviderMap = new HashMap<Class<? extends PushConfiguration<?>>, ConfigurationProvider<?>>();

    private static OnPushRegistrarCreatedListener onPushRegistrarCreatedListener = new OnPushRegistrarCreatedListener() {

        @Override
        public void onPushRegistrarCreated(PushConfiguration<?> configuration, PushRegistrar registrar) {
            registrars.put(configuration.getName(), registrar);
        }
    };

    static {
        RegistrarManager.registerConfigurationProvider(AeroGearGCMPushConfiguration.class,
                new AeroGearGCMPushConfigurationProvider());
        RegistrarManager.registerConfigurationProvider(AeroGearGCMPushJsonConfiguration.class,
                new AeroGearGCMPushJsonConfigurationProvider());
    }

    /**
     * 
     * This will add a new Configuration that this Manager can build
     * Configurations for.
     * 
     * @param <CFG> the actual Configuration type
     * @param configurationClass the class of configuration to be registered
     * @param provider the instance which will provide the configuration.
     */
    public static <CFG extends PushConfiguration<CFG>> void registerConfigurationProvider(
            Class<CFG> configurationClass, ConfigurationProvider<CFG> provider) {
        configurationProviderMap.put(configurationClass, provider);
    }

    /**
     * Begins a new fluent configuration stanza.
     * 
     * @param <CFG> the Configuration type.
     * @param name an identifier which will be used to fetch the
     *            PushRegistrar after configuration is finished.
     * @param pushConfigurationClass the class of the configuration
     *            type.
     * 
     * @return a {@link PushConfiguration} which can be used to build a
     *         AuthenticationModule object.
     */
    public static <CFG extends PushConfiguration<CFG>> CFG config(String name, Class<CFG> pushConfigurationClass) {

        @SuppressWarnings("unchecked")
        ConfigurationProvider<? extends PushConfiguration<CFG>> provider = (ConfigurationProvider<? extends PushConfiguration<CFG>>) configurationProviderMap
                .get(pushConfigurationClass);

        if (provider == null) {
            throw new IllegalArgumentException("Configuration not registered!");
        }

        return provider.newConfiguration()
                .setName(name)
                .addOnPushRegistrarCreatedListener(onPushRegistrarCreatedListener);

    }

    /**
     * Fetches a named registrar
     * 
     * @param name the name of the {@link PushRegistrar} given in {@link RegistrarManager#config(java.lang.String, java.lang.Class)
     * }
     * 
     * @return the named {@link PushRegistrar} or null
     */
    public static PushRegistrar getRegistrar(String name) {
        return registrars.get(name);
    }

    /**
     * 
     * When a push message is received, all main thread handlers will be
     * notified on the main(UI) thread. This is very convenient for Activities
     * and Fragments.
     * 
     * @param handler a handler to added to the list of handlers to be notified.
     */
    public static void registerMainThreadHandler(MessageHandler handler) {
        mainThreadHandlers.add(handler);
    }

    /**
     * 
     * When a push message is received, all background thread handlers will be
     * notified on a non UI thread. This should be used by classes which need to
     * update internal state or preform some action which doesn't change the UI.
     * 
     * @param handler a handler to added to the list of handlers to be notified.
     */
    public static void registerBackgroundThreadHandler(MessageHandler handler) {
        backgroundThreadHandlers.add(handler);
    }

    /**
     * 
     * This will remove the given handler from the collection of main thread
     * handlers. This MUST be called when a Fragment or activity is backgrounded
     * via onPause.
     * 
     * @param handler a new handler
     */
    public static void unregisterMainThreadHandler(MessageHandler handler) {
        mainThreadHandlers.remove(handler);
    }

    /**
     * 
     * This will remove the given handler from the collection of background
     * thread handlers.
     * 
     * @param handler a new handler
     */
    public static void unregisterBackgroundThreadHandler(MessageHandler handler) {
        backgroundThreadHandlers.remove(handler);
    }

    /**
     * 
     * This will deliver an intent to all registered handlers. See {@link PushConstants} for information on how messages will be routed.
     * 
     * @param context the application's context
     * @param message the message to pass
     * @param defaultHandler a default handler is a handler which will be called
     *            if there are no other handlers registered. May be null
     */
    public static void notifyHandlers(final Context context, final Intent message, final MessageHandler defaultHandler) {

        if (backgroundThreadHandlers.isEmpty() && mainThreadHandlers.isEmpty()
                && defaultHandler != null) {
            new Thread(new Runnable() {
                public void run() {

                    if (message.getBooleanExtra(PushConstants.ERROR, false)) {
                        defaultHandler.onError();
                    } else if (message.getBooleanExtra(PushConstants.DELETED, false)) {
                        defaultHandler.onDeleteMessage(context, message.getExtras());
                    } else {
                        defaultHandler.onMessage(context, message.getExtras());
                    }

                }
            }).start();
        }

        for (final MessageHandler handler : backgroundThreadHandlers) {
            new Thread(new Runnable() {
                public void run() {

                    if (message.getBooleanExtra(PushConstants.ERROR, false)) {
                        handler.onError();
                    } else if (message.getBooleanExtra(PushConstants.DELETED, false)) {
                        handler.onDeleteMessage(context, message.getExtras());
                    } else {
                        handler.onMessage(context, message.getExtras());
                    }

                }
            }).start();
        }

        Looper main = Looper.getMainLooper();

        for (final MessageHandler handler : mainThreadHandlers) {
            new Handler(main).post(new Runnable() {
                @Override
                public void run() {
                    if (message.getBooleanExtra(PushConstants.ERROR, false)) {
                        handler.onError();
                    } else if (message.getBooleanExtra(PushConstants.DELETED, false)) {
                        handler.onDeleteMessage(context, message.getExtras());
                    } else {
                        handler.onMessage(context, message.getExtras());
                    }
                }
            });
        }
    }

    /**
     * 
     * This will deliver an intent to all registered handlers. Currently it is
     * GCM centric, but this will be changed in the future.
     * 
     * See: <a href="https://issues.jboss.org/browse/AGDROID-84">AGDROID-84</a>
     * 
     * @param context the application's context
     * @param message the message to pass
     */
    protected static void notifyHandlers(final Context context,
            final Intent message) {
        notifyHandlers(context, message, null);
    }

}
