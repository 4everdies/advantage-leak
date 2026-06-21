/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.utils.misc;

import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PlayerUtils;
import java.util.ArrayList;
import java.util.Comparator;
import lombok.Generated;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBarrier;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockSnowBlock;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public final class PathFinder
extends Util {
    private final ArrayList<Hub> hubsToWork = new ArrayList();
    private ArrayList<Vec3> path = new ArrayList();
    private final ArrayList<Hub> hubs = new ArrayList();
    private final double minDistanceSquared = 9.5;
    private final boolean nearest = true;
    private final Vec3 startVec3;
    private final Vec3 endVec3;
    private static final Vec3[] flatCardinalDirections = new Vec3[]{new Vec3(1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(0.0, 0.0, -1.0)};

    public PathFinder(Vec3 startVec3, Vec3 endVec3) {
        this.startVec3 = startVec3.addVector(0.0, 0.0, 0.0).floor();
        this.endVec3 = endVec3.addVector(0.0, 0.0, 0.0).floor();
    }

    public ArrayList<Vec3> getPath() {
        return this.path;
    }

    public void compute() {
        this.compute(1000, 4);
    }

    public void compute(int loops, int depth) {
        this.path.clear();
        this.hubsToWork.clear();
        ArrayList<Vec3> initPath = new ArrayList<Vec3>();
        initPath.add(this.startVec3);
        this.hubsToWork.add(new Hub(this.startVec3, null, initPath, this.startVec3.squareDistanceTo(this.endVec3), 0.0, 0.0));
        block0: for (int i = 0; i < loops; ++i) {
            this.hubsToWork.sort(new CompareHub());
            int j = 0;
            if (this.hubsToWork.size() == 0) break;
            for (Hub hub : new ArrayList<Hub>(this.hubsToWork)) {
                Vec3 loc2;
                if (++j > depth) continue block0;
                this.hubsToWork.remove(hub);
                this.hubs.add(hub);
                for (Vec3 direction : flatCardinalDirections) {
                    Vec3 loc = hub.getLoc().add(direction).floor();
                    if (PathFinder.checkPositionValidity(loc, false) && this.addHub(hub, loc, 0.0)) break block0;
                }
                Vec3 loc1 = hub.getLoc().addVector(0.0, 1.0, 0.0).floor();
                if ((!PathFinder.checkPositionValidity(loc1, false) || !this.addHub(hub, loc1, 0.0)) && (!PathFinder.checkPositionValidity(loc2 = hub.getLoc().addVector(0.0, -1.0, 0.0).floor(), false) || !this.addHub(hub, loc2, 0.0))) continue;
                break block0;
            }
        }
        this.hubs.sort(new CompareHub());
        this.path = this.hubs.get(0).getPath();
    }

    public static boolean checkPositionValidity(Vec3 loc, boolean checkGround) {
        return PathFinder.checkPositionValidity((int)loc.xCoord, (int)loc.yCoord, (int)loc.zCoord, checkGround);
    }

    public static boolean checkPositionValidity(int x, int y, int z, boolean checkGround) {
        BlockPos block3 = new BlockPos(x, y - 1, z);
        return !PathFinder.isBlockSolid(new BlockPos(x, y, z)) && !PathFinder.isBlockSolid(new BlockPos(x, y + 1, z)) && (PathFinder.isBlockSolid(block3) || !checkGround) && PathFinder.isSafeToWalkOn(block3);
    }

    private static boolean isBlockSolid(BlockPos blockPos) {
        Block b = PlayerUtils.block(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return b.isFullBlock() || b instanceof BlockSlab || b instanceof BlockStairs || b instanceof BlockCactus || b instanceof BlockChest || b instanceof BlockEnderChest || b instanceof BlockSkull || b instanceof BlockPane || b instanceof BlockFence || b instanceof BlockWall || b instanceof BlockGlass || b instanceof BlockPistonBase || b instanceof BlockPistonExtension || b instanceof BlockPistonMoving || b instanceof BlockStainedGlass || b instanceof BlockTrapDoor || b instanceof BlockEndPortalFrame || b instanceof BlockEndPortal || b instanceof BlockBed || b instanceof BlockWeb || b instanceof BlockBarrier || b instanceof BlockLadder || b instanceof BlockLeaves || b instanceof BlockSnow || b instanceof BlockSnowBlock || b instanceof BlockCarpet || b instanceof BlockDoor || b instanceof BlockVine || b instanceof BlockLilyPad;
    }

    private static boolean isSafeToWalkOn(BlockPos block) {
        Block blockClazz = Minecraft.getMinecraft().theWorld.getBlockState(block).getBlock();
        return !(blockClazz instanceof BlockFence) && !(blockClazz instanceof BlockWall);
    }

    public Hub isHubExisting(Vec3 loc) {
        for (Hub hub : this.hubs) {
            if (hub.getLoc().xCoord != loc.xCoord || hub.getLoc().yCoord != loc.yCoord || hub.getLoc().zCoord != loc.zCoord) continue;
            return hub;
        }
        for (Hub hub : this.hubsToWork) {
            if (hub.getLoc().xCoord != loc.xCoord || hub.getLoc().yCoord != loc.yCoord || hub.getLoc().zCoord != loc.zCoord) continue;
            return hub;
        }
        return null;
    }

    public boolean addHub(Hub parent, Vec3 loc, double cost) {
        Hub existingHub = this.isHubExisting(loc);
        double totalCost = cost;
        if (parent != null) {
            totalCost += parent.getTotalCost();
        }
        if (existingHub == null) {
            if (loc.xCoord == this.endVec3.xCoord && loc.yCoord == this.endVec3.yCoord && loc.zCoord == this.endVec3.zCoord || loc.squareDistanceTo(this.endVec3) <= 9.5) {
                if (parent != null) {
                    this.path.clear();
                    this.path = parent.getPath();
                    this.path.add(loc);
                    return true;
                }
                return false;
            }
            ArrayList<Vec3> path = new ArrayList<Vec3>(parent.getPath());
            path.add(loc);
            this.hubsToWork.add(new Hub(loc, parent, path, loc.squareDistanceTo(this.endVec3), cost, totalCost));
        } else if (existingHub.getCost() > cost) {
            ArrayList<Vec3> path = new ArrayList<Vec3>(parent.getPath());
            path.add(loc);
            existingHub.setLoc(loc);
            existingHub.setParent(parent);
            existingHub.setPath(path);
            existingHub.setSquareDistanceToFromTarget(loc.squareDistanceTo(this.endVec3));
            existingHub.setCost(cost);
            existingHub.setTotalCost(totalCost);
        }
        return false;
    }

    private static class Hub {
        private Vec3 loc;
        private Hub parent;
        private ArrayList<Vec3> path;
        private double squareDistanceToFromTarget;
        private double cost;
        private double totalCost;

        public Hub(Vec3 loc, Hub parent, ArrayList<Vec3> path, double squareDistanceToFromTarget, double cost, double totalCost) {
            this.loc = loc;
            this.parent = parent;
            this.path = path;
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
            this.cost = cost;
            this.totalCost = totalCost;
        }

        @Generated
        public Vec3 getLoc() {
            return this.loc;
        }

        @Generated
        public Hub getParent() {
            return this.parent;
        }

        @Generated
        public ArrayList<Vec3> getPath() {
            return this.path;
        }

        @Generated
        public double getSquareDistanceToFromTarget() {
            return this.squareDistanceToFromTarget;
        }

        @Generated
        public double getCost() {
            return this.cost;
        }

        @Generated
        public double getTotalCost() {
            return this.totalCost;
        }

        @Generated
        public void setLoc(Vec3 loc) {
            this.loc = loc;
        }

        @Generated
        public void setParent(Hub parent) {
            this.parent = parent;
        }

        @Generated
        public void setPath(ArrayList<Vec3> path) {
            this.path = path;
        }

        @Generated
        public void setSquareDistanceToFromTarget(double squareDistanceToFromTarget) {
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
        }

        @Generated
        public void setCost(double cost) {
            this.cost = cost;
        }

        @Generated
        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }
    }

    public static class CompareHub
    implements Comparator<Hub> {
        @Override
        public int compare(Hub o1, Hub o2) {
            return (int)(o1.getSquareDistanceToFromTarget() + o1.getTotalCost() - (o2.getSquareDistanceToFromTarget() + o2.getTotalCost()));
        }
    }
}

