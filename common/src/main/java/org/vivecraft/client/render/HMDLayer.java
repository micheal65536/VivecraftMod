package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.extensions.EntityRenderStateExtension;

public class HMDLayer extends RenderLayer<PlayerRenderState, PlayerModel> {

    ResourceLocation DIAMOND_HMD = ResourceLocation.parse("vivecraft:textures/diamond_hmd.png");
    ResourceLocation GOLD_HMD = ResourceLocation.parse("vivecraft:textures/gold_hmd.png");
    ResourceLocation BLACK_HMD = ResourceLocation.parse("vivecraft:textures/black_hmd.png");

    public HMDLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState entityState, float f, float g) {
        if (this.getParentModel().head.visible && this.getParentModel() instanceof VRPlayerModel vrPlayerModel) {
            VRPlayersClient.RotInfo rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(((EntityRenderStateExtension)entityState).vivecraft$getEntityUUID());
            ResourceLocation hmd;
            switch (rotinfo.hmd) {
                default -> hmd = null;
                case 1 -> hmd = this.BLACK_HMD;
                case 2 -> hmd = this.GOLD_HMD;
                case 3, 4 -> hmd = this.DIAMOND_HMD;
            }

            if (hmd == null) {
                return;
            }
            poseStack.pushPose();
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(hmd));
            vrPlayerModel.renderHMDR(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
