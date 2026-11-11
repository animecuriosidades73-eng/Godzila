package com.godzillamod.client;

import com.godzillamod.GodzillaMod;
import com.godzillamod.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;

public class GodzillaModClient implements ClientModInitializer {
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(GodzillaMod.MOD_ID, "godzilla"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(MODEL_LAYER, GodzillaModel::createBodyLayer);
        EntityRenderers.register(ModEntities.GODZILLA, GodzillaRenderer::new);
    }
}
