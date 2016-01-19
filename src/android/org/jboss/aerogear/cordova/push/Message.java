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

import android.os.Bundle;
import org.jboss.aerogear.android.core.RecordId;
import java.util.UUID;

/**
 * Message
 */
public class Message {
    @RecordId
    private UUID id;
    private String alert;
    private String sound;
    private int badge = -1;
    private String aerogearPushId;
    private String userData;

    public Message() {}

    public Message(String alert, String sound, int badge, String aerogearPushId, String userData) {
        this.alert = alert;
        this.sound = sound;
        this.badge = badge;
        this.aerogearPushId = aerogearPushId;
        this.userData = userData;
    }

    public Message(Bundle extras) {
        this(extras.getString("alert"), extras.getString("sound"),
                Integer.parseInt(extras.getString("badge")),
                extras.getString("aerogear-push-id"),
                extras.getString("user-data"));
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getAerogearPushId() {
        return aerogearPushId;
    }

    public void setAerogearPushId(String aerogearPushId) {
        this.aerogearPushId = aerogearPushId;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle(5);
        bundle.putString("alert", alert);
        bundle.putString("sound", sound);
        bundle.putString("badge", String.valueOf(badge));
        bundle.putString("aerogear-push-id", aerogearPushId);
        bundle.putString("user-data", userData);
        return bundle;
    }
}