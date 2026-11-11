package com.godzillamod;

import com.godzillamod.entity.GodzillaEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    private static final ResourceKey<EntityType<?>> GODZILLA_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(GodzillaMod.MOD_ID, "godzilla"));

    // Tamano: 4 bloques de ancho x 10 de alto (como un arbol grande). Cambialo aqui.
    public static final EntityType<GodzillaEntity> GODZILLA = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            GODZILLA_KEY,
            EntityType.Builder.of(GodzillaEntity::new, MobCategory.MONSTER)
                    .sized(4.0f, 10.0f)
                    .eyeHeight(8.0f)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .build(GODZILLA_KEY));

    public static void register() {
        FabricDefaultAttributeRegistry.register(GODZILLA, GodzillaEntity.createAttributes());
    }
}
