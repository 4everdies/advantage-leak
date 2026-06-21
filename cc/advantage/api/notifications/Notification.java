/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.notifications;

import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.api.notifications.NotificationManager;
import cc.advantage.api.notifications.NotificationType;
import cc.advantage.modules.impl.visuals.NotificationsModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.animations.Animation;
import cc.advantage.utils.render.animations.impl.DecelerateAnimation;
import java.awt.Color;
import lombok.Generated;

public class Notification
extends Util {
    private final NotificationType notificationType;
    private final String title;
    private final String description;
    private final float time;
    private final Timer timerUtil;
    private final Animation animation;

    public Notification(NotificationType type, String title, String description) {
        this(type, title, description, NotificationManager.getToggleTime());
    }

    public Notification(NotificationType type, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long)(time * 1000.0f);
        this.timerUtil = new Timer();
        this.notificationType = type;
        this.animation = new DecelerateAnimation(250, 1.0);
    }

    public void draw(float x, float y, float width, float height) {
        CustomFontRenderer fr = NotificationsModule.customFont.getValue() != false ? FontUtils.getFont("advantage") : FontUtils.getFont("mc");
        RenderUtils.drawRect(x, y, width, height, new Color(0.1f, 0.1f, 0.1f, 0.9f));
        float percentage = Math.min((float)this.timerUtil.getTime() / this.getTime(), 1.0f);
        RenderUtils.drawRect(x + width * percentage, y + height - 1.0f, width - width * percentage, 1.0f, ColorProcess.getColor());
        FontUtils.getFont("icon").drawString(this.getNotificationType().getIcon(), x + 3.0f, y + FontUtils.getFont("icon").getMiddleOfBox(height) + 1.0f, this.getNotificationType().getColor().getRGB());
        fr.drawString(this.getTitle(), x + 7.0f + (float)FontUtils.getFont("icon").getStringWidth(this.getNotificationType().getIcon()), y + 4.0f, Color.WHITE.getRGB());
        fr.drawString(this.getDescription(), x + 7.0f + (float)FontUtils.getFont("icon").getStringWidth(this.getNotificationType().getIcon()), y + 8.5f + (float)FontUtils.getFont("mc").getHeight(), Color.WHITE.getRGB());
    }

    @Generated
    public NotificationType getNotificationType() {
        return this.notificationType;
    }

    @Generated
    public String getTitle() {
        return this.title;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public float getTime() {
        return this.time;
    }

    @Generated
    public Timer getTimerUtil() {
        return this.timerUtil;
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }
}

