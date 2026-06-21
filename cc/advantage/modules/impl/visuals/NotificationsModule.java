/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.api.notifications.Notification;
import cc.advantage.api.notifications.NotificationManager;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.DragUtils;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.animations.Animation;
import cc.advantage.utils.render.animations.Direction;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.ScaledResolution;

@ModuleInfo(label="Notifications", category=ModuleCategory.VISUALS)
public final class NotificationsModule
extends Module {
    public static Property<Boolean> customFont = new Property<Boolean>("Custom Font", false);
    public static Property<Boolean> toggleNotifications = new Property<Boolean>("Toggle Notifications", true);
    private static boolean positionInitialized = false;
    private static final int NOTIFICATION_HEIGHT = 30;
    private static final int NOTIFICATION_SPACING = 5;
    private static final int MIN_NOTIFICATION_WIDTH = 150;
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> this.renderNotifications();
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> this.renderNotifications();

    private void initializePosition(ScaledResolution sr) {
        if (!positionInitialized && !DragUtils.components.containsKey("Notifications")) {
            DragUtils.components.put("Notifications", new DragUtils.DraggableComponent(sr.getScaledWidth() - 150 - 10, 10.0));
            positionInitialized = true;
        }
    }

    private void renderNotifications() {
        ScaledResolution sr = new ScaledResolution(Util.mc);
        CustomFontRenderer fr = customFont.getValue() != false ? FontUtils.getCurrentFont() : FontUtils.getFont("mc");
        NotificationManager.setToggleTime(2.0f);
        float yOffset = 0.0f;
        int maxWidth = 150;
        for (Notification notification : NotificationManager.getNotifications()) {
            int titleWidth = fr.getStringWidth(notification.getTitle());
            int descWidth = fr.getStringWidth(notification.getDescription());
            int iconWidth = FontUtils.getFont("icon").getStringWidth(notification.getNotificationType().getIcon());
            int notificationWidth = Math.max(titleWidth, descWidth) + iconWidth + 15;
            maxWidth = Math.max(maxWidth, notificationWidth);
        }
        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long)notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);
            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }
            animation.setDuration(250);
            int titleWidth = fr.getStringWidth(notification.getTitle());
            int descWidth = fr.getStringWidth(notification.getDescription());
            int iconWidth = FontUtils.getFont("icon").getStringWidth(notification.getNotificationType().getIcon());
            int notificationWidth = Math.max(titleWidth, descWidth) + iconWidth + 15;
            float slideOffset = (float)(maxWidth - notificationWidth) * (1.0f - animation.getOutput().floatValue());
            float x = (float)(sr.getScaledWidth() - notificationWidth - 2) + slideOffset;
            float y = (float)sr.getScaledHeight() - yOffset - 30.0f - 2.0f;
            notification.draw(x, y, notificationWidth, 30.0f);
            yOffset += 35.0f * animation.getOutput().floatValue();
        }
    }
}

