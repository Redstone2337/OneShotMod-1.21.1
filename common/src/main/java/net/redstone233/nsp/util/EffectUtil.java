// EffectUtil.java
package net.redstone233.nsp.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EffectUtil {

    @ExpectPlatform
    public static void spawnInstakillParticles(Entity target) {
        // 这个方法将在平台特定模块中实现
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void playInstakillSound(Entity target) {
        // 这个方法将在平台特定模块中实现
        throw new AssertionError();
    }

    // 通用的工具方法，可以在common模块中直接实现
    public static Vec3d getEntityCenter(Entity entity) {
        return new Vec3d(
                entity.getX(),
                entity.getY() + entity.getHeight() / 2,
                entity.getZ()
        );
    }
}