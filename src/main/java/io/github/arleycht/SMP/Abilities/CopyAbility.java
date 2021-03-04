package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.Tokens.AbilityToken;
import io.github.arleycht.SMP.util.Cooldown;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class CopyAbility extends Ability {
    public static final long KEY_ITEM_CHECK_INTERVAL = 5L * 20L;

    private final Cooldown createTokenCooldown = new Cooldown(0.5);

    private static class ItemChecker implements Runnable {
        @Override
        public void run() {
            Bukkit.getLogger().info("Performing token check");


        }
    }

    @Override
    public void initialize() {
        ItemChecker checker = new ItemChecker();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, checker, 0L, KEY_ITEM_CHECK_INTERVAL);
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        ItemStack itemStack = event.getItem().getItemStack();

        if (AbilityToken.getAbilityFromToken(itemStack) == null) {
            return;
        }

        Bukkit.getLogger().info("Picked up token!");
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItem(EquipmentSlot.HAND);
        Ability ability = AbilityToken.getAbilityFromToken(heldItem);

        if (ability == null) {
            return;
        }

        Ability[] abilities = AbilityRegistry.getAbilities(player);

        for (int i = 0; i < abilities.length; ++i) {
            if (ability.getName().equals(abilities[i].getName())) {
                Bukkit.getLogger().info(String.format("Ability index: %d", i));

                Ability selectedAbility = abilities[(i + 1) % abilities.length];

                inventory.setItem(EquipmentSlot.HAND, AbilityToken.createToken(player, selectedAbility));

                event.setCancelled(true);

                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        if (createTokenCooldown.isNotReady()) {
            return;
        }

        Player player = event.getPlayer();
        Entity targetEntity = event.getRightClicked();

        if (!player.isSneaking() || !isOwner(targetEntity)) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItem(EquipmentSlot.HAND); // Somehow not null?

        if (heldItem.getType() != Material.PAPER) {
            return;
        }

        Ability[] abilities = AbilityRegistry.getAbilities(player);

        if (abilities.length < 1) {
            String msg = "You need to have an ability in order to gift a token.";
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));

            return;
        }

        // Create token for ability

        Ability ability = abilities[0];

        ItemStack tokenItemStack = AbilityToken.createToken(player, ability);

        Map<Integer, ItemStack> excess = inventory.addItem(tokenItemStack);

        if (excess.isEmpty()) {
            heldItem.setAmount(heldItem.getAmount() - 1);
        }

        createTokenCooldown.reset();
    }

    @Override
    public String getName() {
        return "Full Roster";
    }

    @Override
    public String getDescription() {
        return "With the power of MacGuffins to drive central plot points...";
    }
}
