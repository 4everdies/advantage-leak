/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.mc;

import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.EnumFacingOffset;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.mc.MovementUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class PlayerUtils
extends Util {
    private static final HashMap<Integer, Integer> GOOD_POTIONS = new HashMap<Integer, Integer>(){
        {
            this.put(6, 1);
            this.put(10, 2);
            this.put(11, 3);
            this.put(21, 4);
            this.put(22, 5);
            this.put(23, 6);
            this.put(5, 7);
            this.put(1, 8);
            this.put(12, 9);
            this.put(14, 10);
            this.put(3, 11);
            this.put(13, 12);
        }
    };

    public static Block block(double x, double y, double z) {
        return PlayerUtils.mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static Block blockRelativeToPlayer(double offsetX, double offsetY, double offsetZ) {
        return PlayerUtils.block(PlayerUtils.mc.thePlayer.posX + offsetX, PlayerUtils.mc.thePlayer.posY + offsetY, PlayerUtils.mc.thePlayer.posZ + offsetZ);
    }

    public static boolean onLiquid() {
        boolean onLiquid = false;
        AxisAlignedBB playerBB = PlayerUtils.mc.thePlayer.getEntityBoundingBox();
        WorldClient world = PlayerUtils.mc.theWorld;
        int y = (int)playerBB.offset((double)0.0, (double)-0.01, (double)0.0).minY;
        for (int x = MathHelper.floor_double(playerBB.minX); x < MathHelper.floor_double(playerBB.maxX) + 1; ++x) {
            for (int z = MathHelper.floor_double(playerBB.minZ); z < MathHelper.floor_double(playerBB.maxZ) + 1; ++z) {
                Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block == null || block instanceof BlockAir) continue;
                if (!(block instanceof BlockLiquid)) {
                    return false;
                }
                onLiquid = true;
            }
        }
        return onLiquid;
    }

    public static boolean isBlockUnder() {
        return PlayerUtils.isBlockUnder(10.0, true);
    }

    public static boolean isBlockUnder(double height, boolean boundingBox) {
        if (boundingBox) {
            AxisAlignedBB bb = PlayerUtils.mc.thePlayer.getEntityBoundingBox().offset(0.0, -height, 0.0);
            if (!PlayerUtils.mc.theWorld.getCollidingBoundingBoxes(PlayerUtils.mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        } else {
            int offset = 0;
            while ((double)offset < height) {
                if (PlayerUtils.blockRelativeToPlayer(0.0, -offset, 0.0).isFullBlock()) {
                    return true;
                }
                ++offset;
            }
        }
        return false;
    }

    public static boolean goodPotion(int id) {
        return GOOD_POTIONS.containsKey(id);
    }

    public static Vec3 getPlacePossibility(double offsetX, double offsetY, double offsetZ) {
        return PlayerUtils.getPlacePossibility(offsetX, offsetY, offsetZ, null);
    }

    public static Vec3 getPlacePossibility(double offsetX, double offsetY, double offsetZ, Integer plane) {
        ArrayList<Vec3> possibilities = new ArrayList<Vec3>();
        int range = (int)(5.0 + (Math.abs(offsetX) + Math.abs(offsetZ)));
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    Block block = PlayerUtils.blockRelativeToPlayer(x, y, z);
                    if (block.isReplaceable(PlayerUtils.mc.theWorld, new BlockPos(PlayerUtils.mc.thePlayer.posX + (double)x, PlayerUtils.mc.thePlayer.posY + (double)y, PlayerUtils.mc.thePlayer.posZ + (double)z))) continue;
                    for (int x2 = -1; x2 <= 1; x2 += 2) {
                        possibilities.add(new Vec3(PlayerUtils.mc.thePlayer.posX + (double)x + (double)x2, PlayerUtils.mc.thePlayer.posY + (double)y, PlayerUtils.mc.thePlayer.posZ + (double)z));
                    }
                    for (int y2 = -1; y2 <= 1; y2 += 2) {
                        possibilities.add(new Vec3(PlayerUtils.mc.thePlayer.posX + (double)x, PlayerUtils.mc.thePlayer.posY + (double)y + (double)y2, PlayerUtils.mc.thePlayer.posZ + (double)z));
                    }
                    for (int z2 = -1; z2 <= 1; z2 += 2) {
                        possibilities.add(new Vec3(PlayerUtils.mc.thePlayer.posX + (double)x, PlayerUtils.mc.thePlayer.posY + (double)y, PlayerUtils.mc.thePlayer.posZ + (double)z + (double)z2));
                    }
                }
            }
        }
        possibilities.removeIf(vec3 -> PlayerUtils.mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) > 5.0 || !PlayerUtils.block(vec3.xCoord, vec3.yCoord, vec3.zCoord).isReplaceable(PlayerUtils.mc.theWorld, new BlockPos(vec3.xCoord, vec3.yCoord, vec3.zCoord)));
        if (possibilities.isEmpty()) {
            return null;
        }
        if (plane != null) {
            possibilities.removeIf(vec3 -> Math.floor(vec3.yCoord + 1.0) != (double)plane.intValue());
        }
        possibilities.sort(Comparator.comparingDouble(vec3 -> {
            double d0 = PlayerUtils.mc.thePlayer.posX + offsetX - vec3.xCoord;
            double d1 = PlayerUtils.mc.thePlayer.posY - 1.0 + offsetY - vec3.yCoord;
            double d2 = PlayerUtils.mc.thePlayer.posZ + offsetZ - vec3.zCoord;
            return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
        }));
        return possibilities.isEmpty() ? null : (Vec3)possibilities.get(0);
    }

    public Vec3 getPlacePossibility() {
        return PlayerUtils.getPlacePossibility(0.0, 0.0, 0.0);
    }

    public static EnumFacingOffset getEnumFacing(Vec3 position) {
        return PlayerUtils.getEnumFacing(position, false);
    }

    public static EnumFacingOffset getEnumFacing(Vec3 position, boolean downwards) {
        ArrayList<EnumFacingOffset> possibleFacings = new ArrayList<EnumFacingOffset>();
        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (PlayerUtils.block(position.xCoord, position.yCoord, position.zCoord + (double)z2).isReplaceable(PlayerUtils.mc.theWorld, new BlockPos(position.xCoord, position.yCoord, position.zCoord + (double)z2))) continue;
            if (z2 < 0) {
                possibleFacings.add(new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0.0, 0.0, z2)));
                continue;
            }
            possibleFacings.add(new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0.0, 0.0, z2)));
        }
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (PlayerUtils.block(position.xCoord + (double)x2, position.yCoord, position.zCoord).isReplaceable(PlayerUtils.mc.theWorld, new BlockPos(position.xCoord + (double)x2, position.yCoord, position.zCoord))) continue;
            if (x2 > 0) {
                possibleFacings.add(new EnumFacingOffset(EnumFacing.WEST, new Vec3(x2, 0.0, 0.0)));
                continue;
            }
            possibleFacings.add(new EnumFacingOffset(EnumFacing.EAST, new Vec3(x2, 0.0, 0.0)));
        }
        possibleFacings.sort(Comparator.comparingDouble(enumFacing -> {
            double enumFacingRotations = Math.toDegrees(Math.atan2(enumFacing.getOffset().zCoord, enumFacing.getOffset().xCoord)) % 360.0;
            double rotations = RotationProcess.rotations.x % 360.0f + 90.0f;
            return Math.abs(MathUtils.wrappedDifference(enumFacingRotations, rotations));
        }));
        if (!possibleFacings.isEmpty()) {
            return (EnumFacingOffset)possibleFacings.get(0);
        }
        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (PlayerUtils.block(position.xCoord, position.yCoord + (double)y2, position.zCoord).isReplaceable(PlayerUtils.mc.theWorld, new BlockPos(position.xCoord, position.yCoord + (double)y2, position.zCoord))) continue;
            if (y2 < 0) {
                return new EnumFacingOffset(EnumFacing.UP, new Vec3(0.0, y2, 0.0));
            }
            if (!downwards) continue;
            return new EnumFacingOffset(EnumFacing.DOWN, new Vec3(0.0, y2, 0.0));
        }
        return null;
    }

    public static Block blockAheadOfPlayer(double offsetXZ, double offsetY) {
        return PlayerUtils.blockRelativeToPlayer(-Math.sin(MovementUtils.direction()) * offsetXZ, offsetY, Math.cos(MovementUtils.direction()) * offsetXZ);
    }

    public static boolean insideBlock() {
        if (PlayerUtils.mc.thePlayer.ticksExisted < 5) {
            return false;
        }
        EntityPlayerSP player = PlayerUtils.mc.thePlayer;
        WorldClient world = PlayerUtils.mc.theWorld;
        AxisAlignedBB bb = player.getEntityBoundingBox();
        for (int x = MathHelper.floor_double(bb.minX); x < MathHelper.floor_double(bb.maxX) + 1; ++x) {
            for (int y = MathHelper.floor_double(bb.minY); y < MathHelper.floor_double(bb.maxY) + 1; ++y) {
                for (int z = MathHelper.floor_double(bb.minZ); z < MathHelper.floor_double(bb.maxZ) + 1; ++z) {
                    AxisAlignedBB boundingBox;
                    Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block == null || block instanceof BlockAir || (boundingBox = block.getCollisionBoundingBox(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)))) == null || !player.getEntityBoundingBox().intersectsWith(boundingBox)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isReplaceable(BlockPos blockPos) {
        return PlayerUtils.isReplaceable(PlayerUtils.mc.theWorld.getBlockState(blockPos).getBlock());
    }

    public static boolean isReplaceable(Block block) {
        if (!block.getMaterial().isReplaceable()) {
            return false;
        }
        if (!(block instanceof BlockSnow)) {
            return true;
        }
        return !(block.getBlockBoundsMaxY() > 0.125);
    }

    public static boolean isInteractable(BlockPos blockPos) {
        return PlayerUtils.isInteractable(PlayerUtils.mc.theWorld.getBlockState(blockPos).getBlock());
    }

    public static boolean isInteractable(Block block) {
        if (block instanceof BlockContainer) {
            return true;
        }
        if (block instanceof BlockWorkbench) {
            return true;
        }
        if (block instanceof BlockAnvil) {
            return true;
        }
        if (block instanceof BlockBed) {
            return true;
        }
        if (block instanceof BlockDoor && block.getMaterial() != Material.iron) {
            return true;
        }
        if (block instanceof BlockTrapDoor) {
            return true;
        }
        if (block instanceof BlockFenceGate) {
            return true;
        }
        if (block instanceof BlockFence) {
            return true;
        }
        if (block instanceof BlockButton) {
            return true;
        }
        if (block instanceof BlockLever) {
            return true;
        }
        return block instanceof BlockJukebox;
    }

    public static boolean isSolid(Block block) {
        if (block instanceof BlockStairs) {
            return false;
        }
        if (block instanceof BlockSlab) {
            return false;
        }
        if (block instanceof BlockEndPortalFrame) {
            return false;
        }
        if (block instanceof BlockEndPortal) {
            return false;
        }
        if (block instanceof BlockVine) {
            return false;
        }
        if (block instanceof BlockPumpkin) {
            return false;
        }
        if (block instanceof BlockCactus) {
            return false;
        }
        if (block instanceof BlockBush) {
            return false;
        }
        if (block instanceof BlockFalling) {
            return false;
        }
        if (block instanceof BlockWeb) {
            return false;
        }
        if (block instanceof BlockPane) {
            return false;
        }
        if (block instanceof BlockCarpet) {
            return false;
        }
        if (block instanceof BlockSnow) {
            return false;
        }
        if (block instanceof BlockFence) {
            return false;
        }
        if (block instanceof BlockFenceGate) {
            return false;
        }
        if (block instanceof BlockWall) {
            return false;
        }
        if (block instanceof BlockLadder) {
            return false;
        }
        if (block instanceof BlockTorch) {
            return false;
        }
        if (block instanceof BlockRedstoneWire) {
            return false;
        }
        if (block instanceof BlockRedstoneDiode) {
            return false;
        }
        if (block instanceof BlockBasePressurePlate) {
            return false;
        }
        if (block instanceof BlockTripWire) {
            return false;
        }
        if (block instanceof BlockTripWireHook) {
            return false;
        }
        if (block instanceof BlockRailBase) {
            return false;
        }
        if (block instanceof BlockSlime) {
            return false;
        }
        return !(block instanceof BlockTNT);
    }

    public static class PredictProcess {
        public final Vec3 position;
        public final float fallDistance;
        private final boolean onGround;
        public final boolean isCollidedHorizontally;
        public final EntityPlayerSP player;
        public int tick;

        public PredictProcess(Vec3 position, float fallDistance, boolean onGround, boolean isCollidedHorizontally, EntityPlayerSP player) {
            this.position = position;
            this.fallDistance = fallDistance;
            this.onGround = onGround;
            this.isCollidedHorizontally = isCollidedHorizontally;
            this.player = player;
        }

        public PredictProcess(Vec3 position, float fallDistance, boolean onGround, boolean isCollidedHorizontally) {
            this.position = position;
            this.fallDistance = fallDistance;
            this.onGround = onGround;
            this.isCollidedHorizontally = isCollidedHorizontally;
            this.player = Util.mc.thePlayer;
        }
    }
}

