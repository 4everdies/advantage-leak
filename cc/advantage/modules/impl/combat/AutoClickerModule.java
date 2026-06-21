/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(label="Auto Clicker", category=ModuleCategory.COMBAT)
public final class AutoClickerModule
extends Module {
    private final Property<Boolean> left = new Property<Boolean>("Left Click", true);
    private final NumberProperty lminCPS = new NumberProperty("Left Min CPS", 10.0, () -> this.left.getValue(), 1.0, 20.0, 1.0);
    private final NumberProperty lmaxCPS = new NumberProperty("Left Max CPS", 12.0, () -> this.left.getValue(), 1.0, 20.0, 1.0);
    private final Property<Boolean> breakBlocks = new Property<Boolean>("Break Blocks", true, () -> this.left.getValue());
    private final Property<Boolean> right = new Property<Boolean>("Right Click", true);
    private final NumberProperty rminCPS = new NumberProperty("Right Min CPS", 10.0, () -> this.right.getValue(), 1.0, 20.0, 1.0);
    private final NumberProperty rmaxCPS = new NumberProperty("Right Max CPS", 12.0, () -> this.right.getValue(), 1.0, 20.0, 1.0);
    private long leftLastClick = 0L;
    private long rightLastClick = 0L;
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        this.handleRightClick();
        this.handleLeftClick();
    };

    private void handleRightClick() {
        if (this.right.getValue().booleanValue() && Util.mc.gameSettings.keyBindUseItem.isKeyDown()) {
            long currentTime = System.currentTimeMillis();
            int minCPS = ((Double)this.rminCPS.getValue()).intValue();
            int maxCPS = ((Double)this.rmaxCPS.getValue()).intValue();
            int cps = ThreadLocalRandom.current().nextInt(minCPS, maxCPS + 1);
            long delay = 1000 / cps;
            if (currentTime - this.rightLastClick >= delay) {
                Util.mc.rightClickMouse();
                this.rightLastClick = currentTime;
            }
        }
    }

    private void handleLeftClick() {
        if (this.left.getValue().booleanValue() && Util.mc.gameSettings.keyBindAttack.isKeyDown()) {
            if (this.breakBlocks.getValue().booleanValue() && Util.mc.objectMouseOver != null && Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            int minCPS = ((Double)this.lminCPS.getValue()).intValue();
            int maxCPS = ((Double)this.lmaxCPS.getValue()).intValue();
            int cps = ThreadLocalRandom.current().nextInt(minCPS, maxCPS + 1);
            long delay = 1000 / cps;
            if (currentTime - this.leftLastClick >= delay) {
                Util.mc.clickMouse();
                Util.mc.leftClickCounter = 0;
                this.leftLastClick = currentTime;
            }
        }
    }

    @Override
    public void onEnable() {
        this.leftLastClick = 0L;
        this.rightLastClick = 0L;
    }
}

