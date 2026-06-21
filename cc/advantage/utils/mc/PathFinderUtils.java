/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.mc;

import cc.advantage.utils.Util;
import cc.advantage.utils.misc.PathFinder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class PathFinderUtils
extends Util {
    public static List<Vec3> computePath(Vec3 from, Vec3 to, boolean exact) {
        return PathFinderUtils.computePath(from, to, exact, 9.5);
    }

    public static List<Vec3> computePath(Vec3 from, Vec3 to, boolean exact, double step) {
        BlockPos blockPos = new BlockPos(from);
        IBlockState state = PathFinderUtils.mc.theWorld.getBlockState(blockPos);
        if (state == null) {
            return null;
        }
        Block block = state.getBlock();
        if (block == null) {
            return null;
        }
        if (!PathFinderUtils.canPassThroughMaterial(block)) {
            from = from.addVector(0.0, 1.0, 0.0);
        }
        PathFinder pathFinder = new PathFinder(from, to);
        pathFinder.compute();
        int i = 0;
        Vec3 lastLoc = null;
        Vec3 lastDashLoc = null;
        ArrayList<Vec3> path = new ArrayList<Vec3>();
        ArrayList<Vec3> pathFinderPath = pathFinder.getPath();
        for (Vec3 pathElm : pathFinderPath) {
            if (i == 0 || i == pathFinderPath.size() - 1) {
                path.add(pathElm.addVector(0.5, 0.0, 0.5));
                lastDashLoc = pathElm;
            } else {
                boolean canContinue = true;
                if (pathElm.squareDistanceTo(lastDashLoc) > step * step) {
                    canContinue = false;
                } else {
                    double smallX = Math.min(lastDashLoc.xCoord, pathElm.xCoord);
                    double smallY = Math.min(lastDashLoc.yCoord, pathElm.yCoord);
                    double smallZ = Math.min(lastDashLoc.zCoord, pathElm.zCoord);
                    double bigX = Math.max(lastDashLoc.xCoord, pathElm.xCoord);
                    double bigY = Math.max(lastDashLoc.yCoord, pathElm.yCoord);
                    double bigZ = Math.max(lastDashLoc.zCoord, pathElm.zCoord);
                    int x = (int)smallX;
                    block1: while ((double)x <= bigX) {
                        int y = (int)smallY;
                        while ((double)y <= bigY) {
                            int z = (int)smallZ;
                            while ((double)z <= bigZ) {
                                if (!PathFinder.checkPositionValidity(x, y, z, false)) {
                                    canContinue = false;
                                    break block1;
                                }
                                ++z;
                            }
                            ++y;
                        }
                        ++x;
                    }
                }
                if (!canContinue) {
                    path.add(lastLoc.addVector(0.5, 0.0, 0.5));
                    lastDashLoc = lastLoc;
                }
            }
            lastLoc = pathElm;
            ++i;
        }
        if (exact) {
            path.add(to);
        }
        return path;
    }

    private static boolean canPassThroughMaterial(Block block) {
        Material material = block.getMaterial();
        return material == Material.air || material == Material.plants || material == Material.vine || block == Blocks.ladder || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.wall_sign || block == Blocks.standing_sign;
    }
}

