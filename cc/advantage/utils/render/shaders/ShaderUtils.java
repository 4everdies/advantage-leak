/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.shaders;

import cc.advantage.utils.Util;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;

public class ShaderUtils
extends Util {
    private final int programID;
    private final String shadow = "#version 120\n\nuniform sampler2D inTexture;\nuniform sampler2D textureToCheck;\nuniform vec2 texelSize;\nuniform vec2 direction;\nuniform float radius;\n  uniform float strength;\nuniform float weights[256];\n\nvoid main() {\n    vec2 uv = gl_TexCoord[0].st;\n\n    if (direction.y > 0.0 && texture2D(textureToCheck, uv).a != 0.0) {\n        discard;\n    }\n\n    float alpha = texture2D(inTexture, uv).a * weights[0];\n    float weightSum = weights[0];\n\n    for (int i = 1; i <= int(radius); ++i) {\n        vec2 offset = texelSize * direction * float(i);\n        float w = weights[i];\n\n        alpha += texture2D(inTexture, clamp(uv + offset, vec2(0.0), vec2(1.0))).a * w;\n        alpha += texture2D(inTexture, clamp(uv - offset, vec2(0.0), vec2(1.0))).a * w;\n        weightSum += 2.0 * w;\n    }\n\n    alpha = (alpha / weightSum) * strength;\n    gl_FragColor = vec4(0.0, 0.0, 0.0, alpha);\n}\n";
    private final String roundRectTexture = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D textureIn;\nuniform float radius, alpha;\n\nfloat roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) -size, 0.)) - radius;\n}\n\nvoid main() {\n    float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;\n    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);\n}";
    private final String roundRectOutline = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color, outlineColor;\nuniform float radius, outlineThickness;\n\nfloat roundedSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;\n}\n\nvoid main() {\n    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n\n    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n\n    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n\n}";
    private final String roundedRectGradient = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color1, color2, color3, color4;\nuniform float radius;\n\n#define NOISE .5/255.0\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b , 0.0)) - r;\n}\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 st = gl_TexCoord[0].st;\n    vec2 halfSize = rectSize * .5;\n    \n   // use the bottom leftColor as the alpha\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));\n    vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);\n}";
    private final String roundedRect = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color;\nuniform float radius;\nuniform bool blur;\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b, 0.0)) - r;\n}\n\nvoid main() {\n    vec2 rectHalf = rectSize * 0.5;\n    gl_FragColor = vec4(color.rgb, (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1.0, radius))) * color.a);\n}";
    private final String kawaseUpBloom = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\nuniform float strength;\n\nvoid main() {\n  //  if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a > 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    vec3 boosted = (result.rgb / result.a) * strength;\n    gl_FragColor = vec4(boosted, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a),check));\n}";
    private final String kawaseDownBloom = "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum.rgb *= sum.a;\n    sum *= 4.0;\n    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);\n    smp1.rgb *= smp1.a;\n    sum += smp1;\n    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3;\n    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 result = sum / 8.0;\n    gl_FragColor = vec4(result.rgb / result.a, result.a);\n}";
    private final String kawaseUp = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;\n\n    gl_FragColor = vec4(sum.rgb /12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}\n";
    private final String kawaseDown = "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0;\n    sum += texture2D(inTexture, uv - halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    gl_FragColor = vec4(sum.rgb * .125, 1.0);\n}\n";
    private final String gradient = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec4 color1, color2, color3, color4;\n\n#define NOISE .5/255.0\n\nvec3 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4).rgb, texColorAlpha);\n}";
    private final String mainmenu = "uniform float TIME;\nuniform vec2 RESOLUTION;\n\n#define NUM_OCTAVES 6\n\nmat3 rotX(float a) {\n    float c = cos(a);\n    float s = sin(a);\n    return mat3(\n        1, 0, 0,\n        0, c, -s,\n        0, s, c\n    );\n}\n\nmat3 rotY(float a) {\n    float c = cos(a);\n    float s = sin(a);\n    return mat3(\n        c, 0, -s,\n        0, 1, 0,\n        s, 0, c\n    );\n}\n\nfloat random(vec2 pos) {\n    return fract(sin(dot(pos.xy, vec2(12.9898, 78.233))) * 43758.5453123);\n}\n\nfloat noise(vec2 pos) {\n    vec2 i = floor(pos);\n    vec2 f = fract(pos);\n    float a = random(i + vec2(0.0, 0.0));\n    float b = random(i + vec2(1.0, 0.0));\n    float c = random(i + vec2(0.0, 1.0));\n    float d = random(i + vec2(1.0, 1.0));\n    vec2 u = f * f * (3.0 - 2.0 * f);\n    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;\n}\n\nfloat fbm(vec2 pos) {\n    float v = 0.0;\n    float a = 0.5;\n    vec2 shift = vec2(100.0);\n    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));\n    for (int i = 0; i < NUM_OCTAVES; i++) {\n        float dir = mod(float(i), 2.0) > 0.5 ? 1.0 : -1.0;\n        v += a * noise(pos - 0.05 * dir * TIME);\n\n        pos = rot * pos * 2.0 + shift;\n        a *= 0.5;\n    }\n    return v;\n}\n\nvec3 render(in vec2 fragCoord) {\n    vec2 p = (fragCoord * 2.0 - RESOLUTION.xy) / min(RESOLUTION.x, RESOLUTION.y);\n    p -= vec2(12.0, 0.0);\n\n    float time2 = 1.0;\n    vec2 q = vec2(0.0);\n    q.x = fbm(p + 0.00 * time2);\n    q.y = fbm(p + vec2(1.0));\n    vec2 r = vec2(0.0);\n    r.x = fbm(p + 1.0 * q + vec2(1.7, 9.2) + 0.15 * time2);\n    r.y = fbm(p + 1.0 * q + vec2(8.3, 2.8) + 0.126 * time2);\n    float f = fbm(p + r);\n\n    vec3 color = mix(\n        vec3(0.3, 0.3, 0.6),\n        vec3(0.7, 0.7, 0.7),\n        clamp((f * f) * 4.0, 0.0, 1.0)\n    );\n\n    color = mix(\n        color,\n        vec3(0.7, 0.7, 0.7),\n        clamp(length(q), 0.0, 1.0)\n    );\n\n    color = mix(\n        color,\n        vec3(0.4, 0.4, 0.4),\n        clamp(length(r.x), 0.0, 1.0)\n    );\n\n    color = (f * f * f + 0.9 * f * f + 0.8 * f) * color;\n\n    return color * 0.5;\n}\n\nvoid mainImage(out vec4 fragColor, in vec2 fragCoord) {\n    vec3 color = render(fragCoord);\n    fragColor = vec4(color, color.r);\n}\n\nvoid main(void) {\n    mainImage(gl_FragColor, gl_FragCoord.xy);\n}\n";
    private final String gaussianBlur = "#version 120\n\nuniform sampler2D textureIn;\nuniform vec2 texelSize;\nuniform vec2 direction;\nuniform float strength;\nuniform float radius;\nuniform float weights[128];\n\n#define offset (texelSize * direction)\n\nvoid main() {\n    vec2 uv = gl_TexCoord[0].st;\n    vec3 color = texture2D(textureIn, uv).rgb * weights[0];\n\n    for (int i = 1; i < 128; ++i) {\n        if (i > int(radius)) break;\n\n        vec2 delta = float(i) * offset;\n        color += texture2D(textureIn, uv + delta).rgb * weights[i];\n        color += texture2D(textureIn, uv - delta).rgb * weights[i];\n    }\n\n    // apply strength to control blur intensity\n    gl_FragColor = vec4(color * strength, 1.0);\n}\n";
    private final String cape = "#extension GL_OES_standard_derivatives : enable\n\n#ifdef GL_ES\nprecision highp float;\n#endif\n\nuniform float time;\nuniform vec2  resolution;\nuniform float zoom;\n\n#define PI 3.1415926535\n\nmat2 rotate3d(float angle)\n{\n    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));\n}\n\nvoid main()\n{\n    vec2 p = (gl_FragCoord.xy * 2.0 - resolution) / min(resolution.x, resolution.y);\n    p = rotate3d((time * 2.0) * PI) * p;\n    float t;\n    if (sin(time) == 10.0)\n        t = 0.075 / abs(1.0 - length(p));\n    else\n        t = 0.075 / abs(0.4/*sin(time)*/ - length(p));\n    gl_FragColor = vec4(     ( 1. -exp( -vec3(t)  * vec3(0.13*(sin(time)+12.0), p.y*0.7, 3.0) )) , 1.0);\n}";
    private final String glow = "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure, radius;\nuniform float weights[256];\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n\n    float innerAlpha = texture2D(textureIn, gl_TexCoord[0].st).a * weights[0];\n\n    for (float r = 1.0; r <= radius; r ++) {\n        innerAlpha += texture2D(textureIn, gl_TexCoord[0].st + offset * r).a * weights[int(r)];\n        innerAlpha += texture2D(textureIn, gl_TexCoord[0].st - offset * r).a * weights[int(r)];\n    }\n\n    gl_FragColor = vec4(color, mix(innerAlpha, 1.0 - exp(-innerAlpha * exposure), step(0.0, direction.y)));\n}\n";
    private final String outline = "#version 120\n\nuniform vec2 texelSize, direction;\nuniform sampler2D texture;\nuniform float radius;\nuniform vec3 color;\n\n#define offset direction * texelSize\n\nvoid main() {\n    float centerAlpha = texture2D(texture, gl_TexCoord[0].xy).a;\n    float innerAlpha = centerAlpha;\n    for (float r = 1.0; r <= radius; r++) {\n        float alphaCurrent1 = texture2D(texture, gl_TexCoord[0].xy + offset * r).a;\n        float alphaCurrent2 = texture2D(texture, gl_TexCoord[0].xy - offset * r).a;\n\n        innerAlpha += alphaCurrent1 + alphaCurrent2;\n    }\n\n    gl_FragColor = vec4(color, innerAlpha) * step(0.0, -centerAlpha);\n}\n\n\n";

    public ShaderUtils(String fragmentShaderLoc, String vertexShaderLoc) {
        int program = GL20.glCreateProgram();
        try {
            int fragmentShaderID = switch (fragmentShaderLoc) {
                case "shadow" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture;\nuniform sampler2D textureToCheck;\nuniform vec2 texelSize;\nuniform vec2 direction;\nuniform float radius;\n  uniform float strength;\nuniform float weights[256];\n\nvoid main() {\n    vec2 uv = gl_TexCoord[0].st;\n\n    if (direction.y > 0.0 && texture2D(textureToCheck, uv).a != 0.0) {\n        discard;\n    }\n\n    float alpha = texture2D(inTexture, uv).a * weights[0];\n    float weightSum = weights[0];\n\n    for (int i = 1; i <= int(radius); ++i) {\n        vec2 offset = texelSize * direction * float(i);\n        float w = weights[i];\n\n        alpha += texture2D(inTexture, clamp(uv + offset, vec2(0.0), vec2(1.0))).a * w;\n        alpha += texture2D(inTexture, clamp(uv - offset, vec2(0.0), vec2(1.0))).a * w;\n        weightSum += 2.0 * w;\n    }\n\n    alpha = (alpha / weightSum) * strength;\n    gl_FragColor = vec4(0.0, 0.0, 0.0, alpha);\n}\n".getBytes()), 35632);
                case "roundRectTexture" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D textureIn;\nuniform float radius, alpha;\n\nfloat roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) -size, 0.)) - radius;\n}\n\nvoid main() {\n    float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;\n    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);\n}".getBytes()), 35632);
                case "roundRectOutline" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color, outlineColor;\nuniform float radius, outlineThickness;\n\nfloat roundedSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;\n}\n\nvoid main() {\n    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n\n    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n\n    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n\n}".getBytes()), 35632);
                case "roundedRect" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color;\nuniform float radius;\nuniform bool blur;\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b, 0.0)) - r;\n}\n\nvoid main() {\n    vec2 rectHalf = rectSize * 0.5;\n    gl_FragColor = vec4(color.rgb, (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1.0, radius))) * color.a);\n}".getBytes()), 35632);
                case "roundedRectGradient" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color1, color2, color3, color4;\nuniform float radius;\n\n#define NOISE .5/255.0\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b , 0.0)) - r;\n}\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 st = gl_TexCoord[0].st;\n    vec2 halfSize = rectSize * .5;\n    \n   // use the bottom leftColor as the alpha\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));\n    vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);\n}".getBytes()), 35632);
                case "gradient" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec4 color1, color2, color3, color4;\n\n#define NOISE .5/255.0\n\nvec3 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4).rgb, texColorAlpha);\n}".getBytes()), 35632);
                case "mainmenu" -> this.createShader(new ByteArrayInputStream("uniform float TIME;\nuniform vec2 RESOLUTION;\n\n#define NUM_OCTAVES 6\n\nmat3 rotX(float a) {\n    float c = cos(a);\n    float s = sin(a);\n    return mat3(\n        1, 0, 0,\n        0, c, -s,\n        0, s, c\n    );\n}\n\nmat3 rotY(float a) {\n    float c = cos(a);\n    float s = sin(a);\n    return mat3(\n        c, 0, -s,\n        0, 1, 0,\n        s, 0, c\n    );\n}\n\nfloat random(vec2 pos) {\n    return fract(sin(dot(pos.xy, vec2(12.9898, 78.233))) * 43758.5453123);\n}\n\nfloat noise(vec2 pos) {\n    vec2 i = floor(pos);\n    vec2 f = fract(pos);\n    float a = random(i + vec2(0.0, 0.0));\n    float b = random(i + vec2(1.0, 0.0));\n    float c = random(i + vec2(0.0, 1.0));\n    float d = random(i + vec2(1.0, 1.0));\n    vec2 u = f * f * (3.0 - 2.0 * f);\n    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;\n}\n\nfloat fbm(vec2 pos) {\n    float v = 0.0;\n    float a = 0.5;\n    vec2 shift = vec2(100.0);\n    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));\n    for (int i = 0; i < NUM_OCTAVES; i++) {\n        float dir = mod(float(i), 2.0) > 0.5 ? 1.0 : -1.0;\n        v += a * noise(pos - 0.05 * dir * TIME);\n\n        pos = rot * pos * 2.0 + shift;\n        a *= 0.5;\n    }\n    return v;\n}\n\nvec3 render(in vec2 fragCoord) {\n    vec2 p = (fragCoord * 2.0 - RESOLUTION.xy) / min(RESOLUTION.x, RESOLUTION.y);\n    p -= vec2(12.0, 0.0);\n\n    float time2 = 1.0;\n    vec2 q = vec2(0.0);\n    q.x = fbm(p + 0.00 * time2);\n    q.y = fbm(p + vec2(1.0));\n    vec2 r = vec2(0.0);\n    r.x = fbm(p + 1.0 * q + vec2(1.7, 9.2) + 0.15 * time2);\n    r.y = fbm(p + 1.0 * q + vec2(8.3, 2.8) + 0.126 * time2);\n    float f = fbm(p + r);\n\n    vec3 color = mix(\n        vec3(0.3, 0.3, 0.6),\n        vec3(0.7, 0.7, 0.7),\n        clamp((f * f) * 4.0, 0.0, 1.0)\n    );\n\n    color = mix(\n        color,\n        vec3(0.7, 0.7, 0.7),\n        clamp(length(q), 0.0, 1.0)\n    );\n\n    color = mix(\n        color,\n        vec3(0.4, 0.4, 0.4),\n        clamp(length(r.x), 0.0, 1.0)\n    );\n\n    color = (f * f * f + 0.9 * f * f + 0.8 * f) * color;\n\n    return color * 0.5;\n}\n\nvoid mainImage(out vec4 fragColor, in vec2 fragCoord) {\n    vec3 color = render(fragCoord);\n    fragColor = vec4(color, color.r);\n}\n\nvoid main(void) {\n    mainImage(gl_FragColor, gl_FragCoord.xy);\n}\n".getBytes()), 35632);
                case "kawaseUp" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;\n    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;\n\n    gl_FragColor = vec4(sum.rgb /12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}\n".getBytes()), 35632);
                case "kawaseDown" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0;\n    sum += texture2D(inTexture, uv - halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + halfpixel.xy * offset);\n    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    gl_FragColor = vec4(sum.rgb * .125, 1.0);\n}\n".getBytes()), 35632);
                case "kawaseUpBloom" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\nuniform float strength;\n\nvoid main() {\n  //  if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a > 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    vec3 boosted = (result.rgb / result.a) * strength;\n    gl_FragColor = vec4(boosted, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a),check));\n}".getBytes()), 35632);
                case "kawaseDownBloom" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum.rgb *= sum.a;\n    sum *= 4.0;\n    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);\n    smp1.rgb *= smp1.a;\n    sum += smp1;\n    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3;\n    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 result = sum / 8.0;\n    gl_FragColor = vec4(result.rgb / result.a, result.a);\n}".getBytes()), 35632);
                case "gaussianBlur" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D textureIn;\nuniform vec2 texelSize;\nuniform vec2 direction;\nuniform float strength;\nuniform float radius;\nuniform float weights[128];\n\n#define offset (texelSize * direction)\n\nvoid main() {\n    vec2 uv = gl_TexCoord[0].st;\n    vec3 color = texture2D(textureIn, uv).rgb * weights[0];\n\n    for (int i = 1; i < 128; ++i) {\n        if (i > int(radius)) break;\n\n        vec2 delta = float(i) * offset;\n        color += texture2D(textureIn, uv + delta).rgb * weights[i];\n        color += texture2D(textureIn, uv - delta).rgb * weights[i];\n    }\n\n    // apply strength to control blur intensity\n    gl_FragColor = vec4(color * strength, 1.0);\n}\n".getBytes()), 35632);
                case "cape" -> this.createShader(new ByteArrayInputStream("#extension GL_OES_standard_derivatives : enable\n\n#ifdef GL_ES\nprecision highp float;\n#endif\n\nuniform float time;\nuniform vec2  resolution;\nuniform float zoom;\n\n#define PI 3.1415926535\n\nmat2 rotate3d(float angle)\n{\n    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));\n}\n\nvoid main()\n{\n    vec2 p = (gl_FragCoord.xy * 2.0 - resolution) / min(resolution.x, resolution.y);\n    p = rotate3d((time * 2.0) * PI) * p;\n    float t;\n    if (sin(time) == 10.0)\n        t = 0.075 / abs(1.0 - length(p));\n    else\n        t = 0.075 / abs(0.4/*sin(time)*/ - length(p));\n    gl_FragColor = vec4(     ( 1. -exp( -vec3(t)  * vec3(0.13*(sin(time)+12.0), p.y*0.7, 3.0) )) , 1.0);\n}".getBytes()), 35632);
                case "outline" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform vec2 texelSize, direction;\nuniform sampler2D texture;\nuniform float radius;\nuniform vec3 color;\n\n#define offset direction * texelSize\n\nvoid main() {\n    float centerAlpha = texture2D(texture, gl_TexCoord[0].xy).a;\n    float innerAlpha = centerAlpha;\n    for (float r = 1.0; r <= radius; r++) {\n        float alphaCurrent1 = texture2D(texture, gl_TexCoord[0].xy + offset * r).a;\n        float alphaCurrent2 = texture2D(texture, gl_TexCoord[0].xy - offset * r).a;\n\n        innerAlpha += alphaCurrent1 + alphaCurrent2;\n    }\n\n    gl_FragColor = vec4(color, innerAlpha) * step(0.0, -centerAlpha);\n}\n\n\n".getBytes()), 35632);
                case "glow" -> this.createShader(new ByteArrayInputStream("#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure, radius;\nuniform float weights[256];\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n\n    float innerAlpha = texture2D(textureIn, gl_TexCoord[0].st).a * weights[0];\n\n    for (float r = 1.0; r <= radius; r ++) {\n        innerAlpha += texture2D(textureIn, gl_TexCoord[0].st + offset * r).a * weights[int(r)];\n        innerAlpha += texture2D(textureIn, gl_TexCoord[0].st - offset * r).a * weights[int(r)];\n    }\n\n    gl_FragColor = vec4(color, mix(innerAlpha, 1.0 - exp(-innerAlpha * exposure), step(0.0, direction.y)));\n}\n".getBytes()), 35632);
                default -> this.createShader(mc.getResourceManager().getResource(new ResourceLocation(fragmentShaderLoc)).getInputStream(), 35632);
            };
            GL20.glAttachShader(program, fragmentShaderID);
            int vertexShaderID = this.createShader(mc.getResourceManager().getResource(new ResourceLocation(vertexShaderLoc)).getInputStream(), 35633);
            GL20.glAttachShader(program, vertexShaderID);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        GL20.glLinkProgram(program);
        int status = GL20.glGetProgrami(program, 35714);
        if (status == 0) {
            throw new IllegalStateException("Shader failed to link!");
        }
        this.programID = program;
    }

    public ShaderUtils(String fragmentShaderLoc) {
        this(fragmentShaderLoc, "advantage/shaders/vertex.vsh");
    }

    public void init() {
        GL20.glUseProgram(this.programID);
    }

    public void unload() {
        GL20.glUseProgram(0);
    }

    public int getUniform(String name) {
        return GL20.glGetUniformLocation(this.programID, name);
    }

    public void setUniformf(String name, float ... args) {
        int loc = GL20.glGetUniformLocation(this.programID, name);
        switch (args.length) {
            case 1: {
                GL20.glUniform1f(loc, args[0]);
                break;
            }
            case 2: {
                GL20.glUniform2f(loc, args[0], args[1]);
                break;
            }
            case 3: {
                GL20.glUniform3f(loc, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                GL20.glUniform4f(loc, args[0], args[1], args[2], args[3]);
            }
        }
    }

    public void setUniformi(String name, int ... args) {
        int loc = GL20.glGetUniformLocation(this.programID, name);
        if (args.length > 1) {
            GL20.glUniform2i(loc, args[0], args[1]);
        } else {
            GL20.glUniform1i(loc, args[0]);
        }
    }

    public static void drawQuads(float x, float y, float width, float height) {
        GL20.glBegin(7);
        GL20.glTexCoord2f(0.0f, 0.0f);
        GL20.glVertex2f(x, y);
        GL20.glTexCoord2f(0.0f, 1.0f);
        GL20.glVertex2f(x, y + height);
        GL20.glTexCoord2f(1.0f, 1.0f);
        GL20.glVertex2f(x + width, y + height);
        GL20.glTexCoord2f(1.0f, 0.0f);
        GL20.glVertex2f(x + width, y);
        GL20.glEnd();
    }

    public static void drawQuads() {
        ScaledResolution sr = new ScaledResolution(mc);
        float width = (float)sr.getScaledWidth_double();
        float height = (float)sr.getScaledHeight_double();
        GL20.glBegin(7);
        GL20.glTexCoord2f(0.0f, 1.0f);
        GL20.glVertex2f(0.0f, 0.0f);
        GL20.glTexCoord2f(0.0f, 0.0f);
        GL20.glVertex2f(0.0f, height);
        GL20.glTexCoord2f(1.0f, 0.0f);
        GL20.glVertex2f(width, height);
        GL20.glTexCoord2f(1.0f, 1.0f);
        GL20.glVertex2f(width, 0.0f);
        GL20.glEnd();
    }

    public static void drawQuads(float width, float height) {
        ShaderUtils.drawQuads(0.0f, 0.0f, width, height);
    }

    public static void drawFixedQuads() {
        ScaledResolution sr = new ScaledResolution(mc);
        ShaderUtils.drawQuads(ShaderUtils.mc.displayWidth / sr.getScaleFactor(), (float)ShaderUtils.mc.displayHeight / (float)sr.getScaleFactor());
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, (CharSequence)ShaderUtils.readInputStream(inputStream));
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, 35713) == 0) {
            System.out.println(GL20.glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }
        return shader;
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}

