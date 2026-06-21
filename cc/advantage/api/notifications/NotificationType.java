/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.notifications;

import java.awt.Color;
import lombok.Generated;

public enum NotificationType {
    SUCCESS(new Color(20, 250, 90), "o"),
    DISABLE(new Color(255, 30, 30), "p"),
    INFO(Color.WHITE, "m"),
    WARNING(Color.YELLOW, "r");

    private final Color color;
    private final String icon;

    @Generated
    public Color getColor() {
        return this.color;
    }

    @Generated
    public String getIcon() {
        return this.icon;
    }

    @Generated
    private NotificationType(Color color, String icon) {
        this.color = color;
        this.icon = icon;
    }
}

