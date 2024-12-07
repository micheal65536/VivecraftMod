package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;

import java.util.UUID;

public class VRPlayerRenderer extends PlayerRenderer {
    static LayerDefinition VRLayerDef = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    static LayerDefinition VRLayerDef_arms = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, false), 64, 64);
    static LayerDefinition VRLayerDef_slim = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64);
    static LayerDefinition VRLayerDef_arms_slim = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, true), 64, 64);

    public VRPlayerRenderer(EntityRendererProvider.Context context, boolean slim, boolean seated) {
        super(context, slim);
        model = !slim ?
                (seated ?
                 new VRPlayerModel(VRLayerDef.bakeRoot(), slim) :
                 new VRPlayerModel_WithArms(VRLayerDef_arms.bakeRoot(), slim)) :
                (seated ?
                 new VRPlayerModel(VRLayerDef_slim.bakeRoot(), slim) :
                 new VRPlayerModel_WithArms(VRLayerDef_arms_slim.bakeRoot(), slim));

        this.addLayer(new HMDLayer(this));
    }

    public boolean hasLayerType(RenderLayer<?,?> renderLayer) {
        return this.layers.stream().anyMatch(layer -> layer.getClass() == renderLayer.getClass());
    }

    @Override
    public Vec3 getRenderOffset(PlayerRenderState playerRenderState) {
        //idk why we do this anymore
        return playerRenderState.isVisuallySwimming ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
        // return pEntity.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(pEntity, pPartialTicks);
    }

    @Override
    protected void setupRotations(PlayerRenderState playerRenderState, PoseStack poseStack, float yRot, float scale) {
        UUID uuid = ((EntityRenderStateExtension) playerRenderState).vivecraft$getEntityUUID();
        if (ClientDataHolderVR.getInstance().currentPass != RenderPass.GUI && VRPlayersClient.getInstance().isTracked(uuid)) {
            VRPlayersClient.RotInfo playermodelcontroller$rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(uuid);
            yRot = (float) Math.toDegrees(playermodelcontroller$rotinfo.getBodyYawRadians());
        }

        //vanilla below here
        super.setupRotations(playerRenderState, poseStack, yRot, scale);
    }
}
