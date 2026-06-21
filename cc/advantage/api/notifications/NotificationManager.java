/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.notifications;

import cc.advantage.Advantage;
import cc.advantage.api.notifications.Notification;
import cc.advantage.api.notifications.NotificationType;
import cc.advantage.modules.impl.visuals.NotificationsModule;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;

public class NotificationManager {
    private static float toggleTime = 2.0f;
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList();

    public static void post(NotificationType type, String title, String description) {
        NotificationManager.post(new Notification(type, title, description));
    }

    public static void post(NotificationType type, String title, String description, float time) {
        NotificationManager.post(new Notification(type, title, description, time));
    }

    private static void post(Notification notification) {
        if (Advantage.INSTANCE.getModuleManager().getModule(NotificationsModule.class).isEnabled()) {
            notifications.add(notification);
        }
    }

    @Generated
    public static float getToggleTime() {
        return toggleTime;
    }

    @Generated
    public static void setToggleTime(float toggleTime) {
        NotificationManager.toggleTime = toggleTime;
    }

    @Generated
    public static CopyOnWriteArrayList<Notification> getNotifications() {
        return notifications;
    }
}

