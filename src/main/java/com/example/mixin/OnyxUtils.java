package com.example.mixin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import java.util.Comparator;

public class OnyxUtils implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null || client.world == null) return;

            // AUTO WIND CHARGE JUMP
            if (client.options.jumpKey.isPressed() && !player.isOnGround()) {
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = player.getInventory().getStack(i);
                    if (stack.isOf(Items.WIND_CHARGE)) {
                        player.getInventory().selectedSlot = i;
                        player.setPitch(90.0f);
                        client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                        break; 
                    }
                }
            }

            // MACE SNAP (Auto aim when falling with Mace)
            if (player.isFallFlying() && player.getMainHandStack().isOf(Items.MACE)) {
                if (client.options.attackKey.isPressed()) {
                    Entity target = client.world.getEntitiesByClass(PlayerEntity.class, 
                        player.getBoundingBox().expand(6.0), e -> e != player && e.isAlive())
                        .stream().min(Comparator.comparingDouble(player::squaredDistanceTo)).orElse(null);
                    if (target != null) {
                        double dx = target.getX() - player.getX();
                        double dy = (target.getY() + target.getEyeHeight() * 0.8) - (player.getY() + player.getEyeHeight());
                        double dz = target.getZ() - player.getZ();
                        player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f);
                        player.setPitch((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
                        client.interactionManager.attackEntity(player, target);
                    }
                }
            }
        });
    }
}
