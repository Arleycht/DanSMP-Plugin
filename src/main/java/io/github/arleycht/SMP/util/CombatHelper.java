package io.github.arleycht.SMP.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class CombatHelper {
    public static double getFinalDamage(double damage, LivingEntity entity, DamageCause cause) {
        damage = getDamageAfterArmor(damage, entity);
        damage = getDamageAfterMagicAbsorb(damage, entity, cause);

        return damage;
    }

    public static double getDamageAfterArmor(double damage, LivingEntity entity) {
        AttributeInstance armorAttribute = entity.getAttribute(Attribute.GENERIC_ARMOR);
        AttributeInstance toughnessAttribute = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);

        double armor = 0.0;
        double toughness = 0.0;

        if (armorAttribute != null) {
            armor = armorAttribute.getValue();
        }

        if (toughnessAttribute != null) {
            toughness = toughnessAttribute.getValue();
        }

        double reduction = armor - ((damage * 4.0) / (toughness + 8.0));
        reduction = Math.min(20.0, Math.max(armor * 0.2, reduction)) * 0.04;

        damage *= 1.0 - reduction;

        return damage;
    }

    public static double getDamageAfterMagicAbsorb(double damage, LivingEntity entity, DamageCause cause) {
        // Apply resistance reduction

        PotionEffect effect = entity.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);

        if (effect != null) {
            damage *= 1.0 - ((effect.getAmplifier() + 1) * 0.2);
        }

        if (damage < 0.0) {
            return 0.0;
        }

        // Apply protection reductions

        double protectionLevel = getEnchantmentModifiers(entity.getEquipment(), cause);
        double reduction = Math.min(0.8, Math.max(0.0, protectionLevel * 0.04));

        damage *= 1.0 - reduction;

        return damage;
    }

    public static int getEnchantmentModifiers(EntityEquipment equipment, DamageCause cause) {
        if (equipment == null) {
            return 0;
        }

        int total = 0;

        ItemStack[] contents = equipment.getArmorContents();

        for (ItemStack itemStack : contents) {
            if (itemStack != null) {
                for (Map.Entry<Enchantment, Integer> e : itemStack.getEnchantments().entrySet()) {
                    total += getProtectionLevel(e.getKey(), e.getValue(), cause);
                }
            }
        }

        return total;
    }

    /**
     *
     * @param type Protection type
     * @param level Enchantment level
     * @param cause Damage cause
     * @return Protection level
     */
    public static int getProtectionLevel(Enchantment type, int level, DamageCause cause) {
        if (type.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) {
            return level;
        } else if (type.equals(Enchantment.PROTECTION_FIRE) && isFireDamage(cause)) {
            return level * 2;
        } else if (type.equals(Enchantment.PROTECTION_FALL) && cause == DamageCause.FALL) {
            return level * 3;
        } else if (type.equals(Enchantment.PROTECTION_EXPLOSIONS) && isExplosionDamage(cause)) {
            return level * 2;
        } else if (type.equals(Enchantment.PROTECTION_PROJECTILE) && cause == DamageCause.PROJECTILE) {
            return level * 2;
        }

        return 0;
    }

    public static boolean isFireDamage(DamageCause cause) {
        return cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK;
    }

    public static boolean isExplosionDamage(DamageCause cause) {
        return cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION;
    }
}
