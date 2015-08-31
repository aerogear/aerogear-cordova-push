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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a convenience to wrap up headers from a
 * HTTPResponse with its entity.
 */
public class HeaderAndBody {

    private final byte[] body;

    private final Map<String, Object> headers;

    public HeaderAndBody(byte[] body, Map<String, Object> headers) {
        this.body = Arrays.copyOf(body, body.length);
        this.headers = new HashMap<String, Object>(headers);
    }

    public byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    public Object getHeader(String headerName) {
        return headers.get(headerName);
    }

    public void setHeader(String headerName, Object headerValue) {
        headers.put(headerName, headerValue);
    }

}
