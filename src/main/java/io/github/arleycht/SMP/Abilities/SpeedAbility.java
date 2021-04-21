package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SpeedAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 1L;
    public static final long STATIONARY_LAG_TICKS = 3L * 20L;
    public static final double MUL_SPEED = 0.5;
    public static final double MUL_LAG = -0.2;
    public static final float ADD_EXHAUSTION = 0.2f;

    private long stationaryCounter = 0L;
    private SpeedState state = SpeedState.INACTIVE;

    private Vector delta = new Vector();

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

        Util.applyEffect(player, PotionEffectType.FAST_DIGGING, 10.0f, 1, false, false, false);

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ItemStack offItem = player.getInventory().getItemInOffHand();

        boolean moving = delta.length() > 0.01;
        boolean holdingItem = heldItem.getType() == Material.GOLD_NUGGET || offItem.getType() == Material.GOLD_NUGGET;

        Operation mul = Operation.MULTIPLY_SCALAR_1;

        if (moving) {
            --stationaryCounter;
        } else {
            ++stationaryCounter;
        }

        if (stationaryCounter <= 0) {
            stationaryCounter = 0;
        } else if (stationaryCounter >= STATIONARY_LAG_TICKS) {
            stationaryCounter = STATIONARY_LAG_TICKS;
        }

        switch (state) {
            case INACTIVE:
                if (moving) {
                    stationaryCounter = 0;
                }

                if (stationaryCounter >= STATIONARY_LAG_TICKS) {
                    addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MUL_LAG, mul);
                    Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));

                    state = SpeedState.LAG;
                } else if (holdingItem) {
                    addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MUL_SPEED, mul);
                    Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));

                    state = SpeedState.ACTIVE;
                }

                break;
            case ACTIVE:
                if (moving) {
                    stationaryCounter = 0;
                }

                if (stationaryCounter >= STATIONARY_LAG_TICKS) {
                    clearAttributeModifiers();
                    addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MUL_LAG, mul);
                    Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));

                    state = SpeedState.LAG;
                } else if (!holdingItem) {
                    clearAttributeModifiers();
                    Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));

                    state = SpeedState.INACTIVE;
                }

                break;
            case LAG:
                if (stationaryCounter <= 0) {
                    clearAttributeModifiers();

                    if (holdingItem) {
                        addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MUL_SPEED, mul);

                        state = SpeedState.ACTIVE;
                    } else {
                        state = SpeedState.INACTIVE;
                    }

                    Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));
                }

                if (moving) {
                    World world = player.getWorld();

                    if (world.getTime() % 5 == 0) {
                        float p = 1.0f - (stationaryCounter / (float) STATIONARY_LAG_TICKS);

                        world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.5f + (p * 0.5f));
                    }
                }

                break;
            default:
                break;
        }

        delta = new Vector();
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isOwner(player) && event.getTo() != null) {
            Vector from = event.getFrom().toVector();
            Vector to = event.getTo().toVector();
            Vector diff = to.subtract(from);

            delta.add(diff);

            if (state == SpeedState.ACTIVE) {
                float exhaustion = ADD_EXHAUSTION * (float) diff.length();

                if (exhaustion > 0.01f) {
                    player.setExhaustion(player.getExhaustion() + exhaustion);
                }
            }
        }
    }

    private void activateAbility() {
        if (state == SpeedState.ACTIVE) {
            return;
        }

        state = SpeedState.ACTIVE;

        clearAttributeModifiers();

        Operation mul = Operation.MULTIPLY_SCALAR_1;

        addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MUL_SPEED, mul);

        Player player = owner.getPlayer();

        if (player != null) {
            Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));
        }
    }

    private void activateLag() {
        if (state == SpeedState.ACTIVE) {
            return;
        }

        state = SpeedState.ACTIVE;

        clearAttributeModifiers();

        Operation mul = Operation.MULTIPLY_SCALAR_1;

        addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MUL_LAG, mul);

        Player player = owner.getPlayer();

        if (player != null) {
            Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));
        }
    }

    private void deactivateAbility() {
        if (state == SpeedState.INACTIVE) {
            return;
        }

        state = SpeedState.INACTIVE;

        clearAttributeModifiers();

        Player player = owner.getPlayer();

        if (player != null) {
            Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));
        }
    }

    @Override
    public String getName() {
        return "I am Speed";
    }

    @Override
    public String getDescription() {
        return "I am the embodiment of speed, travelling incarnate. By my will the world moves beneath me.";
    }

    private enum SpeedState {
        INACTIVE,
        ACTIVE,
        LAG
    }
}
