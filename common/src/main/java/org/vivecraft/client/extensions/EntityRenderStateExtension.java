package org.vivecraft.client.extensions;

import java.util.UUID;

public interface EntityRenderStateExtension {
    /**
     * @return the uuid this state was created for
     */
    UUID vivecraft$getEntityUUID();

    /**
     * set the uuid this state was created for
     */
    void vivecraft$setEntityUUID(UUID uuid);
}
