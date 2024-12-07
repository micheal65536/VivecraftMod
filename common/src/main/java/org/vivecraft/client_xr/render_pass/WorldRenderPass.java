package org.vivecraft.client_xr.render_pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import org.vivecraft.client_vr.VRTextureTarget;

public class WorldRenderPass implements AutoCloseable {

    public static WorldRenderPass stereoXR;
    public static WorldRenderPass center;
    public static WorldRenderPass mixedReality;
    public static WorldRenderPass leftTelescope;
    public static WorldRenderPass rightTelescope;
    public static WorldRenderPass camera;


    public final VRTextureTarget target;
    public final RenderTarget outlineTarget;

    public WorldRenderPass(VRTextureTarget target) {
        this.target = target;
        this.outlineTarget = new TextureTarget(this.target.width, this.target.height, true);
    }

    public void resize(int width, int height) {
        target.resize(width, height);
        outlineTarget.resize(width, height);
    }

    @Override
    public void close() {
        this.target.destroyBuffers();
        this.outlineTarget.destroyBuffers();
    }
}
