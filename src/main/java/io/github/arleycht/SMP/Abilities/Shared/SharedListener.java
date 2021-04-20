package io.github.arleycht.SMP.Abilities.Shared;

import io.github.arleycht.SMP.Abilities.Ability;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SharedListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!WaterAllergyManager.isAllergic(player)) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItem(EquipmentSlot.HAND);

        if (WaterAllergyManager.isValidProtection(heldItem)) {
            if (inventory.getHelmet() == null) {
                ItemStack helmet = new ItemStack(heldItem.getType(), 1);

                Util.decrementItemStack(heldItem, 1);

                inventory.setItem(EquipmentSlot.HEAD, helmet);

                player.getWorld().playSound(player.getEyeLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (WaterAllergyManager.isAllergic(player) && WaterAllergyManager.isAllergenicPotion(itemStack)) {
            UUID uuid = player.getUniqueId();
            Ability[] abilities = WaterAllergyManager.getAbilities(uuid);

            if (abilities.length <= 0) {
                // This shouldn't happen, but it'll be obvious if it did during runtime
                return;
            }

            Ability ability = abilities[Util.nextIntRange(0, abilities.length)];

            DeathMessageManager.setNextDeathMessage(uuid, ability);

            Util.dealTrueDamage(player, player.getHealth());

        }
    }

    private void doWaterSplashDamage(HashMap<LivingEntity, Double> affectedEntities) {
        for (Map.Entry<LivingEntity, Double> e : affectedEntities.entrySet()) {
            LivingEntity entity = e.getKey();
            double intensity = e.getValue();

            Util.dealTrueDamage(entity, intensity * 6.0);
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (event.getEntity() instanceof ThrownPotion) {
            ItemStack itemStack = ((ThrownPotion) event.getEntity()).getItem();

            if (WaterAllergyManager.isAllergenicPotion(itemStack)) {
                HashMap<LivingEntity, Double> affectedEntities = new HashMap<>();

                Location splashLocation = event.getEntity().getLocation();
                UUID uuid = event.getHitEntity() != null ? event.getHitEntity().getUniqueId() : null;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (splashLocation.getWorld() != player.getWorld() || !WaterAllergyManager.isAllergic(player)) {
                        continue;
                    }

                    if (player.getUniqueId() == uuid) {
                        affectedEntities.put(player, 1.0);

                        continue;
                    }

                    double distanceSquared = player.getLocation().distanceSquared(splashLocation);

                    if (distanceSquared < 16.0) {
                        double intensity = 1.0 - (Math.sqrt(distanceSquared) * 0.25);

                        affectedEntities.put(player, intensity);
                    }
                }

                doWaterSplashDamage(affectedEntities);
            }
        }
    }

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        ItemStack itemStack = event.getPotion().getItem();

        if (WaterAllergyManager.isAllergenicPotion(itemStack)) {
            HashMap<LivingEntity, Double> affectedEntities = new HashMap<>();

            for (LivingEntity e : event.getAffectedEntities()) {
                if (WaterAllergyManager.isAllergic(e)) {
                    affectedEntities.put(e, event.getIntensity(e));
                }
            }

            doWaterSplashDamage(affectedEntities);
        }
    }
}
