/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.ai.EntityAIBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAITasks {
    private static final Logger logger = LogManager.getLogger();
    private final List<EntityAITaskEntry> taskEntries = Lists.newArrayList();
    private final List<EntityAITaskEntry> executingTaskEntries = Lists.newArrayList();
    private int tickCount;
    private final int tickRate = 3;

    public void addTask(int priority, EntityAIBase task) {
        this.taskEntries.add(new EntityAITaskEntry(priority, task));
    }

    public void removeTask(EntityAIBase task) {
        Iterator<EntityAITaskEntry> iterator2 = this.taskEntries.iterator();
        while (iterator2.hasNext()) {
            EntityAITaskEntry entityaitasks$entityaitaskentry = iterator2.next();
            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;
            if (entityaibase != task) continue;
            if (this.executingTaskEntries.contains(entityaitasks$entityaitaskentry)) {
                entityaibase.resetTask();
                this.executingTaskEntries.remove(entityaitasks$entityaitaskentry);
            }
            iterator2.remove();
        }
    }

    public void onUpdateTasks() {
        if (this.tickCount++ % this.tickRate == 0) {
            for (EntityAITaskEntry entityaitasks$entityaitaskentry : this.taskEntries) {
                boolean flag = this.executingTaskEntries.contains(entityaitasks$entityaitaskentry);
                if (flag) {
                    if (this.canUse(entityaitasks$entityaitaskentry) && this.canContinue(entityaitasks$entityaitaskentry)) continue;
                    entityaitasks$entityaitaskentry.action.resetTask();
                    this.executingTaskEntries.remove(entityaitasks$entityaitaskentry);
                }
                if (!this.canUse(entityaitasks$entityaitaskentry) || !entityaitasks$entityaitaskentry.action.shouldExecute()) continue;
                entityaitasks$entityaitaskentry.action.startExecuting();
                this.executingTaskEntries.add(entityaitasks$entityaitaskentry);
            }
        } else {
            Iterator<EntityAITaskEntry> iterator1 = this.executingTaskEntries.iterator();
            while (iterator1.hasNext()) {
                EntityAITaskEntry entityaitasks$entityaitaskentry1 = iterator1.next();
                if (this.canContinue(entityaitasks$entityaitaskentry1)) continue;
                entityaitasks$entityaitaskentry1.action.resetTask();
                iterator1.remove();
            }
        }
        for (EntityAITaskEntry entityaitasks$entityaitaskentry2 : this.executingTaskEntries) {
            entityaitasks$entityaitaskentry2.action.updateTask();
        }
    }

    private boolean canContinue(EntityAITaskEntry taskEntry) {
        boolean flag = taskEntry.action.continueExecuting();
        return flag;
    }

    private boolean canUse(EntityAITaskEntry taskEntry) {
        for (EntityAITaskEntry entityaitasks$entityaitaskentry : this.taskEntries) {
            if (entityaitasks$entityaitaskentry == taskEntry || !(taskEntry.priority >= entityaitasks$entityaitaskentry.priority ? !this.areTasksCompatible(taskEntry, entityaitasks$entityaitaskentry) && this.executingTaskEntries.contains(entityaitasks$entityaitaskentry) : !entityaitasks$entityaitaskentry.action.isInterruptible() && this.executingTaskEntries.contains(entityaitasks$entityaitaskentry))) continue;
            return false;
        }
        return true;
    }

    private boolean areTasksCompatible(EntityAITaskEntry taskEntry1, EntityAITaskEntry taskEntry2) {
        return (taskEntry1.action.getMutexBits() & taskEntry2.action.getMutexBits()) == 0;
    }

    class EntityAITaskEntry {
        public EntityAIBase action;
        public int priority;

        public EntityAITaskEntry(int priorityIn, EntityAIBase task) {
            this.priority = priorityIn;
            this.action = task;
        }
    }
}

