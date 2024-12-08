package org.vivecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Polygon;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;

public class VRPlayerModel_WithArms extends VRPlayerModel {
    private final boolean slim;
    public ModelPart leftHand;
    public ModelPart rightHand;
    public ModelPart leftHandSleeve;
    public ModelPart rightHandSleeve;

    private boolean laying;

    // TODO 1.21.3 check if that can be solved a different way
    private float attackTime;

    public VRPlayerModel_WithArms(ModelPart modelPart, boolean isSlim) {
        super(modelPart, isSlim);
        this.slim = isSlim;
        // use left/right arm as shoulders
        this.rightHand = modelPart.getChild("right_hand");
        this.leftHand = modelPart.getChild("left_hand");
        this.rightHandSleeve = rightHand.getChild("right_hand_sleeve");
        this.leftHandSleeve = leftHand.getChild("left_hand_sleeve");


        //finger hax
        // some mods remove the base parts
        if (!leftArm.cubes.isEmpty()) {
            copyUV(leftArm.cubes.get(0).polygons[1], leftHand.cubes.get(0).polygons[1]);
            copyUV(leftArm.cubes.get(0).polygons[1], leftHand.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(leftArm, leftHand, 3, 3);
                SodiumHelper.copyModelCuboidUV(leftArm, leftHand, 3, 2);
            }
        }
        if (!rightArm.cubes.isEmpty()) {
            copyUV(rightArm.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[1]);
            copyUV(rightArm.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(rightArm, rightHand, 3, 3);
                SodiumHelper.copyModelCuboidUV(rightArm, rightHand, 3, 2);
            }
        }

        if (!rightHandSleeve.cubes.isEmpty()) {
            copyUV(rightSleeve.cubes.get(0).polygons[1], this.rightHandSleeve.cubes.get(0).polygons[1]);
            copyUV(rightSleeve.cubes.get(0).polygons[1], this.rightHandSleeve.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(rightSleeve, rightHandSleeve, 3, 3);
                SodiumHelper.copyModelCuboidUV(rightSleeve, rightHandSleeve, 3, 2);
            }
        }
        if (!leftHandSleeve.cubes.isEmpty()) {
            copyUV(leftSleeve.cubes.get(0).polygons[1], leftHandSleeve.cubes.get(0).polygons[1]);
            copyUV(leftSleeve.cubes.get(0).polygons[1], leftHandSleeve.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(leftSleeve, leftHandSleeve, 3, 3);
                SodiumHelper.copyModelCuboidUV(leftSleeve, leftHandSleeve, 3, 2);
            }
        }
    }

    private void copyUV(Polygon source, Polygon dest) {
        for (int i = 0; i < source.vertices().length; i++) {
            Vertex newVertex = new Vertex(dest.vertices()[i].pos(), source.vertices()[i].u(), source.vertices()[i].v());
            if (OptifineHelper.isOptifineLoaded()) {
                OptifineHelper.copyRenderPositions(dest.vertices()[i], newVertex);
            }
            dest.vertices()[i] = newVertex;
        }
    }

    public static MeshDefinition createMesh(CubeDeformation p_170826_, boolean p_170827_) {
        MeshDefinition meshdefinition = VRPlayerModel.createMesh(p_170826_, p_170827_);
        PartDefinition partdefinition = meshdefinition.getRoot();

        if (p_170827_) {
            PartDefinition leftHand = partdefinition.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            leftHand.addOrReplaceChild("left_hand_sleeve", CubeListBuilder.create().texOffs(48, 55).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
            PartDefinition rightHand = partdefinition.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            rightHand.addOrReplaceChild("right_hand_sleeve", CubeListBuilder.create().texOffs(40, 39).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
            PartDefinition leftShoulder = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            PartDefinition rightShoulder = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            leftShoulder.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
            rightShoulder.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
        } else {
            PartDefinition leftHand = partdefinition.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            leftHand.addOrReplaceChild("left_hand_sleeve", CubeListBuilder.create().texOffs(48, 55).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
            PartDefinition rightHand = partdefinition.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            rightHand.addOrReplaceChild("right_hand_sleeve", CubeListBuilder.create().texOffs(40, 39).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
            PartDefinition leftShoulder = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            PartDefinition rightShoulder = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            leftShoulder.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
            rightShoulder.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25f)), PartPose.ZERO);
        }
        return meshdefinition;
    }


    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.leftHand, this.rightHand, this.leftArm, this.rightArm, this.leftSleeve, this.rightSleeve, this.rightLeg, this.leftLeg, this.hat, this.leftPants, this.rightPants, this.leftHandSleeve, this.rightHandSleeve, this.jacket);
    }

    public void setupAnim(PlayerRenderState playerRenderState) {
        super.setupAnim(playerRenderState);

        this.attackTime = playerRenderState.attackTime;

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(((EntityRenderStateExtension) playerRenderState).vivecraft$getEntityUUID()) && ClientDataHolderVR.getInstance().currentPass == RenderPass.CAMERA && ClientDataHolderVR.getInstance().cameraTracker.isQuickMode() && ClientDataHolderVR.getInstance().grabScreenShot) {
            // player hands block the camera, so disable them for the screenshot
            this.leftHand.visible = false;
            this.rightHand.visible = false;
            this.leftHandSleeve.visible = false;
            this.rightHandSleeve.visible = false;
        }

        if (this.rotInfo == null) {
            return;
        }

        double d0 = -1.501F * this.rotInfo.heightScale;
        float f = (float) Math.toRadians(playerRenderState.yRot);
        float f1 = (float) Math.atan2(-this.rotInfo.headRot.x, -this.rotInfo.headRot.z);
        float f2 = (float) Math.asin(this.rotInfo.headRot.y / this.rotInfo.headRot.length());
        float f3 = (float) Math.atan2(-this.rotInfo.leftArmRot.x, -this.rotInfo.leftArmRot.z);
        float f4 = (float) Math.asin(this.rotInfo.leftArmRot.y / this.rotInfo.leftArmRot.length());
        float f5 = (float) Math.atan2(-this.rotInfo.rightArmRot.x, -this.rotInfo.rightArmRot.z);
        float f6 = (float) Math.asin(this.rotInfo.rightArmRot.y / this.rotInfo.rightArmRot.length());
        double d1 = this.rotInfo.getBodyYawRadians();

        this.laying = playerRenderState.swimAmount > 0.0F || playerRenderState.isFallFlying && !playerRenderState.isAutoSpinAttack;

        if (!this.rotInfo.reverse) {
            this.rightArm.setPos(-Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, Mth.sin(this.body.yRot) * 5.0F);
            this.leftArm.setPos(Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, -Mth.sin(this.body.yRot) * 5.0F);
        } else {
            this.leftArm.setPos(-Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, Mth.sin(this.body.yRot) * 5.0F);
            this.rightArm.setPos(Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, -Mth.sin(this.body.yRot) * 5.0F);
        }

        if (playerRenderState.isCrouching) {
            this.rightArm.y += 3.2F;
            this.leftArm.y += 3.2F;
        }

        // remove entity scale from that
        float scale = 1.0F / playerRenderState.scale;
        Vec3 vec3 = this.rotInfo.leftArmPos.scale(scale);
        Vec3 vec32 = this.rotInfo.rightArmPos.scale(scale);

        vec3 = vec3.add(0.0D, d0, 0.0D);
        vec3 = vec3.yRot((float) (-Math.PI + d1));
        vec3 = vec3.scale(16.0F / this.rotInfo.heightScale);
        this.leftHand.setPos((float) (-vec3.x), (float) (-vec3.y), (float) vec3.z);
        this.leftHand.xRot = (float) ((double) (-f4) + (Math.PI * 1.5D));
        this.leftHand.yRot = (float) (Math.PI - (double) f3 - d1);
        this.leftHand.zRot = 0.0F;


        Vec3 vec31 = new Vec3((double) this.leftArm.x + vec3.x, (double) this.leftArm.y + vec3.y, (double) this.leftArm.z - vec3.z);
        float f7 = (float) Math.atan2(vec31.x, vec31.z);
        float f8 = (float) ((Math.PI * 1.5D) - Math.asin(vec31.y / vec31.length()));
        this.leftArm.zRot = 0.0F;
        this.leftArm.xRot = f8;
        this.leftArm.yRot = f7;

        if (this.leftArm.yRot > 0.0F) {
            this.leftArm.yRot = 0.0F;
        }

        if (playerRenderState.rightArmPose == ArmPose.THROW_SPEAR) {
            this.leftHand.xRot = (float) ((double) this.leftHand.xRot - (Math.PI / 2D));
        }

        vec32 = vec32.add(0.0D, d0, 0.0D);
        vec32 = vec32.yRot((float) (-Math.PI + d1));
        vec32 = vec32.scale(16.0F / this.rotInfo.heightScale);
        this.rightHand.setPos((float) (-vec32.x), -((float) vec32.y), (float) vec32.z);
        this.rightHand.xRot = (float) ((double) (-f6) + (Math.PI * 1.5D));
        this.rightHand.yRot = (float) (Math.PI - (double) f5 - d1);
        this.rightHand.zRot = 0.0F;

        Vec3 vec33 = new Vec3((double) this.rightArm.x + vec32.x, (double) this.rightArm.y + vec32.y, (double) this.rightArm.z - vec32.z);
        float f9 = (float) Math.atan2(vec33.x, vec33.z);
        float f10 = (float) ((Math.PI * 1.5D) - Math.asin(vec33.y / vec33.length()));
        this.rightArm.zRot = 0.0F;
        this.rightArm.xRot = f10;
        this.rightArm.yRot = f9;

        if (this.rightArm.yRot < 0.0F) {
            this.rightArm.yRot = 0.0F;
        }

        if (playerRenderState.rightArmPose == ArmPose.THROW_SPEAR) {
            this.rightHand.xRot = (float) ((double) this.rightHand.xRot - (Math.PI / 2D));
        }

        if (this.laying) {
            this.rightArm.xRot = (float) ((double) this.rightArm.xRot - (Math.PI / 2D));
            this.leftArm.xRot = (float) ((double) this.leftArm.xRot - (Math.PI / 2D));
        }

        //this.leftHandSleeve.copyFrom(this.leftHand);
        //this.rightHandSleeve.copyFrom(this.rightHand);
        //this.leftShoulder_sleeve.copyFrom(this.leftShoulder);
        //this.rightShoulder_sleeve.copyFrom(this.rightShoulder);
        this.leftSleeve.visible = this.leftHandSleeve.visible;
        this.rightSleeve.visible = this.rightHandSleeve.visible;
        setAllVisible(true);
    }

    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        this.rightArm.visible = pVisible;
        this.leftArm.visible = pVisible;
        this.rightSleeve.visible = pVisible;
        this.leftSleeve.visible = pVisible;
        this.rightHand.visible = pVisible;
        this.leftHand.visible = pVisible;
    }

    protected ModelPart getArm(HumanoidArm pSide) {
        return pSide == HumanoidArm.LEFT ? this.leftHand : this.rightHand;
    }

    public void translateToHand(HumanoidArm pSide, PoseStack pMatrixStack) {
        ModelPart modelpart = this.getArm(pSide);

        if (this.laying) {
            pMatrixStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }

        modelpart.translateAndRotate(pMatrixStack);
        pMatrixStack.mulPose(Axis.XP.rotation((float) Math.sin((double) this.attackTime * Math.PI)));
        pMatrixStack.translate(0.0D, -0.5D, 0.0D);
    }

//	public void renderToBuffer(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
//	{
//		this.body.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.jacket.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.leftLeg.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.rightLeg.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.leftPants.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.rightPants.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		pMatrixStack.pushPose();
//		this.head.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.hat.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.vrHMD.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//
//		if (this.seated)
//		{
//			this.leftArm.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//			this.rightArm.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		}
//		else
//		{
//			this.leftShoulder.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//			this.rightShoulder.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//
//			if (this.laying)
//			{
//				pMatrixStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
//			}
//
//			this.rightHand.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//			this.leftHand.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		}
//
//		this.leftHandSleeve.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.rightHandSleeve.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		pMatrixStack.popPose();
//	}
}
