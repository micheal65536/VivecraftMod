package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.extensions.EntityRenderStateExtension;

public class VRPlayerModel extends PlayerModel {
    private final boolean slim;
    public ModelPart vrHMD;
    VRPlayersClient.RotInfo rotInfo;
    private boolean laying;

    public VRPlayerModel(ModelPart modelPart, boolean isSlim) {
        super(modelPart, isSlim);
        this.slim = isSlim;
        this.vrHMD = this.head.getChild("vrHMD");
    }

    public static MeshDefinition createMesh(CubeDeformation p_170826_, boolean p_170827_) {
        MeshDefinition meshdefinition = PlayerModel.createMesh(p_170826_, p_170827_);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.getChild("head").addOrReplaceChild("vrHMD", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -6.0F, -7.5F, 7.0F, 4.0F, 5.0F, p_170826_), PartPose.ZERO);
        return meshdefinition;
    }


    public void setupAnim(PlayerRenderState playerRenderState) {
        playerRenderState.isCrouching &= !playerRenderState.isVisuallySwimming;

        super.setupAnim(playerRenderState);
        
        this.rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(((EntityRenderStateExtension)playerRenderState).vivecraft$getEntityUUID());

        if (this.rotInfo == null) {
            return; //how
        }

        double d0 = -1.501F * this.rotInfo.heightScale;
        float f = (float) Math.toRadians(playerRenderState.yRot);
        float f1 = (float) Math.atan2(-this.rotInfo.headRot.x, -this.rotInfo.headRot.z);
        float f2 = (float) Math.asin(this.rotInfo.headRot.y / this.rotInfo.headRot.length());
        double d1 = this.rotInfo.getBodyYawRadians();
        this.head.xRot = -f2;
        this.head.yRot = (float) (Math.PI - (double) f1 - d1);
        this.laying = playerRenderState.swimAmount > 0.0F || playerRenderState.isFallFlying && !playerRenderState.isAutoSpinAttack;

        if (this.laying) {
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = -4.0F;
            this.head.xRot = (float) ((double) this.head.xRot - (Math.PI / 2D));
        } else if (playerRenderState.isCrouching) {
            // move head down when crouching
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = 4.2f;
        } else {
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = 0.0F;
        }

        this.vrHMD.visible = false;
    }

    public void renderHMDR(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int noOverlay) {
        this.vrHMD.visible = true;
        this.vrHMD.render(poseStack, vertexConsumer, i, noOverlay);
        this.vrHMD.visible = false;
    }
}
