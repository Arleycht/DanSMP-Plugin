package io.github.arleycht.SMP.Commands;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

public class SMPCommandCompletion implements Listener {
    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!event.getBuffer().equalsIgnoreCase("/smp ")) {
            return;
        }

        //ArrayList<String> completions = new ArrayList<>();

        // TODO: Have commands to tab complete

        //event.setCompletions(completions);
    }
}
