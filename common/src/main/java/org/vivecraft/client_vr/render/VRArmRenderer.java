package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.provider.ControllerType;

public class VRArmRenderer extends PlayerRenderer {

    public float armAlpha = 1F;

    public VRArmRenderer(EntityRendererProvider.Context p_117733_, boolean p_117734_) {
        super(p_117733_, p_117734_);
    }

    @Override
    public void renderRightHand(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, ResourceLocation resourceLocation, boolean sleeve) {
        this.renderItem(ControllerType.RIGHT, pMatrixStack, pBuffer, pCombinedLight, resourceLocation, (this.model).rightArm, sleeve);
    }

    @Override
    public void renderLeftHand(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, ResourceLocation resourceLocation, boolean sleeve) {
        this.renderItem(ControllerType.LEFT, pMatrixStack, pBuffer, pCombinedLight, resourceLocation, (this.model).leftArm, sleeve);
    }

    private void renderItem(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, ResourceLocation resourceLocation, ModelPart arm, boolean sleeve) {
        PlayerModel playermodel = this.getModel();
        arm.resetPose();
        arm.visible = true;
        playermodel.leftSleeve.visible = sleeve;
        playermodel.rightSleeve.visible = sleeve;

        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        arm.xRot = 0.0F;
        arm.render(matrixStackIn, bufferIn.getBuffer(RenderType.entityTranslucent(resourceLocation)), combinedLightIn, OverlayTexture.NO_OVERLAY, ARGB.white(armAlpha));

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
