package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BloodExchangeAbility extends Ability {
    public static final Material ABILITY_ITEM = Material.SPECTRAL_ARROW;
    public static final double TRANSFER_RATE = 1.0;

    public static final String[] DEATH_MESSAGES = {
            "{0} died to charitable causes",
            "{0} gave away too much",
            "{0} sacrificed their blood"
    };

    @Override
    public void initialize() {
        DeathMessageManager.setDeathMessages(this, DEATH_MESSAGES);
    }

    @EventHandler
    public void onItemUse(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        EquipmentSlot hand = event.getHand();

        // If hand is not primary or targeted entity is not a player
        if (hand != EquipmentSlot.HAND || !(entity instanceof Damageable)) {
            return;
        }

        LivingEntity target = (LivingEntity) entity;

        if (target.isDead()) {
            return;
        }

        if (player.getInventory().getItem(hand).getType() == ABILITY_ITEM) {
            double sourceHealth = player.getHealth();
            double targetHealth = target.getHealth();
            double targetMaxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

            double transferable = Math.min(sourceHealth, TRANSFER_RATE);
            double newTargetHealth = Math.min(targetMaxHealth, targetHealth + transferable);
            double transferred = newTargetHealth - targetHealth;
            double newSourceHealth = sourceHealth - transferred;

            if (transferred > 0.0) {
                target.setHealth(newTargetHealth);

                if (player.getGameMode() == GameMode.CREATIVE) {
                    return;
                }

                if (newSourceHealth <= 0.0) {
                    if (Math.random() < 0.5) {
                        DeathMessageManager.setNextDeathMessage(player.getUniqueId(), this);
                    } else {
                        player.damage(transferred, target);
                    }
                } else {
                    player.setHealth(newSourceHealth);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Blood for Blood";
    }

    @Override
    public String getDescription() {
        return "Heals others with your own health!";
    }
}
