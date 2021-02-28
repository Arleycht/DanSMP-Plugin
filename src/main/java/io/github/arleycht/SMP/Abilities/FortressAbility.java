package io.github.arleycht.SMP.Abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

public class FortressAbility extends Ability {
    public static final double ADD_ARMOR = 5.0;
    public static final double ADD_KNOCKBACK_RESISTANCE = 0.6;
    public static final double MOVEMENT_SPEED_MODIFIER = -0.15;

    @Override
    public void applyAttributeModifiers(Player player) {
        AttributeInstance armor = player.getAttribute(Attribute.GENERIC_ARMOR);
        AttributeInstance knockbackResistance = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        AttributeInstance speed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        AttributeModifier.Operation add = AttributeModifier.Operation.ADD_NUMBER;
        AttributeModifier.Operation mul = AttributeModifier.Operation.ADD_SCALAR;

        armor.addModifier(new AttributeModifier(getName(), ADD_ARMOR, add));
        knockbackResistance.addModifier(new AttributeModifier(getName(), ADD_KNOCKBACK_RESISTANCE, add));
        speed.addModifier(new AttributeModifier(getName(), MOVEMENT_SPEED_MODIFIER, mul));
    }

    @Override
    public String getName() {
        return "« Walking Fortress »";
    }

    @Override
    public String getDescription() {
        return "Increased base armor and knockback resistance at the cost of movement speed.";
    }
}
