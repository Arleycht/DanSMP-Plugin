package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Characters.Actor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class Ability implements Listener, Runnable {
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

    public boolean isOwner(@Nullable UUID uuid) {
        if (uuid == null) {
            return false;
        }

        return uuid.equals(owner.getUniqueId());
    }

    public boolean isOwner(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }

        return isOwner(entity.getUniqueId());
    }

    public abstract String getName();
    public abstract String getDescription();

    public AttributeModifier[] getAttributeModifiers(Attribute attribute) {
        if (attributeModifiers.containsKey(attribute)) {
            ArrayList<AttributeModifier> modifiers = attributeModifiers.get(attribute);

            return modifiers.toArray(new AttributeModifier[modifiers.size()]);
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
