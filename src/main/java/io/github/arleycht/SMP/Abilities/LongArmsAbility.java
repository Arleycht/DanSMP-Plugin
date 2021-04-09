package io.github.arleycht.SMP.Abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

public class LongArmsAbility extends Ability {
    public static final double ADD_SPEED = 0.33;

    @Override
    public void initialize() {
        AttributeModifier.Operation mul = AttributeModifier.Operation.ADD_SCALAR;

        addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, ADD_SPEED, mul);
    }

    @Override
    public String getName() {
        return "Long Arms";
    }

    @Override
    public String getDescription() {
        return "Increased speed lol";
    }
}
