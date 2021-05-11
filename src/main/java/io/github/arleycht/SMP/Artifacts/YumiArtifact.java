package io.github.arleycht.SMP.Artifacts;

import io.github.arleycht.SMP.DanSMP;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicReference;

public class YumiArtifact implements IArtifact {
    private final String artifactName;

    public YumiArtifact(String artifactName) {
        this.artifactName = artifactName;
    }

    @Override
    public void initialize(ItemStack itemStack) {
        itemStack.addEnchantment(Enchantment.MENDING, 0);
        itemStack.addEnchantment(Enchantment.ARROW_INFINITE, 0);
    }

    @EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();

        event.getNewSlot();

        if (ArtifactManager.getArtifact(inventory.getItem(event.getNewSlot())) == this
                || ArtifactManager.getArtifact(inventory.getItemInOffHand()) == this) {
            AtomicReference<BukkitTask> taskAtomicReference = new AtomicReference<>();

            Util.applyEffect(player, PotionEffectType.SPEED, 2.0f, 1, true, true, true);
            Util.applyEffect(player, PotionEffectType.JUMP, 2.0f, 1, true, true, true);

            taskAtomicReference.set(Bukkit.getScheduler().runTaskTimer(DanSMP.getPlugin(), () -> {
                if (ArtifactManager.getArtifact(inventory.getItemInMainHand()) != this
                        && ArtifactManager.getArtifact(inventory.getItemInOffHand()) != this) {
                    Util.safeTaskCancel(taskAtomicReference.get());
                } else {
                    Util.applyEffect(player, PotionEffectType.SPEED, 2.0f, 1, true, true, true);
                    Util.applyEffect(player, PotionEffectType.JUMP, 2.0f, 1, true, true, true);
                }
            }, 20L, 20L));
        }
    }

    @EventHandler
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        if (ArtifactManager.getArtifact(event.getBow()) != this) {
            return;
        }

        Entity projectile = event.getProjectile();

        if (projectile instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow) projectile;

            arrow.setKnockbackStrength((int) event.getForce() * 3);

            arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean allowDestruction() {
        return true;
    }

    @Override
    public String getName() {
        return artifactName;
    }

    @Override
    public String[] getLore() {
        return new String[]{
            "Fujin Yumi"
        };
    }

    @Override
    public Material getType() {
        return Material.BOW;
    }
}
