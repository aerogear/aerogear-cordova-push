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

import java.util.Collection;
import java.util.HashSet;
import org.jboss.aerogear.android.core.Config;

/**
 * The configuration builder for push registrars.
 * 
 * @param <CONFIGURATION> The concrete implementation of the PushConfiguration
 */
public abstract class PushConfiguration<CONFIGURATION extends PushConfiguration> implements Config<CONFIGURATION> {

    private String name;

    private Collection<OnPushRegistrarCreatedListener> listeners = new HashSet<OnPushRegistrarCreatedListener>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CONFIGURATION setName(String name) {
        this.name = name;
        return (CONFIGURATION) this;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     * 
     * @return the current collection.
     */
    public Collection<OnPushRegistrarCreatedListener> getOnAuthenticationCreatedListeners() {
        return listeners;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     * 
     * @param listener new listener to add to the collection
     * @return this configuration
     */
    public CONFIGURATION addOnPushRegistrarCreatedListener(OnPushRegistrarCreatedListener listener) {
        this.listeners.add(listener);
        return (CONFIGURATION) this;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     * 
     * @param listeners new collection to replace the current one
     * @return this configuration
     */
    public CONFIGURATION setOnPushRegistrarCreatedListeners(Collection<OnPushRegistrarCreatedListener> listeners) {
        listeners.addAll(listeners);
        return (CONFIGURATION) this;
    }

    /**
     * 
     * Creates a {@link PushRegistrar} based on the current configuration and
     * notifies all listeners
     * 
     * @return An {@link PushRegistrar} based on this configuration
     * 
     * @throws IllegalStateException if the {@link PushRegistrar} can not be
     *             constructed.
     * 
     */
    public final PushRegistrar asRegistrar() {

        PushRegistrar registrar = buildRegistrar();
        for (OnPushRegistrarCreatedListener listener : getOnAuthenticationCreatedListeners()) {
            listener.onPushRegistrarCreated(this, registrar);
        }
        return registrar;
    }

    /**
     * 
     * Validates configuration parameters and returns a PushRegistrar
     * instance.
     * 
     * @return An {@link PushRegistrar} based on this configuration
     * 
     */
    protected abstract PushRegistrar buildRegistrar();

}
