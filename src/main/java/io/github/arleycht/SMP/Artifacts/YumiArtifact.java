package io.github.arleycht.SMP.Artifacts;

import io.github.arleycht.SMP.DanSMP;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class YumiArtifact implements IArtifact {
    public static final int KNOCKBACK_FORCE = 3;

    public static final double AOE_KNOCKBACK_RADIUS = 8.0;

    public static final double AOE_KNOCKBACK_FORCE = 2.0;
    public static final double VERTICAL_MULTIPLIER = 1.0;
    public static final double HORIZONTAL_MULTIPLIER = 1.5;

    public static final double SELF_FORCE = 2.5;
    public static final double SELF_VERTICAL_MULTIPLIER = 1.0;
    public static final double SELF_HORIZONTAL_MULTIPLIER = 2.0;

    public static final double AOE_KNOCKBACK_RADIUS_SQUARED = AOE_KNOCKBACK_RADIUS * AOE_KNOCKBACK_RADIUS;

    private final String artifactName;

    private final HashMap<UUID, Projectile> activeArrows = new HashMap<>();

    public YumiArtifact(String artifactName) {
        this.artifactName = artifactName;
    }

    @Override
    public void initialize(ItemStack itemStack) {
        itemStack.addEnchantment(Enchantment.MENDING, 1);
        itemStack.addEnchantment(Enchantment.ARROW_INFINITE, 1);
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

            arrow.setKnockbackStrength((int) (event.getForce() * KNOCKBACK_FORCE));

            if (event.getForce() > 0.5f) {
                activeArrows.put(arrow.getUniqueId(), arrow);
            }
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        Projectile projectile = activeArrows.get(event.getEntity().getUniqueId());

        if (projectile != null) {
            ProjectileSource shooter = projectile.getShooter();

            if (!(shooter instanceof Entity)) {
                return;
            }

            UUID shooterUUID = ((Entity) shooter).getUniqueId();

            Bukkit.getScheduler().runTaskLater(DanSMP.getPlugin(), () -> {
                Location location = projectile.getLocation();

                activeArrows.remove(projectile.getUniqueId());

                for (Entity entity : projectile.getNearbyEntities(8.0, 8.0, 8.0)) {
                    if (entity.equals(projectile)) {
                        continue;
                    }

                    if (entity.equals(event.getHitEntity())) {
                        continue;
                    }

                    double distanceSquared = entity.getLocation().distanceSquared(location);
                    double keepPercentage = 1.0 - Math.sqrt(distanceSquared / AOE_KNOCKBACK_RADIUS_SQUARED);

                    if (keepPercentage < 0.0) {
                        continue;
                    }

                    Vector dir = entity.getLocation().toVector().subtract(location.toVector());

                    dir.normalize();

                    Vector velocityAdd = dir.clone().multiply(keepPercentage);

                    if (entity.getUniqueId() == shooterUUID) {
                        velocityAdd.setY(velocityAdd.getY() * SELF_VERTICAL_MULTIPLIER);
                        velocityAdd.setX(velocityAdd.getX() * SELF_HORIZONTAL_MULTIPLIER);
                        velocityAdd.setZ(velocityAdd.getZ() * SELF_HORIZONTAL_MULTIPLIER);

                        velocityAdd.multiply(SELF_FORCE);
                    } else {
                        velocityAdd.setY(velocityAdd.getY() * VERTICAL_MULTIPLIER);
                        velocityAdd.setX(velocityAdd.getX() * HORIZONTAL_MULTIPLIER);
                        velocityAdd.setZ(velocityAdd.getZ() * HORIZONTAL_MULTIPLIER);

                        velocityAdd.multiply(AOE_KNOCKBACK_FORCE);
                    }

                    try {
                        dir.checkFinite();
                    } catch (IllegalArgumentException ignored) {
                        continue;
                    }

                    Vector velocity = entity.getVelocity().add(velocityAdd);

                    entity.setVelocity(velocity);

                    entity.setFallDistance(0.0f);
                }

                projectile.getWorld().playSound(location, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
            }, 0L);
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
                "Fujin Yumi, a legendary artifact bow.",
                "Deals great knockback and grants the wielder higher speed and jumps."
        };
    }

    @Override
    public Material getType() {
        return Material.BOW;
    }
}
