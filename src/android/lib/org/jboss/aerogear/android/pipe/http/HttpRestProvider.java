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
package org.jboss.aerogear.android.pipe.http;

import android.text.TextUtils;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.aerogear.android.core.Provider;

import android.util.Log;
import org.jboss.aerogear.android.pipe.util.UrlUtils;

/**
 * These are tuned for AeroGear, assume the body is String data, and that the
 * headers don't do anything funny.
 */
public final class HttpRestProvider implements HttpProvider {

    private static final String TAG = HttpRestProvider.class.getSimpleName();
    private final URL url;
    private final Map<String, String> defaultHeaders = new HashMap<String, String>();
    private final Integer timeout;
    private final static java.net.CookieManager cm = new java.net.CookieManager();

    static {
        java.net.CookieHandler.setDefault(cm);
    }
    /**
     * The get method of this provider optionally takes a String which is the id
     * in a restful URL
     * ex http://example.com/data/$id.
     */
    private final Provider<HttpURLConnection> connectionPreparer = new Provider<HttpURLConnection>() {
        @Override
        public HttpURLConnection get(Object... in) {
            String id = null;
            if (in != null) {
                id = (String) in[0];
            }

            URL resourceURL = HttpRestProvider.this.url;

            if (id != null) {
                resourceURL = UrlUtils.appendToBaseURL(HttpRestProvider.this.url, id);
            }

            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection) resourceURL
                        .openConnection();

            } catch (IOException ex) {
                Log.e(TAG, String.format("Failed to open %s", resourceURL
                        .toString()), ex);
                throw new RuntimeException(ex);
            }

            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            for (Entry<String, String> entry : defaultHeaders.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry
                        .getValue());
            }

            return urlConnection;

        }
    };

    public HttpRestProvider(URL url) {
        this.url = url;
        this.timeout = 0;
    }

    public HttpRestProvider(URL url, Integer timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl() {
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody get() throws HttpException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = prepareConnection();
            return getHeaderAndBody(urlConnection);

        } catch (IOException e) {
            Log.e(TAG, "Error on GET of " + url, e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody post(String data) throws RuntimeException {
        return post(data.getBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody post(byte[] data) throws RuntimeException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = prepareConnection();
            urlConnection.setRequestMethod("POST");
            addBodyRequest(urlConnection, data);
            return getHeaderAndBody(urlConnection);

        } catch (IOException e) {
            Log.e(TAG, "Error on POST of " + url, e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody put(String id, String data) throws RuntimeException {
        return put(id, data.getBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody put(String id, byte[] data) throws RuntimeException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = prepareConnection(id);
            urlConnection.setRequestMethod("PUT");
            addBodyRequest(urlConnection, data);

            return getHeaderAndBody(urlConnection);
        } catch (IOException e) {
            Log.e(TAG, "Error on PUT of " + url, e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody delete(String id) throws RuntimeException {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = prepareConnection(id);
            urlConnection.setRequestMethod("DELETE");
            return getHeaderAndBody(urlConnection);
        } catch (IOException e) {
            Log.e(TAG, "Error on DELETE of " + url, e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void addBodyRequest(HttpURLConnection urlConnection, byte[] data)
            throws IOException {

        urlConnection.setDoOutput(true);

        if (data != null) {
            OutputStream out = new BufferedOutputStream(urlConnection
                    .getOutputStream());
            out.write(data);
            out.flush();
        }

    }

    private HttpURLConnection prepareConnection() throws IOException {
        return prepareConnection(null);
    }

    private HttpURLConnection prepareConnection(String id) {
        HttpURLConnection connection = connectionPreparer.get(id);
        connection.setReadTimeout(timeout);
        connection.setConnectTimeout(timeout);
        return connection;
    }

    @Override
    public void setDefaultHeader(String headerName, String headerValue) {
        defaultHeaders.put(headerName, headerValue);
    }

    private HeaderAndBody getHeaderAndBody(HttpURLConnection urlConnection)
            throws IOException {

        int statusCode = urlConnection.getResponseCode();
        HeaderAndBody result;
        Map<String, List<String>> headers;
        byte[] responseData;

        switch (statusCode) {
        case HttpURLConnection.HTTP_OK:
        case HttpURLConnection.HTTP_CREATED:
            InputStream in = new BufferedInputStream(urlConnection
                    .getInputStream());

            responseData = readBytes(in);

            break;

        case HttpURLConnection.HTTP_NO_CONTENT:
            responseData = new byte[0];

            break;

        default:
            InputStream err = new BufferedInputStream(urlConnection
                    .getErrorStream());

            byte[] errData = readBytes(err);
            Map<String, List<String>> errorListHeaders = urlConnection.getHeaderFields();
            Map<String, String> errorHeaders = new HashMap<String, String>();

            for (String header : errorListHeaders.keySet()) {

                String comma = "";
                StringBuilder errorHeaderBuilder = new StringBuilder();

                for (String errorHeader : errorListHeaders.get(header)) {
                    errorHeaderBuilder.append(comma).append(errorHeader);
                    comma = ",";
                }

                errorHeaders.put(header, errorHeaderBuilder.toString());

            }

            throw new HttpException(errData, statusCode, errorHeaders);

        }

        headers = urlConnection.getHeaderFields();
        result = new HeaderAndBody(responseData, new HashMap<String, Object>(
                headers.size()));

        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            result.setHeader(header.getKey(), TextUtils.join(",", header.getValue()));
        }

        return result;

    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

}
