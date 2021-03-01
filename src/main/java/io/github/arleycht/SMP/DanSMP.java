package io.github.arleycht.SMP;

import io.github.arleycht.SMP.Abilities.*;
import io.github.arleycht.SMP.Characters.ActorRegistry;
import io.github.arleycht.SMP.Commands.SMPCommandExecutor;
import org.bukkit.command.CommandExecutor;
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

    private void initializeCommands() {
        registerCommand(SMPCommandExecutor.COMMAND_NAME, new SMPCommandExecutor());
    }

    private void initializeCharacters() {
        ActorRegistry.setPlugin(this);

        // TODO: Have these stored in a JSON file or something, yikes
        ActorRegistry.addActor("Angela", "AkinaS0");
        ActorRegistry.addActor("Max", "ALCleveland");
        ActorRegistry.addActor("Amaan", "AmaanKillzAll");
        ActorRegistry.addActor("Alex", "Arleycht");
        ActorRegistry.addActor("Daniel", "BleuInfern");
        ActorRegistry.addActor(null, "Brother1n4rms");
        ActorRegistry.addActor("Isaias", "Captain_Cheeks");
        ActorRegistry.addActor("Jeff", "Cuquirimonga");
        ActorRegistry.addActor("Sage", "fluff_sheepie");
        ActorRegistry.addActor("Steven", "hy_phon");
        ActorRegistry.addActor("Justin", "Ithaca44");
        ActorRegistry.addActor("Artemio", "Kaos_Greed");
        ActorRegistry.addActor("Brandon", "Kaos_Lust7");
        ActorRegistry.addActor("Sergio", "Kaos_Pride");
        ActorRegistry.addActor("Tien", "KingZedx");
        ActorRegistry.addActor("Tori", "kittysnitch69");
        ActorRegistry.addActor("Chris", "L1ghtEater");
        ActorRegistry.addActor(null, "lil_gloomi");
        ActorRegistry.addActor("Alfredo", "M0MMYM1LKRS");
        ActorRegistry.addActor("Andy", "MustardIsBad");
        ActorRegistry.addActor(null, "MuyJugoso");
        ActorRegistry.addActor("Nyla", "nylahyuga");
        ActorRegistry.addActor(null, "per_seph_one");
        ActorRegistry.addActor(null, "philly4321");
        ActorRegistry.addActor("Shelby", "shelbyharrisss");
        ActorRegistry.addActor(null, "Sonacrownguard");
        ActorRegistry.addActor("Kai-Lan", "squishyssb");
        ActorRegistry.addActor(null, "TacoSword");
        ActorRegistry.addActor("Mario", "TDACerberus");
        ActorRegistry.addActor(null, "YungWaffleFry");

        getLogger().info("DanSMP finished initialization!");
    }

    private void initializeAbilities() {
        AbilityRegistry.setPlugin(this);

        // TODO: Have these stored in JSON files too, my goodness
        AbilityRegistry.registerAbility("BleuInfern", CreepyManAbility.class, this);
        AbilityRegistry.registerAbility("Captain_Cheeks", BloodExchangeAbility.class, this);
        AbilityRegistry.registerAbility("Kaos_Greed", FortressAbility.class, this);
        AbilityRegistry.registerAbility("L1ghtEater", DarkAbility.class, this);
        AbilityRegistry.registerAbility("Kaos_Pride", LightAbility.class, this);
        AbilityRegistry.registerAbility("fluff_sheepie", SheepAbility.class, this);
        AbilityRegistry.registerAbility("kittysnitch69", BeeAbility.class, this);
    }

    private void registerCommand(String command, CommandExecutor executor) {
        if (command == null) {
            String msg = "Attempted to register a null command to executor of class '%s'!";
            getLogger().severe(String.format(msg, String.valueOf(executor.getClass())));

            return;
        } else if (executor == null) {
            String msg = "Attempted to register command '%s' to a null executor!";
            getLogger().severe(String.format(msg, command));

            return;
        }

        this.getCommand(command).setExecutor(executor);

        getLogger().info(String.format("Registered command '%s'", command));
    }
}
