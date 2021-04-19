package io.github.arleycht.SMP.Abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;

public class SpeedAbility extends Ability {
    public static final double ADD_SPEED = 1.2;

    @Override
    public void initialize() {
        Operation mul = Operation.ADD_SCALAR;

        addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, ADD_SPEED, mul);
    }

    @Override
    public String getName() {
        return "I am Speed";
    }

    @Override
    public String getDescription() {
        return "I am the embodiment of speed, travelling incarnate. By my will the world moves beneath me.";
    }
}
