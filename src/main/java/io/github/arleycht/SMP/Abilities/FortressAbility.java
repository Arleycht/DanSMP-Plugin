package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class FortressAbility extends Ability {
    public static final double ADD_ARMOR = 5.0;
    public static final double KNOCKBACK_RESISTANCE_ADD = 0.4;
    public static final double MOVEMENT_SPEED_MUL = -0.15;
    public static final double ATTACK_SPEED_MUL = -0.1;

    private final Cooldown ABILITY_COOLDOWN = new Cooldown(60.0);

    @Override
    public void initialize() {
        AttributeModifier.Operation add = AttributeModifier.Operation.ADD_NUMBER;
        AttributeModifier.Operation mul = AttributeModifier.Operation.ADD_SCALAR;

        addAttributeModifier(Attribute.GENERIC_ARMOR, ADD_ARMOR, add);
        addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_ADD, add);
        addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED_MUL, mul);
        addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, ATTACK_SPEED_MUL, mul);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack heldItem = player.getInventory().getItem(EquipmentSlot.HAND);

        World world = player.getWorld();

        if (heldItem.getType() == Material.DIAMOND) {
            if (ABILITY_COOLDOWN.isNotReady()) {
                return;
            }

            ABILITY_COOLDOWN.reset();

            Util.applyEffect(player, PotionEffectType.DAMAGE_RESISTANCE, 25.0f, 0);
            Util.applyEffect(player, PotionEffectType.GLOWING, 15.0f, 0, false, false, false);

            world.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.25f);
        }
    }

    @Override
    public String getName() {
        return "Walking Fortress";
    }

    @Override
    public String getDescription() {
        return "Increased base armor and knockback resistance at the cost of movement speed.";
    }
}
