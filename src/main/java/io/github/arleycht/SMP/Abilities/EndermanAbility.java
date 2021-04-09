package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

public class EndermanAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;
    public static final double WATER_DAMAGE = 1.0;
    public static final long WATER_DAMAGE_INTERVAL_TICKS = 10L;
    public static final String[] DEATH_MESSAGES = {
            "{0} had their life extinguished by water",
            "{0} couldn't swim",
            "{0} died as they begged the question", // Water you doing : )
            "{0} blubbed their last blub",
            "{0} became fish food"
    };

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

        if (!Util.isInRain(player) && !Util.isInWater(player)) {
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

    @Override
    public String getName() {
        return "Of the Ender";
    }

    @Override
    public String getDescription() {
        return "Teleport like an Enderman. Try swimming.";
    }
}
