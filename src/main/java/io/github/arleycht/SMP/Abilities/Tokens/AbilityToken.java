package io.github.arleycht.SMP.Abilities.Tokens;

import io.github.arleycht.SMP.Abilities.Ability;
import io.github.arleycht.SMP.Abilities.AbilityRegistry;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbilityToken {
    public static final String TOKEN_TITLE = "Ability Token";

    public abstract String getTokenName();

    public abstract void applyAbility(Player player);

    public abstract void removeAbility(Player player);

    /**
     * Creates a token for a player's ability.
     *
     * @param player
     * @return
     */
    public static ItemStack createToken(Player player, Ability ability) {
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) meta.clone();

            // Duplication prevention meta

            ArrayList<String> lore = new ArrayList<>();
            lore.add(String.format("« %s »", ability.getName()));

            bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
            bookMeta.setLore(lore);

            // Store ability owner information in the pages

            /*
                0 Ability Name
                1 Player UUID
                2 Player Name
             */

            ArrayList<BaseComponent[]> pages = new ArrayList<>();
            pages.add(
                    new ComponentBuilder(ability.getName())
                            .obfuscated(true)
                            .create()
            );
            pages.add(
                    new ComponentBuilder(player.getUniqueId().toString())
                            .obfuscated(true)
                            .create()
            );
            pages.add(
                    new ComponentBuilder(player.getName())
                            .obfuscated(true)
                            .create()
            );

            bookMeta.spigot().setPages(pages);

            // Aesthetics

            bookMeta.setAuthor(player.getName());
            bookMeta.setTitle(TOKEN_TITLE);

            // Set meta

            itemStack.setItemMeta(bookMeta);

            return itemStack;
        }

        Bukkit.getLogger().severe("Failed to generate ability token meta");

        return null;
    }

    public static Ability getAbilityFromToken(ItemStack itemStack) {
        if (itemStack.getType() == Material.WRITTEN_BOOK) {
            ItemMeta meta = itemStack.getItemMeta();

            if (meta instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta) meta;

                String title = bookMeta.getTitle();
                String author = bookMeta.getAuthor();
                List<String> lore = bookMeta.getLore();

                List<BaseComponent[]> pages = bookMeta.spigot().getPages();

                if (bookMeta.getGeneration() != BookMeta.Generation.ORIGINAL) {
                    Bukkit.getLogger().info("Not original!");

                    return null;
                }

                if (title == null || author == null || lore == null || lore.size() < 1 || pages.size() < 3) {
                    Bukkit.getLogger().info("Missing anti-duplication measure!");

                    return null;
                }

                BaseComponent[] abilityNamePage = pages.get(0);
                BaseComponent[] uuidPage = pages.get(1);
                BaseComponent[] usernamePage = pages.get(2);

                String abilityName;
                UUID abilityOwnerUuid;
                String username;

                try {
                    abilityName = abilityNamePage[0].toPlainText();
                    abilityOwnerUuid = UUID.fromString(uuidPage[0].toPlainText());
                    username = usernamePage[0].toPlainText();
                } catch (Exception ignored) {
                    Bukkit.getLogger().info("Missing ability owner UUID!");

                    return null;
                }

                // Check if ability exists

                Ability ability = null;

                for (Ability a : AbilityRegistry.getAbilities(abilityOwnerUuid)) {
                    if (a.getName().equals(abilityName)) {
                        ability = a;
                    }
                }

                if (ability == null) {
                    return null;
                }

                String msg = String.format("%s's (%s) ability « %s »", username, abilityOwnerUuid.toString(), abilityName);
                Bukkit.getLogger().info(msg);
            }
        }

        return null;
    }
}
