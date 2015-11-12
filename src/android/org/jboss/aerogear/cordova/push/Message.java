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
    private Bundle userData;

    public Message() {}

    public Message(String alert, String sound, int badge, String aerogearPushId, Bundle userData) {
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
                extras.getBundle("userData"));
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

    public Bundle getUserData() {
        return userData;
    }

    public void setUserData(Bundle userData) {
        this.userData = userData;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle(5);
        bundle.putString("alert", alert);
        bundle.putString("sound", sound);
        bundle.putString("badge", String.valueOf(badge));
        bundle.putString("aerogear-push-id", aerogearPushId);
        bundle.putBundle("userData", userData);
        return bundle;
    }
}
