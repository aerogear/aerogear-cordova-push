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

/**
 * 
 * This class contains static strings which are used as constant keys on Intents
 * passed into the Registrars#notifyHandlers methods.
 * 
 */
public final class PushConstants {

    /**
     * Intents with this key indicate that some error occurred. There may be
     * extra information in the Intent.
     * 
     * {@link RegistrarManager#notifyHandlers(android.content.Context, android.content.Intent, org.jboss.aerogear.android.unifiedpush.MessageHandler) } will pass this message to
     * {@link MessageHandler#onError() }
     */
    public static final String ERROR = "org.jboss.aerogear.android.unifiedpush.ERROR";

    /**
     * Intents with this key encapsulate a push message
     * 
     * {@link RegistrarManager#notifyHandlers(android.content.Context, android.content.Intent, org.jboss.aerogear.android.unifiedpush.MessageHandler) } will pass this message to
     * {@link MessageHandler#onMessage(android.content.Context, android.os.Bundle) }
     */
    public static final String MESSAGE = "org.jboss.aerogear.android.unifiedpush.MESSAGE";

    /**
     * An intent with this key means that the message it encapsulates refers to
     * messages which have been deleted by the push provider.
     * 
     * {@link RegistrarManager#notifyHandlers(android.content.Context, android.content.Intent, org.jboss.aerogear.android.unifiedpush.MessageHandler) } will pass this message to
     * {@link MessageHandler#onDeleteMessage(android.content.Context, android.os.Bundle)  }
     */
    public static final String DELETED = "org.jboss.aerogear.android.unifiedpush.DELETED";

}
