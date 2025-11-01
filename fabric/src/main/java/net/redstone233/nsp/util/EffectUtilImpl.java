// fabric/src/main/java/net/redstone233/nsp/util/EffectUtilImpl.java
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

        // Fabric 特有的粒子效果
        serverWorld.spawnParticles(
                ParticleTypes.EXPLOSION,
                center.x, center.y, center.z,
                15,  // 数量
                target.getWidth() * 0.5, target.getHeight() * 0.5, target.getWidth() * 0.5,
                0.8  // 速度
        );

        // 额外的 Fabric 特有粒子
        serverWorld.spawnParticles(
                ParticleTypes.FLAME,
                center.x, center.y, center.z,
                20,
                target.getWidth(), target.getHeight(), target.getWidth(),
                0.3
        );

        // 闪电效果粒子
        serverWorld.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                center.x, center.y + target.getHeight(), center.z,
                10,
                0.5, 0.2, 0.5,
                0.5
        );
    }

    public static void playInstakillSound(Entity target) {
        if (!(target.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Vec3d pos = EffectUtil.getEntityCenter(target);

        // Fabric 使用原版声音系统
        serverWorld.playSound(
                null, // 如果没有特定玩家，传null
                pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.PLAYERS,
                1.0f, // 音量
                0.8f  // 音调
        );

        // 额外的爆炸声音
        serverWorld.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.PLAYERS,
                0.7f,
                1.2f
        );
    }
}