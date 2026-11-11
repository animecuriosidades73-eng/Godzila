package com.godzillamod.client;

import com.godzillamod.GodzillaMod;
import com.godzillamod.entity.GodzillaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class GodzillaRenderer extends MobRenderer<GodzillaEntity, GodzillaRenderState, GodzillaModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(GodzillaMod.MOD_ID, "textures/entity/godzilla.png");

    public GodzillaRenderer(EntityRendererProvider.Context context) {
        super(context, new GodzillaModel(context.bakeLayer(GodzillaModClient.MODEL_LAYER)), 3.0f);
    }

    @Override
    public GodzillaRenderState createRenderState() {
        return new GodzillaRenderState();
    }

    @Override
    public void extractRenderState(GodzillaEntity entity, GodzillaRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.beaming = entity.isBeaming();
    }

    @Override
    public Identifier getTextureLocation(GodzillaRenderState state) {
        return TEXTURE;
    }
}
