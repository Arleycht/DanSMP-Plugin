package io.github.arleycht.SMP;

import io.github.arleycht.SMP.Abilities.*;
import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import io.github.arleycht.SMP.Characters.ActorRegistry;
import io.github.arleycht.SMP.Commands.SMPCommandExecutor;
import io.github.arleycht.SMP.Commands.SMPTabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class DanSMP extends JavaPlugin {
    private boolean initialized = false;

    @Override
    public void onEnable() {
        initializeAll();

        AbilityRegistry.scheduleAllAbilityTasks();

        getLogger().info("DanSMP enabled!");
    }

    @Override
    public void onDisable() {
        AbilityRegistry.cancelAllAbilityTasks();

        getLogger().info("DanSMP disabled!");
    }

    private void initializeAll() {
        if (!initialized) {
            initialized = true;

            initializeCommands();
            initializeCharacters();
            initializeAbilities();
        } else {
            getLogger().info("DanSMP already initialized!");

            return;
        }
    }

    private void initializeConfiguration() {
        // TODO
        saveDefaultConfig();
    }

    private void initializeCommands() {
        registerCommand(SMPCommandExecutor.COMMAND_NAME, new SMPCommandExecutor(), new SMPTabCompleter());
    }

    private void initializeCharacters() {
        ActorRegistry.setPlugin(this);

        // TODO: Have these stored in a JSON file or something, yikes

        // Alphabetically organized by name

        ActorRegistry.addActor("Alex", "99ff480d-e626-4065-a5c7-6cc89354eda4");
        ActorRegistry.addActor("Alfredo", "38772632-6d47-4050-a113-86d45528a269");
        ActorRegistry.addActor("Amaan", "e5fe7e3f-c0a4-471b-a80e-93427db64195");
        ActorRegistry.addActor("Andy", "c3189dec-dcad-4de8-b92a-8063f8a26c50");
        ActorRegistry.addActor("Angela", "1253ff32-ff8f-4d6e-a23f-6f776d8d872d");
        ActorRegistry.addActor("Artemio", "39be03c0-a813-4428-84ca-90b85f4b5657");

        ActorRegistry.addActor("Brandon", "22d85d07-600a-4764-bac1-b28055ade039");

        ActorRegistry.addActor("Chris", "5e454c3a-2eeb-4cea-b899-909c901ce41f");

        ActorRegistry.addActor("Daniel", "d0fc999c-c8c9-4962-a745-cbefdf8ef9c6");
        ActorRegistry.addActor("Daryll", "47e4e7d9-afba-44d8-a14d-7d1fc2fa8a74");

        ActorRegistry.addActor("Isaias", "ca8073ee-0901-4e99-a7cb-7a0b70af361e");

        ActorRegistry.addActor("Jeff", "8f67331d-2036-4e6f-9df5-c56497eed54a");
        ActorRegistry.addActor("Justin", "125aae72-7853-4af0-9761-d5d2dfbf1540");

        ActorRegistry.addActor("Kai-Lan", "b908eef4-afb4-4010-b825-c0703ecf87e3");

        ActorRegistry.addActor("Max", "8b538c12-9a12-4db1-8eeb-243fca3f4bcd");
        ActorRegistry.addActor("Mario", "ff1a9c40-e6b4-425e-ad75-ca721897c302");

        ActorRegistry.addActor("Nyla", "4b8c2fd4-3cf4-4b77-9254-875c911806e6");

        ActorRegistry.addActor("Reginald", "f0606323-6f95-4b1a-9ca0-703c1f10ef2f");

        ActorRegistry.addActor("Sage", "d8e15ac6-a9fa-421d-93b1-6d0e98c14dff");
        ActorRegistry.addActor("Sergio", "57a22258-0e09-48a9-84af-4e888b5ab2e0");
        ActorRegistry.addActor("Shelby", "ebbe93a2-6940-48b8-a49c-9b7befa4da9c");
        ActorRegistry.addActor("Steven", "774c3ecc-fad5-4db8-b97b-18de5cc01ec6");

        ActorRegistry.addActor("Tien", "21104e89-c8b3-4a46-9de9-d6ae5474b184");

        ActorRegistry.addActor("Victoria", "64f3ca8e-1cf0-4233-8dfe-a68be57670ab");

        // People whose names aren't known

        ActorRegistry.addActor("Brother1n4rms", "2a24765c-448c-4193-a254-fa9614a534d2");
        ActorRegistry.addActor("TacoSword", "86a4b33d-b8ed-46fc-9997-eea75ae2e946");

        // People who are not active on the server

        ActorRegistry.addActor("lil_gloomi", "8987b1c1-6726-46bb-a465-be0e1c9cf19e");
        ActorRegistry.addActor("MuyJugoso", "cf719917-ff6a-4606-80a7-32ab512ea10c");
        ActorRegistry.addActor("per_seph_one", "1a615087-022b-4474-8956-0c826f10e018");
        ActorRegistry.addActor("philly4321", "e7ac59d2-68ec-4474-810b-662602f78c2a");
        ActorRegistry.addActor("Sonacrownguard", "c4138cb6-1701-4550-8263-5217485c8e4d");
        ActorRegistry.addActor("YungWaffleFry", "ee6846d1-dcae-416e-8e9a-b944cb60777e");

        getLogger().info("DanSMP finished initialization!");
    }

    private void initializeAbilities() {
        AbilityRegistry.setPlugin(this);

        getServer().getPluginManager().registerEvents(DeathMessageManager.getInstance(), this);

        // TODO: Have these stored in JSON files too, my goodness

        AbilityRegistry.registerAbility("Daniel", CreeperAbility.class, this);
        AbilityRegistry.registerAbility("Isaias", PaladinAbility.class, this);
        AbilityRegistry.registerAbility("Artemio", FortressAbility.class, this);
        AbilityRegistry.registerAbility("Chris", DarkAbility.class, this);
        AbilityRegistry.registerAbility("Sergio", LightAbility.class, this);
        AbilityRegistry.registerAbility("Sage", SheepAbility.class, this);
        AbilityRegistry.registerAbility("Victoria", BeeAbility.class, this);
        AbilityRegistry.registerAbility("Nyla", MooshroomAbility.class, this);
        AbilityRegistry.registerAbility("Tien", SkyWandererAbility.class, this);
        AbilityRegistry.registerAbility("Angela", WyvernAbility.class, this);
        AbilityRegistry.registerAbility("Alfredo", CupidAbility.class, this);
        AbilityRegistry.registerAbility("Steven", OverdriveAbility.class, this);
        AbilityRegistry.registerAbility("Brother1n4rms", LongArmsAbility.class, this);
        AbilityRegistry.registerAbility("Daryll", EndermanAbility.class, this);
        AbilityRegistry.registerAbility("Reginald", PunchAbility.class, this);
        AbilityRegistry.registerAbility("Brandon", LightningAbility.class, this);
        AbilityRegistry.registerAbility("Riku", GhostAbility.class, this);
    }

    private void registerCommand(String command, CommandExecutor executor, TabCompleter tabCompleter) {
        if (command == null) {
            String msg = "Attempted to register a null command to executor of class '%s'!";
            getLogger().severe(String.format(msg, executor.getClass()));

            return;
        } else if (executor == null) {
            String msg = "Attempted to register command '%s' to a null executor!";
            getLogger().severe(String.format(msg, command));

            return;
        }

        PluginCommand pluginCommand = this.getCommand(command);

        if (pluginCommand == null) {
            String msg = "Command '%s' was not found! Maybe it is missing from plugin.yml?";
            getLogger().severe(String.format(msg, command));

            return;
        }

        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(tabCompleter);

        getLogger().info(String.format("Registered command '%s'", command));
    }
}
