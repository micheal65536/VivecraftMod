package org.vivecraft.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.Map;
import java.util.Optional;

@Mixin(ShaderManager.class)
public class ShaderManagerMixin {

    @WrapOperation(method = "apply(Lnet/minecraft/client/renderer/ShaderManager$Configs;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private Object vivecraft$letTransparencyFail(Map instance, Object k, Object v, Operation<Object> original, @Local ShaderManager.CompilationCache compilationCache) {
        if (((ShaderProgram) k).configId().equals(VRShaders.vrTransparency.configId())) {
            // let vrtransparency fail, some gpus don't support the 18 samplers we need
            VRSettings.logger.error("Vivecraft: Failed to compile 'post/vrtransparency' fabulous graphics will not be available in VR.", (ShaderManager.CompilationException) v);
            compilationCache.programs.put((ShaderProgram) k, Optional.empty());
            return null;
        }
        return original.call(instance, k, v);
    }
}
