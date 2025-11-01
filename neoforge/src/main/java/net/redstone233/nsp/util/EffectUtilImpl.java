// neoforge/src/main/java/net/redstone233/nsp/util/EffectUtilImpl.java
package net.redstone233.nsp.util;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class EffectUtilImpl {

    public static void spawnInstakillParticles(Entity target) {
        if (!(target.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Vec3d center = EffectUtil.getEntityCenter(target);

        // NeoForge 特有的粒子效果 - 更华丽的爆炸
        serverWorld.spawnParticles(
                ParticleTypes.EXPLOSION_EMITTER,  // 使用发射器粒子获得更持久的效果
                center.x, center.y, center.z,
                5,  // 数量较少，但每个粒子更复杂
                target.getWidth() * 0.3, target.getHeight() * 0.3, target.getWidth() * 0.3,
                1.2
        );

        // NeoForge 特有的灵魂粒子
        serverWorld.spawnParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                center.x, center.y, center.z,
                25,
                target.getWidth() * 0.8, target.getHeight() * 0.8, target.getWidth() * 0.8,
                0.4
        );

        // 额外的闪光效果
        serverWorld.spawnParticles(
                ParticleTypes.FLASH,
                center.x, center.y + target.getHeight() * 0.2, center.z,
                3,
                0.1, 0.1, 0.1,
                0
        );

        // 冲击波环状粒子
        spawnRingParticles(serverWorld, center, target.getHeight());
    }

    public static void playInstakillSound(Entity target) {
        if (!(target.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Vec3d pos = EffectUtil.getEntityCenter(target);

        // NeoForge 可以使用相同的声音API，但可以调整参数获得不同效果
        serverWorld.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.PLAYERS,
                1.2f, // 更大的音量
                0.6f  // 更低的音调，更震撼
        );

        // NeoForge 特有的声音组合
        serverWorld.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_WITHER_SPAWN,
                SoundCategory.HOSTILE,
                0.5f,
                1.0f
        );

        // 灵魂声音效果
        serverWorld.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.AMBIENT,
                0.8f,
                0.9f
        );
    }

    // NeoForge 特有的粒子效果方法
    private static void spawnRingParticles(ServerWorld world, Vec3d center, double height) {
        int particles = 16;
        double radius = height * 0.8;

        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            world.spawnParticles(
                    ParticleTypes.CRIT,
                    x, center.y, z,
                    2,
                    0.1, 0.3, 0.1,
                    0.2
            );
        }
    }
}