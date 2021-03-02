package io.github.arleycht.SMP.Abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

public class FortressAbility extends Ability {
    public static final double ADD_ARMOR = 5.0;
    public static final double KNOCKBACK_RESISTANCE_ADD = 0.4;
    public static final double MOVEMENT_SPEED_MUL = -0.15;
    public static final double ATTACK_SPEED_MUL = -0.1;

    @Override
    public void initialize() {
        AttributeModifier.Operation add = AttributeModifier.Operation.ADD_NUMBER;
        AttributeModifier.Operation mul = AttributeModifier.Operation.ADD_SCALAR;

        addAttributeModifier(Attribute.GENERIC_ARMOR, ADD_ARMOR, add);
        addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_ADD, add);
        addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED_MUL, mul);
        addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, ATTACK_SPEED_MUL, mul);
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
