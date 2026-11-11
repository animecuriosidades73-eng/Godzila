package com.godzillamod.entity;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Carga unos segundos y dispara un rayo por la boca. */
public class BeamAttackGoal extends Goal {
    private static final int CHARGE_TICKS = 40;     // 2 s cargando
    private static final int FIRE_TICKS = 40;       // 2 s disparando
    private static final int COOLDOWN_TICKS = 160;  // 8 s entre rayos
    private static final double RANGE = 45.0;       // alcance en bloques
    private static final float KILL_DAMAGE = 1_000_000f;

    private final GodzillaEntity mob;
    private int charge;
    private int fire;
    private int nextFireTick = 60;
    private Vec3 dir = Vec3.ZERO;

    public BeamAttackGoal(GodzillaEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null
                && target.isAlive()
                && mob.tickCount >= nextFireTick
                && mob.distanceToSqr(target) < RANGE * RANGE
                && mob.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isAlive() && (charge > 0 || fire > 0);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        charge = CHARGE_TICKS;
        fire = FIRE_TICKS;
        mob.setBeaming(true);
        mob.getNavigation().stop();
        mob.playSound(SoundEvents.ENDER_DRAGON_GROWL, 8.0f, 0.5f);
    }

    @Override
    public void stop() {
        mob.setBeaming(false);
        nextFireTick = mob.tickCount + COOLDOWN_TICKS;
        charge = 0;
        fire = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        mob.getNavigation().stop();
        if (target != null) {
            mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 origin = mob.getMouthPos();

        if (charge > 0) {
            charge--;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, origin.x, origin.y, origin.z, 8, 0.8, 0.8, 0.8, 0.2);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, origin.x, origin.y, origin.z, 3, 0.4, 0.4, 0.4, 0.02);
            if (charge == 0) {
                dir = (target != null)
                        ? target.getEyePosition().subtract(origin).normalize()
                        : mob.getViewVector(1.0f);
                mob.playSound(SoundEvents.WITHER_SHOOT, 8.0f, 0.5f);
            }
            return;
        }

        fire--;
        if (target != null) {
            Vec3 desired = target.getEyePosition().subtract(origin).normalize();
            dir = dir.lerp(desired, 0.2).normalize();
        }
        fireBeam(level, origin, dir);
    }

    private void fireBeam(ServerLevel level, Vec3 origin, Vec3 dir) {
        Set<LivingEntity> alreadyHit = new HashSet<>();
        boolean breakBlocksNow = mob.tickCount % 2 == 0;

        for (double d = 0; d <= RANGE; d += 1.0) {
            Vec3 p = origin.add(dir.scale(d));
            BlockPos center = BlockPos.containing(p);

            // El rayo se detiene en bloques irrompibles (bedrock)
            BlockState centerState = level.getBlockState(center);
            if (!centerState.isAir() && centerState.getDestroySpeed(level, center) < 0.0f) {
                break;
            }

            // Rompe todo en un radio de 1 bloque alrededor del rayo
            if (breakBlocksNow) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos bp = center.offset(dx, dy, dz);
                            BlockState s = level.getBlockState(bp);
                            if (!s.isAir()
                                    && !(s.getBlock() instanceof LiquidBlock)
                                    && s.getDestroySpeed(level, bp) >= 0.0f) {
                                level.destroyBlock(bp, false, mob);
                            }
                        }
                    }
                }
            }

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.25, 0.25, 0.25, 0.01);
            if (((int) d) % 3 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 3, 0.4, 0.4, 0.4, 0.3);
            }

            // Mata de un golpe a todo ser vivo que toque el rayo
            AABB box = new AABB(p, p).inflate(1.5);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != mob && x.isAlive())) {
                if (alreadyHit.add(e)) {
                    e.hurtServer(level, mob.damageSources().mobAttack(mob), KILL_DAMAGE);
                }
            }
        }
    }
}
