package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Characters.Actor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Ability implements Listener, Runnable {
    public static final String NO_NAME = "Blank Ability";
    public static final String NO_DESCRIPTION = "No description.";

    protected Plugin plugin;
    protected Actor owner;

    protected HashMap<Attribute, ArrayList<AttributeModifier>> attributeModifiers;

    public Ability() {
        attributeModifiers = new HashMap<>();
    }

    public void initialize() {

    }

    public boolean isRunnable() {
        return false;
    }

    public long getTaskIntervalTicks() {
        return -1L;
    }

    @Override
    public void run() {

    }

    public boolean isOwner(Entity entity) {
        return owner.getUsername().equalsIgnoreCase(entity.getName()) || owner.getUniqueId().equals(entity.getUniqueId());
    }

    public String getName() {
        return NO_NAME;
    }

    public String getDescription() {
        return NO_DESCRIPTION;
    }

    public AttributeModifier[] getAttributeModifiers(Attribute attribute) {
        if (attributeModifiers.containsKey(attribute)) {
            ArrayList<AttributeModifier> modifiers = attributeModifiers.get(attribute);
            AttributeModifier[] modifierArray = new AttributeModifier[modifiers.size()];

            return modifiers.toArray(modifierArray);
        }

        return new AttributeModifier[0];
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Actor getOwner() {
        return owner;
    }

    public void addAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        AttributeModifier modifier = new AttributeModifier(AbilityRegistry.ABILITY_ATTRIBUTE_MODIFIER_NAME, amount, operation);

        if (!attributeModifiers.containsKey(attribute)) {
            attributeModifiers.put(attribute, new ArrayList<>());
        }

        attributeModifiers.get(attribute).add(modifier);
    }

    public void clearAttributeModifiers() {
        attributeModifiers.clear();
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public void setOwner(Actor owner) {
        this.owner = owner;
    }
}
