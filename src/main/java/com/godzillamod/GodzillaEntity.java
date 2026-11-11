package com.godzillamod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GodzillaEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_BEAMING =
            SynchedEntityData.defineId(GodzillaEntity.class, EntityDataSerializers.BOOLEAN);

    // Barra de vida arriba de la pantalla, igual que el Wither
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.godzillamod.godzilla"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS);

    private int smashTimer = 0;

    public GodzillaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)          // vida
                .add(Attributes.MOVEMENT_SPEED, 0.10)        // lento (zombie = 0.23)
                .add(Attributes.ATTACK_DAMAGE, 200.0)        // golpe cuerpo a cuerpo
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 3.0)            // sube bloques facil
                .add(Attributes.SAFE_FALL_DISTANCE, 100.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BEAMING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BeamAttackGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.8, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 64.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isBeaming() {
        return this.entityData.get(DATA_BEAMING);
    }

    public void setBeaming(boolean beaming) {
        this.entityData.set(DATA_BEAMING, beaming);
    }

    /** Posicion de la boca, de donde sale el rayo. */
    public Vec3 getMouthPos() {
        return this.getEyePosition().add(this.getViewVector(1.0F).scale(3.0));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            if (--this.smashTimer <= 0) {
                this.smashTimer = 10;
                this.smashBlocksAround(serverLevel);
            }
        }
    }

    /** Rompe arboles y bloques que toca su cuerpo (menos el suelo donde pisa). */
    private void smashBlocksAround(ServerLevel level) {
        AABB box = this.getBoundingBox().inflate(0.5, 0.0, 0.5);
        int minY = Mth.floor(box.minY) + 1;
        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(box.minX), minY, Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()
                    && !(state.getBlock() instanceof LiquidBlock)
                    && state.getDestroySpeed(level, pos) >= 0.0f) {
                level.destroyBlock(pos.immutable(), false, this);
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }
}
