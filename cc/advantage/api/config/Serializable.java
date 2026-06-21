/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.config;

import com.google.gson.JsonObject;

public interface Serializable {
    public JsonObject save();

    public void load(JsonObject var1);
}
