package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class SlimeAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 10L;
    public static final double WATER_DAMAGE = 1.0;
    public static final long WATER_DAMAGE_INTERVAL_TICKS = 30L;
    public static final String[] DEATH_MESSAGES = {
            "{0} became too slimy",
            "{0} doesn't like water",
            "{0} died as they begged the question",
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

        Util.applyEffect(player, PotionEffectType.JUMP, 10.0f, 1, false, false, false);

        if ((!Util.isInRain(player) && !Util.isInWater(player)) || player.isDead()) {
            Util.safeTaskCancel(waterDamageTask);
        } else if (Util.safeTaskIsCancelled(waterDamageTask)) {
            waterDamageTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                if (WATER_DAMAGE >= player.getHealth()) {
                    DeathMessageManager.setNextDeathMessage(player.getUniqueId(), this);
                }

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, Util.nextFloatRange(0.8f, 1.2f));
        }
    }

    @Override
    public String getName() {
        return "Slime";
    }

    @Override
    public String getDescription() {
        return "Slime";
    }
}
