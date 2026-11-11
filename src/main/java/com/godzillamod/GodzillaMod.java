package com.godzillamod;

import net.fabricmc.api.ModInitializer;

public class GodzillaMod implements ModInitializer {
    public static final String MOD_ID = "godzillamod";

    @Override
    public void onInitialize() {
        ModEntities.register();
    }
}
