package com.godzillamod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Modelo hecho con cubos. 1 bloque = 16 unidades; el pie esta en y = 24.
 * Textura 256x256 (generada para coincidir con los texOffs de abajo).
 */
public class GodzillaModel extends EntityModel<GodzillaRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart legL;
    private final ModelPart legR;
    private final ModelPart tail1;
    private final ModelPart tail2;

    public GodzillaModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.armL = body.getChild("arm_l");
        this.armR = body.getChild("arm_r");
        this.legL = root.getChild("leg_l");
        this.legR = root.getChild("leg_r");
        this.tail1 = body.getChild("tail1");
        this.tail2 = tail1.getChild("tail2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Piernas (pivote en la cadera)
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 96).addBox(-10, 0, -10, 20, 48, 20);
        root.addOrReplaceChild("leg_l", leg, PartPose.offset(18, -24, 0));
        root.addOrReplaceChild("leg_r", leg, PartPose.offset(-18, -24, 0));

        // Torso inclinado hacia adelante
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-24, -56, -16, 48, 64, 32),
                PartPose.offsetAndRotation(0, -24, 0, 0.25f, 0, 0));

        // Cabeza: craneo + hocico
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 164).addBox(-14, -14, -22, 28, 22, 24)
                        .texOffs(0, 220).addBox(-11, -4, -40, 22, 10, 18),
                PartPose.offsetAndRotation(0, -52, -8, -0.25f, 0, 0));
        // Mandibula (se abre al disparar)
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(80, 220).addBox(-11, 0, -38, 22, 6, 18),
                PartPose.offset(0, 6, -2));

        // Brazos cortos
        CubeListBuilder arm = CubeListBuilder.create().texOffs(160, 0).addBox(-5, 0, -5, 10, 28, 10);
        body.addOrReplaceChild("arm_l", arm, PartPose.offsetAndRotation(29, -46, -14, -0.7f, 0, 0));
        body.addOrReplaceChild("arm_r", arm, PartPose.offsetAndRotation(-29, -46, -14, -0.7f, 0, 0));

        // Cola en dos partes
        PartDefinition tail1 = body.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(80, 96).addBox(-12, -12, 0, 24, 24, 40),
                PartPose.offsetAndRotation(0, 0, 14, -0.35f, 0, 0));
        tail1.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(104, 164).addBox(-8, -8, 0, 16, 16, 40),
                PartPose.offsetAndRotation(0, 0, 38, -0.15f, 0, 0));

        // Placas dorsales
        CubeListBuilder plate = CubeListBuilder.create().texOffs(200, 0).addBox(-3, -10, 0, 6, 20, 12);
        for (int i = 0; i < 5; i++) {
            body.addOrReplaceChild("plate" + i, plate,
                    PartPose.offsetAndRotation(0, -48 + i * 12, 14, 0.5f, 0, 0));
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(GodzillaRenderState state) {
        super.setupAnim(state);

        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = -0.25f + state.xRot * Mth.DEG_TO_RAD;

        float swing = Math.min(1.0f, state.walkAnimationSpeed * 2.0f);
        float pos = state.walkAnimationPos * 0.35f;
        legL.xRot = Mth.cos(pos) * 0.9f * swing;
        legR.xRot = Mth.cos(pos + Mth.PI) * 0.9f * swing;
        armL.xRot = -0.7f + Mth.cos(pos + Mth.PI) * 0.3f * swing;
        armR.xRot = -0.7f + Mth.cos(pos) * 0.3f * swing;
        body.yRot = Mth.cos(pos) * 0.08f * swing;

        tail1.yRot = Mth.cos(state.ageInTicks * 0.06f) * 0.15f;
        tail2.yRot = Mth.cos(state.ageInTicks * 0.06f - 1.0f) * 0.2f;

        jaw.xRot = state.beaming ? 0.7f : 0.0f;
    }
}
