package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class WyvernAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;
    public static final double WATER_DAMAGE = 1.0;
    public static final long WATER_DAMAGE_INTERVAL_TICKS = 30L;
    public static final EntityDamageEvent.DamageCause[] DAMAGE_CAUSE_IMMUNITIES = {
        EntityDamageEvent.DamageCause.FIRE,
        EntityDamageEvent.DamageCause.FIRE_TICK,
        EntityDamageEvent.DamageCause.LAVA
    };
    public static final String[] DEATH_MESSAGES = {
            "{0} had their life extinguished by water",
            "{0} couldn't swim",
            "{0} died as they begged the question",
            "{0} blubbed their last blub",
            "{0} became fish food"
    };

    private final Cooldown ABILITY_COOLDOWN = new Cooldown(15.0);

    private BukkitTask waterDamageTask = null;

    @Override
    public void initialize() {
        DeathMessageManager.setDeathMessages(this, DEATH_MESSAGES);
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public long getTaskIntervalTicks() {
        return TASK_INTERVAL_TICKS;
    }

    @Override
    public void run() {
        Player player = owner.getPlayer();

        if (player == null) {
            return;
        }

        if ((!Util.isInRain(player) && !Util.isInWater(player)) || player.isDead()) {
            Util.safeTaskCancel(waterDamageTask);
        } else if (Util.safeTaskIsCancelled(waterDamageTask)) {
            waterDamageTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                if (WATER_DAMAGE >= player.getHealth()) {
                    DeathMessageManager.setNextDeathMessage(player.getUniqueId(), this);
                }

                Util.dealTrueDamage(player, WATER_DAMAGE);
            }, 0L, WATER_DAMAGE_INTERVAL_TICKS);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!isOwner(entity)) {
            return;
        }

        for (EntityDamageEvent.DamageCause cause : DAMAGE_CAUSE_IMMUNITIES) {
            if (event.getCause() == cause) {
                event.setCancelled(true);

                entity.setFireTicks(0);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player) || !player.isSneaking()) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack handItem = event.getItem();

        if (handItem == null || handItem.getType() != Material.FIRE_CHARGE) {
            return;
        }

        // Activate ability

        if (ABILITY_COOLDOWN.isNotReady()) {
            return;
        }

        ABILITY_COOLDOWN.reset();

        if (player.getGameMode() != GameMode.CREATIVE) {
            handItem.setAmount(handItem.getAmount() - 1);
        }

        // Play sound

        World world = player.getWorld();

        world.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, Util.nextFloatRange(0.9f, 1.0f));

        // Create fireball

        Location location = player.getEyeLocation();
        Vector direction = location.getDirection();

        Fireball fireball = world.spawn(location, Fireball.class);

        fireball.setShooter(player);
        fireball.setDirection(direction);

        fireball.setIsIncendiary(true);
        fireball.setYield(2.0f);
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (isOwner(player) && itemStack.getType() == Material.POTION) {
            ItemMeta meta = itemStack.getItemMeta();

            if (!(meta instanceof PotionMeta)) {
                return;
            }

            PotionType potionType = ((PotionMeta) meta).getBasePotionData().getType();

            if (potionType == PotionType.WATER || potionType == PotionType.MUNDANE || potionType == PotionType.AWKWARD) {
                DeathMessageManager.setNextDeathMessage(player.getUniqueId(), this);

                Util.dealTrueDamage(player, player.getHealth());
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (isOwner(player) && player.isSneaking() && !player.isOnGround()) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (isOwner(player) && !player.isOnGround() && !player.isFlying()) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getName() {
        return "Reign of Fire";
    }

    @Override
    public String getDescription() {
        return "Being a Flying-Fire type, Wyverns are weak to water.";
    }
}
