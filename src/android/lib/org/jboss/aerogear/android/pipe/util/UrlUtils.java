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
package org.jboss.aerogear.android.pipe.util;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class UrlUtils {

    private static final String TAG = UrlUtils.class.getSimpleName();

    private UrlUtils() {
    }

    /**
     * 
     * Append the base url with endpoint
     * 
     * @param baseURL the base url
     * @param endpoint the string to append to the base url
     * @return a new url baseUrl + endpoint
     * @throws IllegalArgumentException if baseUrl+endpoint is not a real url.
     */
    public static URL appendToBaseURL(final URL baseURL, String endpoint) {
        try {
            String baseString = baseURL.toString();
            if (endpoint.isEmpty()) {
                return baseURL;
            } else if (!baseString.endsWith("/") && !endpoint.startsWith("/")) {
                baseString += "/";
            } else if (baseString.endsWith("/") && endpoint.startsWith("/")) {
                endpoint = endpoint.replaceFirst("/", "");
            }
            return new URL(baseString + endpoint);
        } catch (MalformedURLException ex) {
            String message = "Could not append " + endpoint + " to " + baseURL.toString();
            Log.e(TAG, message, ex);
            throw new IllegalArgumentException(message, ex);
        }
    }

    /**
     * 
     * Append the base url with the query.
     * 
     * @param baseURL the base url to append a query to
     * @param query the query to append.
     * 
     * @return a new url baseUrl + endpoint
     * @throws IllegalArgumentException if baseUrl+endpoint is not a real url.
     */
    public static URL appendQueryToBaseURL(final URL baseURL, String query) {

        if (query == null || query.isEmpty()) {
            return baseURL;
        }

        try {
            String baseString = baseURL.toString();
            if (baseString.endsWith("/")) {
                baseString = baseString.replaceAll("/$", "");
            }

            if (!query.startsWith("?")) {
                query = URLEncoder.encode(query, "UTF-8");
                query = "?" + query;
            } else {
                query = query.replaceFirst("[?]", "");
                query = URLEncoder.encode(query, "UTF-8");
                query = "?" + query;
            }

            return new URL(baseString + query);
        } catch (MalformedURLException ex) {
            String message = "Could not append " + query + " to " + baseURL.toString();
            Log.e(TAG, message, ex);
            throw new IllegalArgumentException(message, ex);
        } catch (UnsupportedEncodingException ex) {
            String message = "UTF-8 is not a supported encoding";
            Log.e(TAG, message, ex);
            throw new IllegalStateException(message, ex);
        }
    }

}
