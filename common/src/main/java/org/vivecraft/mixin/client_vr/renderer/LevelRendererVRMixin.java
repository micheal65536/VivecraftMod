package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.LevelTargetBundleExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.util.Set;

// priority 999 to inject before iris, for the vrFast rendering
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {
    @Unique
    private static final ResourceLocation vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID = ResourceLocation.fromNamespaceAndPath("vivecraft", "vrtransparency");

    @Unique
    private Entity vivecraft$capturedEntity;
    @Unique
    private Entity vivecraft$renderedEntity;

    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private ClientLevel level;
    @Shadow
    private RenderTarget entityOutlineTarget;
    @Shadow
    @Final
    private LevelTargetBundle targets;
    @Final
    @Shadow
    private RenderBuffers renderBuffers;

    @Shadow
    protected abstract void renderHitOutline(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, int i);

    @Override
    @Unique
    public Entity vivecraft$getRenderedEntity() {
        return this.vivecraft$renderedEntity;
    }

    @ModifyVariable(at = @At("HEAD"), method = "addWeatherPass", argsOnly = true)
    public Vec3 vivecraft$rainX(Vec3 cameraPos) {
        if (!RenderPassType.isVanilla() && (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT || ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT)) {
            return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition();
        } else {
            return cameraPos;
        }
    }

    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void vivecraft$reinitVR(ResourceManager resourceManager, CallbackInfo ci) {
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("Resource Reload");
        }
    }

    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void vivecraft$setOutlineTarget(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.entityOutlineTarget = RenderPassManager.wrp.outlineTarget;
        }
    }

    /*
     * Start `renderLevel` lighting poll
     */

    // TODO maybe move to the ClientLevel
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;pollLightUpdates()V"), method = "renderLevel")
    public void vivecraft$onePollLightUpdates(ClientLevel instance) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT) {
            instance.pollLightUpdates();
        }
    }

    // TODO maybe move to the LevelLightEngine
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I"), method = "renderLevel")
    public int vivecraft$oneLightingUpdates(LevelLightEngine instance) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT) {
            return instance.runLightUpdates();
        } else {
            return 0;
        }
    }

    /*
     * End `renderLevel` lighting poll
     */

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V", shift = Shift.AFTER),
        method = "renderLevel")
    public void vivecraft$stencil(CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
        if (!RenderPassType.isVanilla()) {
            if (ClientDataHolderVR.getInstance().vrSettings.vrUseStencil) {
                FramePass framePass = frameGraphBuilder.addPass("vr_stencil");
                this.targets.main = framePass.readsAndWrites(this.targets.main);
                framePass.executes(() -> {
                    Profiler.get().popPush("stencil");
                    VREffectsHelper.drawEyeStencil(false);
                });
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), method = "collectVisibleEntities")
    public boolean vivecraft$noPlayerWhenSleeping(LivingEntity instance) {
        if (!RenderPassType.isVanilla()) {
            return false;
        } else {
            return instance.isSleeping();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderEntity")
    public void vivecraft$captureEntityRestoreLoc(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        this.vivecraft$capturedEntity = entity;
        if (!RenderPassType.isVanilla() && vivecraft$capturedEntity == minecraft.getCameraEntity()) {
            ((GameRendererExtension) minecraft.gameRenderer).vivecraft$restoreRVEPos((LivingEntity) vivecraft$capturedEntity);
        }
        this.vivecraft$renderedEntity = vivecraft$capturedEntity;
    }

    @Inject(at = @At("TAIL"), method = "renderEntity")
    public void vivecraft$restoreLoc2(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        if (!RenderPassType.isVanilla() && vivecraft$capturedEntity == minecraft.getCameraEntity()) {
            ((GameRendererExtension) minecraft.gameRenderer).vivecraft$cacheRVEPos((LivingEntity) vivecraft$capturedEntity);
            ((GameRendererExtension) minecraft.gameRenderer).vivecraft$setupRVE();
        }
        this.vivecraft$renderedEntity = null;
    }

    @Inject(at = @At("HEAD"), method = "renderBlockOutline")
    public void vivecraft$interactOutline(CallbackInfo ci, @Local(argsOnly = true) Camera camera, @Local(argsOnly = true) MultiBufferSource.BufferSource bufferSource, @Local(argsOnly = true) PoseStack poseStack) {
        if (!RenderPassType.isVanilla()) {
            Profiler.get().popPush("interact outline");
            Vec3 vec3 = camera.getPosition();
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                OptifineHelper.beginOutlineShader();
            }
            for (int c = 0; c < 2; c++) {
                if (ClientDataHolderVR.getInstance().interactTracker.isInteractActive(c) && (ClientDataHolderVR.getInstance().interactTracker.inBlockHit[c] != null || ClientDataHolderVR.getInstance().interactTracker.bukkit[c])) {
                    BlockPos blockpos = ClientDataHolderVR.getInstance().interactTracker.inBlockHit[c] != null ? ClientDataHolderVR.getInstance().interactTracker.inBlockHit[c].getBlockPos() : BlockPos.containing(ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getController(c).getPosition());

                    BlockState blockstate = this.level.getBlockState(blockpos);

                    this.renderHitOutline(poseStack, bufferSource.getBuffer(RenderType.lines()), camera.getEntity(), vec3.x, vec3.y, vec3.z, blockpos, blockstate, 0xFFFFFFFF);
                }
            }
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                this.renderBuffers.bufferSource().endBatch(RenderType.lines());
                OptifineHelper.endOutlineShader();
            }
        }
    }

    @ModifyExpressionValue(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;"), method = "renderBlockOutline")
    public HitResult vivecraft$noBlockoutlineOnInteract(HitResult hitPos) {
        // don't draw the block outline when the interaction outline is active
        return (ClientDataHolderVR.getInstance().interactTracker.isInteractActive(0) && (ClientDataHolderVR.getInstance().interactTracker.inBlockHit[0] != null || ClientDataHolderVR.getInstance().interactTracker.bukkit[0])) ? null : hitPos;
    }

    @Unique
    private boolean vivecraft$menuHandleft;
    @Unique
    private boolean vivecraft$menuhandright;
    @Unique
    private boolean vivecraft$guiRendered = false;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void vivecraft$resetGuiRendered(CallbackInfo ci) {
        vivecraft$guiRendered = false;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;createInternal(Ljava/lang/String;Lcom/mojang/blaze3d/resource/ResourceDescriptor;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 0))
    public void vivecraft$addVRTargets(CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder, @Local RenderTargetDescriptor renderTargetDescriptor) {
        if (VRState.vrRunning) {
            this.targets.replace(LevelTargetBundleExtension.OCCLUDED_TARGET_ID, frameGraphBuilder.createInternal("vroccluded", renderTargetDescriptor));
            this.targets.replace(LevelTargetBundleExtension.UNOCCLUDED_TARGET_ID, frameGraphBuilder.createInternal("vrunoccluded", renderTargetDescriptor));
            this.targets.replace(LevelTargetBundleExtension.HANDS_TARGET_ID, frameGraphBuilder.createInternal("vrhands", renderTargetDescriptor));
        }
    }

    // no remap needed bnecause of optifine, to have the *
    @Inject(method = {"method_62202", "addMainPass*"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;readsAndWrites(Lcom/mojang/blaze3d/resource/ResourceHandle;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 0, remap = true), remap = false)
    public void vivecraft$markVRTargetsForWrite(CallbackInfo ci, @Local FramePass framePass) {
        if (VRState.vrRunning && this.targets instanceof LevelTargetBundleExtension ext) {
            if (ext.vivecraft$getOccluded() != null) {
                this.targets.replace(LevelTargetBundleExtension.OCCLUDED_TARGET_ID, framePass.readsAndWrites(ext.vivecraft$getOccluded()));
            }
            if (ext.vivecraft$getUnoccluded() != null) {
                this.targets.replace(LevelTargetBundleExtension.UNOCCLUDED_TARGET_ID, framePass.readsAndWrites(ext.vivecraft$getUnoccluded()));
            }
            if (ext.vivecraft$getHands() != null) {
                this.targets.replace(LevelTargetBundleExtension.HANDS_TARGET_ID, framePass.readsAndWrites(ext.vivecraft$getHands()));
            }
            // fix vanilla bug https://bugs.mojang.com/browse/MC-278096
            if (this.targets.clouds != null && this.minecraft.options.getCloudsType() == CloudStatus.OFF) {
                this.targets.clouds = framePass.readsAndWrites(this.targets.clouds);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 0, shift = Shift.AFTER, remap = true), method = {
        "method_62214", // fabric
        "lambda$addMainPass$1(Lnet/minecraft/client/renderer/FogParameters;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;Lnet/minecraft/util/profiling/ProfilerFiller;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/renderer/culling/Frustum;ZLcom/mojang/blaze3d/resource/ResourceHandle;)V", // forge
        "lambda$addMainPass$1(Lnet/minecraft/client/renderer/FogParameters;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;Lnet/minecraft/util/profiling/ProfilerFiller;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/renderer/culling/Frustum;ZLcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/renderer/LightTexture;)V", // optifine
        "lambda$addMainPass$2" // neoforge
    }, remap = false)
    public void vivecraft$renderVrStuffPart1(CallbackInfo ci, @Local float partialTick) {
        if (RenderPassType.isVanilla()) {
            return;
        }
        vivecraft$menuHandleft = MethodHolder.isInMenuRoom() || this.minecraft.screen != null || KeyboardHandler.Showing;
        vivecraft$menuhandright = vivecraft$menuHandleft || ClientDataHolderVR.getInstance().interactTracker.hotbar >= 0 && ClientDataHolderVR.getInstance().vrSettings.vrTouchHotbar;


        if (this.targets.translucent != null) {
            // fix vanilla bug https://bugs.mojang.com/browse/MC-278096
            if (this.targets.clouds != null && this.minecraft.options.getCloudsType() == CloudStatus.OFF) {
                this.targets.clouds.get().clear();
            }
            VREffectsHelper.renderVRFabulous(partialTick, this.targets, vivecraft$menuhandright, vivecraft$menuHandleft);
        } else {
            VREffectsHelper.renderVrFast(partialTick, false, vivecraft$menuhandright, vivecraft$menuHandleft);
            if (ShadersHelper.isShaderActive() && ClientDataHolderVR.getInstance().vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID) {
                // shaders active, and render gui before translucents
                VREffectsHelper.renderVrFast(partialTick, true, vivecraft$menuhandright, vivecraft$menuHandleft);
                vivecraft$guiRendered = true;
            }
        }
    }

    @Inject(at = @At(value = "TAIL"), method = {
        "method_62213", // fabric
        "lambda$addParticlesPass$2", // forge
        "lambda$addParticlesPass$5"}, remap = false) // neoforge
    public void vivecraft$renderVrStuffPart2(CallbackInfo ci, @Local(argsOnly = true) float partialTick) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (this.targets.translucent == null && (!ShadersHelper.isShaderActive() || ClientDataHolderVR.getInstance().vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.AFTER_TRANSLUCENT)) {
            // no shaders, or shaders, and gui after translucents
            VREffectsHelper.renderVrFast(partialTick, true, vivecraft$menuhandright, vivecraft$menuHandleft);
            vivecraft$guiRendered = true;
        }
    }

    // if the gui didn't render yet, render it now.
    // or if shaders are on, and option AFTER_SHADER is selected
    @Inject(at = @At(value = "RETURN"), method = "renderLevel")
    public void vivecraft$renderVrStuffFinal(CallbackInfo ci, @Local(ordinal = 0) float partialTick) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (!vivecraft$guiRendered && !Minecraft.useShaderTransparency()) {
            this.minecraft.getMainRenderTarget().bindWrite(true);
            RenderSystem.getModelViewStack().pushMatrix().identity();
            RenderHelper.applyVRModelView(ClientDataHolderVR.getInstance().currentPass, RenderSystem.getModelViewStack());

            VREffectsHelper.renderVrFast(partialTick, true, vivecraft$menuhandright, vivecraft$menuHandleft);

            RenderSystem.getModelViewStack().popMatrix();

            vivecraft$guiRendered = true;
        }
    }

    @Inject(at = @At("HEAD"), method = {"initOutline", "resize"})
    public void vivecraft$restoreOutlineTarget(CallbackInfo ci) {
        if (VRState.vrInitialized) {
            this.entityOutlineTarget = RenderPassManager.INSTANCE.vanillaOutlineTarget;
        }
    }

    @Inject(at = @At("TAIL"), method = "initOutline")
    public void vivecraft$captureOutlineTarget(CallbackInfo ci) {
        RenderPassManager.INSTANCE.vanillaOutlineTarget = this.entityOutlineTarget;
    }

    @Inject(at = @At("TAIL"), method = "close")
    public void vivecraft$clearOutlineTarget(CallbackInfo ci) {
        RenderPassManager.INSTANCE.vanillaOutlineTarget = null;
    }

    @ModifyArg(method = "getTransparencyChain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/ResourceLocation;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private ResourceLocation vivecraft$VRTransparency(ResourceLocation postShader) {
        if (VRState.vrRunning) {
            return vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID;
        } else {
            return postShader;
        }
    }

    @ModifyArg(method = "getTransparencyChain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/ResourceLocation;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private Set<ResourceLocation> vivecraft$VRTargets(Set<ResourceLocation> targets) {
        if (VRState.vrRunning) {
            return LevelTargetBundleExtension.VR_TARGETS;
        } else {
            return targets;
        }
    }
}
