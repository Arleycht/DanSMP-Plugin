package io.github.arleycht.SMP;

import io.github.arleycht.SMP.Abilities.Ability;

public class OverdriveAbility extends Ability {


    @Override
    public String getName() {
        return "Current Overdrive";
    }

    @Override
    public String getDescription() {
        return "Sacrifice health for temporary buffs.";
    }
}
