package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.renderer.CompiledShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

@Mixin(CompiledShaderProgram.class)
public class CompiledShaderProgramVRMixin {

    @ModifyConstant(method = "setDefaultUniforms", constant = @Constant(intValue = 12))
    public int vivecraft$moreTextures(int constant) {
        return RenderSystemAccessor.getShaderTextures().length;
    }
}
