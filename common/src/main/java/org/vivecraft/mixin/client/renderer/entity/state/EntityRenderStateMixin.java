package org.vivecraft.mixin.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client.extensions.EntityRenderStateExtension;

import java.util.UUID;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateExtension {

    @Unique
    private UUID vivecraft$uuid;

    @Override
    public UUID vivecraft$getEntityUUID() {
        return this.vivecraft$uuid;
    }

    @Override
    public void vivecraft$setEntityUUID(UUID uuid) {
        this.vivecraft$uuid = uuid;
    }
}
