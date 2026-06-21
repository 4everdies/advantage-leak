/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules;

public interface Toggleable {
    public void toggle();

    public void setEnabled(boolean var1);

    public boolean isEnabled();

    public void onEnable();

    public void onDisable();
}

