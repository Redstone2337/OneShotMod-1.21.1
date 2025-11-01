package net.redstone233.nsp.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.config.ClientConfig;

import java.util.Objects;
import java.util.Random;

public class AttackEventHandler {
    private static final Random RANDOM = new Random();

    public static void register() {
        // 监听实体受伤事件
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (!ClientConfig.isEnabled()) {
                return EventResult.pass();
            }

            // 检查伤害来源是否为玩家
            if (source.getAttacker() instanceof PlayerEntity player) {
                // 检查是否为左键攻击（主手）
                if (isValidAttack(player, entity)) {
                    // 检查概率
                    if (RANDOM.nextInt(100) < ClientConfig.getChance()) {
                        // 触发一击必杀
                        triggerInstakill(player, entity);
                        return EventResult.interruptFalse(); // 取消原伤害
                    }
                }
            }
            return EventResult.pass();
        });

        OneShotMod.LOGGER.info("一击必杀事件处理器已注册");
    }

    private static boolean isValidAttack(PlayerEntity player, Entity target) {
        // 检查目标是否有效
        if (!(target instanceof LivingEntity)) {
            return false;
        }

        // 检查是否为Boss生物
        if (isBossEntity(target) && !ClientConfig.affectsBosses()) {
            if (ClientConfig.isDebugMode()) {
                OneShotMod.LOGGER.debug("忽略Boss实体: {}", Objects.requireNonNull(target.getDisplayName()).getString());
            }
            return false;
        }

        // 检查是否在黑名单中
        if (isBlacklistedEntity(target)) {
            if (ClientConfig.isDebugMode()) {
                OneShotMod.LOGGER.debug("忽略黑名单实体: {}", Objects.requireNonNull(target.getDisplayName()).getString());
            }
            return false;
        }

        // 检查是否影响玩家
        if (target instanceof PlayerEntity && !ClientConfig.affectsPlayers()) {
            if (ClientConfig.isDebugMode()) {
                OneShotMod.LOGGER.debug("忽略玩家实体: {}", Objects.requireNonNull(target.getDisplayName()).getString());
            }
            return false;
        }

        // 检查是否需要空手
        if (ClientConfig.requiresEmptyHand()) {
            boolean isEmptyHand = player.getMainHandStack().isEmpty();
            if (ClientConfig.isDebugMode() && !isEmptyHand) {
                OneShotMod.LOGGER.debug("要求空手但玩家手持物品: {}", player.getMainHandStack().getName().getString());
            }
            return isEmptyHand;
        }

        return true;
    }

    private static boolean isBossEntity(Entity entity) {
        return entity instanceof EnderDragonEntity || entity instanceof WitherEntity;
    }

    private static boolean isBlacklistedEntity(Entity entity) {
        String entityId = entity.getName().getString();
        if (entityId == null) return false;

        for (String blacklisted : ClientConfig.getBlacklistedEntities()) {
            if (blacklisted.equals(entityId)) {
                return true;
            }
        }
        return false;
    }

    private static void triggerInstakill(PlayerEntity player, Entity target) {
        if (player.getWorld().isClient) {
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        String playerName = Objects.requireNonNull(player.getDisplayName()).getString();
        String entityName = Objects.requireNonNull(target.getDisplayName()).getString();

        // 发送消息
        String message = ClientConfig.getMessage(playerName, entityName);
        Text messageComponent = Text.literal(message);

        if (ClientConfig.shouldBroadcastMessage()) {
            // 向所有玩家广播消息
            serverPlayer.server.getPlayerManager().broadcast(messageComponent, false);
            if (ClientConfig.isDebugMode()) {
                OneShotMod.LOGGER.debug("向所有玩家广播一击必杀消息");
            }
        } else {
            // 只向触发玩家发送消息
            serverPlayer.sendMessage(messageComponent);
            if (ClientConfig.isDebugMode()) {
                OneShotMod.LOGGER.debug("向玩家 {} 发送一击必杀消息", playerName);
            }
        }

        // 在动作栏显示提示
        if (ClientConfig.shouldShowInActionbar()) {
            Text actionbarMessage = Text.literal("⚡ 一击必杀!");
            serverPlayer.sendMessage(actionbarMessage, true);
            if (ClientConfig.isDebugMode()) {
                OneShotMod.LOGGER.debug("在动作栏显示一击必杀提示");
            }
        }

        // 杀死实体
        if (target instanceof LivingEntity livingEntity) {
            livingEntity.kill();

            if (ClientConfig.isDebugMode()) {
                OneShotMod.LOGGER.info("一击必杀触发: 玩家 {} 杀死了 {}", playerName, entityName);
            }
        }

        // 基础的粒子效果
        spawnBasicParticles(target);
    }

    private static void spawnBasicParticles(Entity target) {
        // 基础的爆炸粒子效果
        // 在实际实现中，这里会调用粒子效果生成代码
        if (ClientConfig.isDebugMode()) {
            OneShotMod.LOGGER.debug("生成基础粒子效果 for {}", Objects.requireNonNull(target.getDisplayName()).getString());
        }
    }
}