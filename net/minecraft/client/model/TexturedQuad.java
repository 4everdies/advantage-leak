/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.src.Config;
import net.minecraft.util.Vec3;
import net.optifine.shaders.SVertexFormat;
import org.lwjgl.opengl.GL11;

public class TexturedQuad {
    public PositionTextureVertex[] vertexPositions;
    public int nVertices;
    private boolean invertNormal;
    private static Boolean immediateModelDraw;

    public TexturedQuad(PositionTextureVertex[] vertices) {
        this.vertexPositions = vertices;
        this.nVertices = vertices.length;
    }

    public TexturedQuad(PositionTextureVertex[] vertices, int texcoordU1, int texcoordV1, int texcoordU2, int texcoordV2, float textureWidth, float textureHeight) {
        this(vertices);
        float f = 0.0f / textureWidth;
        float f1 = 0.0f / textureHeight;
        vertices[0] = vertices[0].setTexturePosition((float)texcoordU2 / textureWidth - f, (float)texcoordV1 / textureHeight + f1);
        vertices[1] = vertices[1].setTexturePosition((float)texcoordU1 / textureWidth + f, (float)texcoordV1 / textureHeight + f1);
        vertices[2] = vertices[2].setTexturePosition((float)texcoordU1 / textureWidth + f, (float)texcoordV2 / textureHeight - f1);
        vertices[3] = vertices[3].setTexturePosition((float)texcoordU2 / textureWidth - f, (float)texcoordV2 / textureHeight - f1);
    }

    public void flipFace() {
        PositionTextureVertex[] apositiontexturevertex = new PositionTextureVertex[this.vertexPositions.length];
        for (int i = 0; i < this.vertexPositions.length; ++i) {
            apositiontexturevertex[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
        }
        this.vertexPositions = apositiontexturevertex;
    }

    public void draw(WorldRenderer renderer, float scale) {
        Vec3 vec3 = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[0].vector3D);
        Vec3 vec31 = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[2].vector3D);
        Vec3 vec32 = vec31.crossProduct(vec3).normalize();
        float f = (float)vec32.xCoord;
        float f1 = (float)vec32.yCoord;
        float f2 = (float)vec32.zCoord;
        if (this.invertNormal) {
            f = -f;
            f1 = -f1;
            f2 = -f2;
        }
        if (!Config.isShaders() && TexturedQuad.shouldUseImmediateModelDraw()) {
            this.drawImmediate(scale, f, f1, f2);
            return;
        }
        if (Config.isShaders()) {
            renderer.begin(7, SVertexFormat.defVertexFormatTextured);
        } else {
            renderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        }
        for (int i = 0; i < 4; ++i) {
            PositionTextureVertex positiontexturevertex = this.vertexPositions[i];
            renderer.pos(positiontexturevertex.vector3D.xCoord * (double)scale, positiontexturevertex.vector3D.yCoord * (double)scale, positiontexturevertex.vector3D.zCoord * (double)scale).tex(positiontexturevertex.texturePositionX, positiontexturevertex.texturePositionY).normal(f, f1, f2).endVertex();
        }
        Tessellator.getInstance().draw();
    }

    static boolean shouldUseImmediateModelDraw() {
        if (Boolean.getBoolean("advantage.forceModelArrays")) {
            return false;
        }
        if (Boolean.getBoolean("advantage.forceModelImmediate")) {
            return true;
        }
        if (immediateModelDraw == null) {
            String glInfo = (TexturedQuad.getGlString(7936) + " " + TexturedQuad.getGlString(7937) + " " + TexturedQuad.getGlString(7938)).toLowerCase();
            boolean intel = glInfo.contains("intel");
            boolean legacyHd = glInfo.contains("hd graphics") && !glInfo.contains("uhd") && !glInfo.contains("iris");
            immediateModelDraw = intel && legacyHd;
        }
        return immediateModelDraw;
    }

    private void drawImmediate(float scale, float normalX, float normalY, float normalZ) {
        GL11.glBegin(7);
        GL11.glNormal3f(normalX, normalY, normalZ);
        for (int i = 0; i < 4; ++i) {
            PositionTextureVertex vertex = this.vertexPositions[i];
            GL11.glTexCoord2f(vertex.texturePositionX, vertex.texturePositionY);
            GL11.glVertex3d(vertex.vector3D.xCoord * (double)scale, vertex.vector3D.yCoord * (double)scale, vertex.vector3D.zCoord * (double)scale);
        }
        GL11.glEnd();
    }

    private static String getGlString(int name) {
        try {
            String value = GL11.glGetString(name);
            return value == null ? "" : value;
        }
        catch (RuntimeException ignored) {
            return "";
        }
    }
}

