/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.TeleportEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Bed Nuker", category=ModuleCategory.PLAYER)
public final class BedNukerModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Normal);
    private final Property<Boolean> keep = new Property<Boolean>("Keep Break Progress When Out Of Range", false);
    private final Property<Boolean> throughWalls = new Property<Boolean>("Through Walls", false);
    private final Property<Boolean> emptySurrounding = new Property<Boolean>("Empty Surrounding", true, () -> this.throughWalls.getValue() == false);
    private final Property<Boolean> rotations = new Property<Boolean>("Rotate", true);
    private final Property<Boolean> importantRotationsOnly = new Property<Boolean>("Only Rotate at Start and Stop", true);
    private final Property<Boolean> whitelistOwnBed = new Property<Boolean>("Whitelist Own Bed", true);
    private final Property<Boolean> slowDownInAir = new Property<Boolean>("Slow Down In Air", false);
    private final Property<Boolean> movementCorrection = new Property<Boolean>("Movement Fix", true);
    private Vector3d block;
    private Vector3d lastBlock;
    private Vector3d home;
    private int delay;
    private boolean down;
    private float damage;
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        --this.delay;
        if (this.delay > 0) {
            return;
        }
        if (this.block == null || Util.mc.thePlayer.getDistance(this.block.getX(), this.block.getY(), this.block.getZ()) > 4.0 || this.getBlock(this.block.getX(), this.block.getY(), this.block.getZ()) instanceof BlockAir) {
            this.updateBlock();
            if (this.down) {
                Util.mc.gameSettings.keyBindAttack.setPressed(false);
                this.down = false;
            }
            if (this.block == null) {
                return;
            }
        }
        this.destroy();
    };
    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        double distance = Util.mc.thePlayer.getDistance(event.getPosX(), event.getPosY(), event.getPosZ());
        if (distance > 40.0) {
            this.home = new Vector3d(event.getPosX(), event.getPosY(), event.getPosZ());
        }
    };

    private void updateBlock() {
        if (this.block != null && !(this.getBlock(this.block.x, this.block.y, this.block.z) instanceof BlockAir) && !(Util.mc.thePlayer.getDistance(this.block.x, this.block.y - (double)Util.mc.thePlayer.getEyeHeight(), this.block.z) > 9.0)) {
            return;
        }
        if (this.lastBlock != null && !this.keep.getValue().booleanValue()) {
            Util.mc.playerController.curBlockDamageMP = 0.0f;
        }
        this.lastBlock = this.block;
        this.block = this.findBlock();
    }

    private Vector3d findBlock() {
        if (this.home != null && Util.mc.thePlayer.getDistanceSq(this.home.getX(), this.home.getY(), this.home.getZ()) < 1225.0 && this.whitelistOwnBed.getValue().booleanValue()) {
            return null;
        }
        int beds = 0;
        for (int x = -5; x <= 5; ++x) {
            for (int y = -5; y <= 5; ++y) {
                for (int z = -5; z <= 5; ++z) {
                    Block block = this.getBlockRelativeToPlayer(x, y, z);
                    Vector3d position = new Vector3d(Util.mc.thePlayer.posX + (double)x, Util.mc.thePlayer.posY + (double)y, Util.mc.thePlayer.posZ + (double)z);
                    if (!(block instanceof BlockBed) || ++beds <= 1) continue;
                    if (!this.throughWalls.getValue().booleanValue()) {
                        BlockPos blockPos;
                        Vector2f rot = this.calculateRotation(position);
                        MovingObjectPosition mop = this.rayCast(rot, 4.5f);
                        if (mop == null) continue;
                        Vec3 vec3 = new Vec3(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY - (double)Util.mc.thePlayer.getEyeHeight(), Util.mc.thePlayer.posZ);
                        if (mop.hitVec.distanceTo(vec3) > 4.5 || !(blockPos = mop.getBlockPos()).equals(new BlockPos(position.getX(), position.getY(), position.getZ()))) {
                            continue;
                        }
                    } else if (this.emptySurrounding.getValue().booleanValue()) {
                        Vector3d addVec = position;
                        double hardness = Double.MAX_VALUE;
                        boolean empty = false;
                        for (int addX = -4; addX <= 4; ++addX) {
                            for (int addY = 0; addY <= 1; ++addY) {
                                for (int addZ = -4; addZ <= 4; ++addZ) {
                                    double possibleHardness;
                                    Block possibleBlock = this.getBlock(position.getX() + (double)addX, position.getY() + (double)addY, position.getZ() + (double)addZ);
                                    if (possibleBlock instanceof BlockBed || empty || Util.mc.thePlayer.getDistance(position.getX() + (double)addX, position.getY() + (double)addY, position.getZ() + (double)addZ) > 4.5 || this.getNeighbours(position.add(new Vector3d(addX, addY, addZ))).stream().noneMatch(neighbour -> neighbour instanceof BlockBed)) continue;
                                    if (possibleBlock instanceof BlockAir || possibleBlock instanceof BlockLiquid) {
                                        empty = true;
                                        continue;
                                    }
                                    if (Util.mc.thePlayer.getDistance(position.getX() + (double)addX, position.getY() + (double)addY - (double)Util.mc.thePlayer.getEyeHeight(), position.getZ() + (double)addZ) > 4.5 || !((possibleHardness = (double)possibleBlock.getBlockHardness(Util.mc.theWorld, new BlockPos(position.getX() + (double)addX, position.getY() + (double)addY, position.getZ() + (double)addZ))) < hardness)) continue;
                                    hardness = possibleHardness;
                                    addVec = position.add(new Vector3d(addX, addY, addZ));
                                }
                            }
                        }
                        if (!empty) {
                            if (addVec.equals(position)) {
                                return null;
                            }
                            return addVec;
                        }
                    }
                    return position;
                }
            }
        }
        return null;
    }

    private List<Block> getNeighbours(Vector3d blockPos) {
        ArrayList<Block> neighbours = new ArrayList<Block>();
        for (EnumFacing enumFacing : EnumFacing.values()) {
            if (enumFacing == EnumFacing.UP) continue;
            Vector3d neighbourPos = blockPos.add(new Vector3d(enumFacing.getDirectionVec().getX(), enumFacing.getDirectionVec().getY(), enumFacing.getDirectionVec().getZ()));
            neighbours.add(this.getBlock(neighbourPos));
        }
        return neighbours;
    }

    private void destroy() {
        boolean slowDown = this.slowDownInAir.getValue();
        boolean ground = Util.mc.thePlayer.onGround;
        if (!slowDown) {
            Util.mc.thePlayer.onGround = true;
        }
        BlockPos blockPos = new BlockPos(this.block.getX(), this.block.getY(), this.block.getZ());
        switch (((Mode)((Object)this.mode.getValue())).ordinal()) {
            case 0: {
                this.rotate();
                this.damage = Util.mc.playerController.curBlockDamageMP;
                Util.mc.thePlayer.swingItem();
                Util.mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
                Util.mc.thePlayer.swingItem();
                Util.mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
                this.block = null;
                this.delay = 20;
                Util.mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
                break;
            }
            case 1: {
                this.damage = Util.mc.playerController.curBlockDamageMP;
                this.rotate();
                Util.mc.gameSettings.keyBindAttack.setPressed(true);
                this.down = true;
            }
        }
        Util.mc.thePlayer.onGround = ground;
    }

    private void rotate() {
        BlockPos blockPos = new BlockPos(this.block.getX(), this.block.getY(), this.block.getZ());
        float blockHardness = this.getBlock(blockPos).getPlayerRelativeBlockHardness(Util.mc.thePlayer, Util.mc.theWorld, blockPos);
        if (this.importantRotationsOnly.getValue().booleanValue() && Util.mc.playerController.curBlockDamageMP != 0.0f && (double)Util.mc.playerController.curBlockDamageMP <= (double)(1.0f - blockHardness) - 0.001) {
            return;
        }
        if (!this.rotations.getValue().booleanValue()) {
            return;
        }
        RotationProcess.setRotations(this.getRotations(), 10.0, this.movementCorrection.getValue() != false ? MovementFix.NORMAL : MovementFix.OFF);
    }

    private Vector2f getRotations() {
        return this.calculateRotation(new Vector3d(Math.floor(this.block.getX()) + 0.5 + (Math.random() - 0.5) / 4.0, Math.floor(this.block.getY()) + 0.1, Math.floor(this.block.getZ()) + 0.5 + (Math.random() - 0.5) / 4.0));
    }

    private Vector2f calculateRotation(Vector3d target) {
        double x = target.getX() - Util.mc.thePlayer.posX;
        double y = target.getY() - (Util.mc.thePlayer.posY + (double)Util.mc.thePlayer.getEyeHeight());
        double z = target.getZ() - Util.mc.thePlayer.posZ;
        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new Vector2f(yaw, pitch);
    }

    private MovingObjectPosition rayCast(Vector2f rotation, float range) {
        Vec3 eyes = Util.mc.thePlayer.getPositionEyes(1.0f);
        Vec3 rotationVector = Util.mc.thePlayer.getVectorForRotation(rotation.y, rotation.x);
        Vec3 forward = eyes.addVector(rotationVector.xCoord * (double)range, rotationVector.yCoord * (double)range, rotationVector.zCoord * (double)range);
        return Util.mc.theWorld.rayTraceBlocks(eyes, forward, false, false, true);
    }

    private Block getBlock(double x, double y, double z) {
        return Util.mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    private Block getBlock(Vector3d pos) {
        return this.getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    private Block getBlock(BlockPos pos) {
        return Util.mc.theWorld.getBlockState(pos).getBlock();
    }

    private Block getBlockRelativeToPlayer(int x, int y, int z) {
        return this.getBlock(Util.mc.thePlayer.posX + (double)x, Util.mc.thePlayer.posY + (double)y, Util.mc.thePlayer.posZ + (double)z);
    }

    @Override
    public void onEnable() {
        this.block = null;
        this.damage = 0.0f;
        this.delay = 0;
        this.down = false;
    }

    @Override
    public void onDisable() {
        this.block = null;
        if (this.down) {
            Util.mc.gameSettings.keyBindAttack.setPressed(false);
            this.down = false;
        }
    }

    private static enum Mode {
        Instant,
        Normal;

    }
}

