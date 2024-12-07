package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;

public class VRShaders {
    public static final ShaderProgram lanczosShader = new ShaderProgram(
        ResourceLocation.fromNamespaceAndPath("vivecraft", "core/lanczos"),
        DefaultVertexFormat.POSITION_TEX, ShaderDefines.EMPTY);
    public static AbstractUniform _Lanczos_texelWidthOffsetUniform;
    public static AbstractUniform _Lanczos_texelHeightOffsetUniform;
    public static AbstractUniform _Lanczos_inputImageTextureUniform;
    public static AbstractUniform _Lanczos_inputDepthTextureUniform;
    public static AbstractUniform _Lanczos_projectionUniform;
    public static AbstractUniform _Lanczos_modelViewUniform;

    public static final ShaderProgram depthMaskShader = new ShaderProgram(
        ResourceLocation.fromNamespaceAndPath("vivecraft", "core/mixedreality"),
    DefaultVertexFormat.POSITION_TEX, ShaderDefines.EMPTY);
    public static AbstractUniform _DepthMask_hmdViewPosition;
    public static AbstractUniform _DepthMask_hmdPlaneNormal;
    public static AbstractUniform _DepthMask_projectionMatrix;
    public static AbstractUniform _DepthMask_viewMatrix;
    public static AbstractUniform _DepthMask_firstPersonPassUniform;
    public static AbstractUniform _DepthMask_keyColorUniform;
    public static AbstractUniform _DepthMask_alphaModeUniform;

    public static final ShaderProgram fovReductionShader = new ShaderProgram(
        ResourceLocation.fromNamespaceAndPath("vivecraft", "core/fovreduction"),
        DefaultVertexFormat.POSITION_TEX, ShaderDefines.EMPTY);
    public static int _FOVReduction_Enabled;
    public static AbstractUniform _FOVReduction_RadiusUniform;
    public static AbstractUniform _FOVReduction_OffsetUniform;
    public static AbstractUniform _FOVReduction_BorderUniform;
    public static AbstractUniform _Overlay_HealthAlpha;
    public static AbstractUniform _Overlay_FreezeAlpha;
    public static AbstractUniform _Overlay_waterAmplitude;
    public static AbstractUniform _Overlay_portalAmplitutde;
    public static AbstractUniform _Overlay_pumpkinAmplitutde;
    public static AbstractUniform _Overlay_time;
    public static AbstractUniform _Overlay_BlackAlpha;
    public static AbstractUniform _Overlay_eye;

    public static final ShaderProgram blitAspectShader = new ShaderProgram(
        ResourceLocation.fromNamespaceAndPath("vivecraft", "core/blit_aspect"),
        DefaultVertexFormat.POSITION_TEX, ShaderDefines.EMPTY);

    public static final ShaderProgram rendertypeEndPortalShaderVR = new ShaderProgram(
        ResourceLocation.fromNamespaceAndPath("vivecraft", "core/rendertype_end_portal_vr"),
        DefaultVertexFormat.POSITION, ShaderDefines.EMPTY);
    public static final ShaderProgram rendertypeEndGatewayShaderVR = new ShaderProgram(
        ResourceLocation.fromNamespaceAndPath("vivecraft", "core/rendertype_end_gateway_vr"),
        DefaultVertexFormat.POSITION, ShaderDefines.EMPTY);

    private VRShaders() {
    }

    public static void setupDepthMask() throws Exception {
        CompiledShaderProgram program = Minecraft.getInstance().getShaderManager().getProgram(depthMaskShader);

        _DepthMask_hmdViewPosition = program.safeGetUniform("hmdViewPosition");
        _DepthMask_hmdPlaneNormal = program.safeGetUniform("hmdPlaneNormal");
        _DepthMask_projectionMatrix = program.safeGetUniform("projectionMatrix");
        _DepthMask_viewMatrix = program.safeGetUniform("viewMatrix");
        _DepthMask_firstPersonPassUniform = program.safeGetUniform("firstPersonPass");
        _DepthMask_keyColorUniform = program.safeGetUniform("keyColor");
        _DepthMask_alphaModeUniform = program.safeGetUniform("alphaMode");
    }

    public static void setupFSAA() throws Exception {
        CompiledShaderProgram program = Minecraft.getInstance().getShaderManager().getProgram(lanczosShader);

        _Lanczos_texelWidthOffsetUniform = program.safeGetUniform("texelWidthOffset");
        _Lanczos_texelHeightOffsetUniform = program.safeGetUniform("texelHeightOffset");
        _Lanczos_inputImageTextureUniform = program.safeGetUniform("inputImageTexture");
        _Lanczos_inputDepthTextureUniform = program.safeGetUniform("inputDepthTexture");
        _Lanczos_projectionUniform = program.safeGetUniform("projection");
        _Lanczos_modelViewUniform = program.safeGetUniform("modelView");
    }

    public static void setupFOVReduction() throws Exception {
        CompiledShaderProgram program = Minecraft.getInstance().getShaderManager().getProgram(fovReductionShader);

        _FOVReduction_RadiusUniform = program.safeGetUniform("circle_radius");
        _FOVReduction_OffsetUniform = program.safeGetUniform("circle_offset");
        _FOVReduction_BorderUniform = program.safeGetUniform("border");
        _Overlay_HealthAlpha = program.safeGetUniform("redalpha");
        _Overlay_FreezeAlpha = program.safeGetUniform("bluealpha");
        _Overlay_waterAmplitude = program.safeGetUniform("water");
        _Overlay_portalAmplitutde = program.safeGetUniform("portal");
        _Overlay_pumpkinAmplitutde = program.safeGetUniform("pumpkin");
        _Overlay_eye = program.safeGetUniform("eye");
        _Overlay_time = program.safeGetUniform("portaltime");
        _Overlay_BlackAlpha = program.safeGetUniform("blackalpha");
    }

    public static void setupBlitAspect() throws Exception {
        Minecraft.getInstance().getShaderManager().getProgram(blitAspectShader);
    }
}
