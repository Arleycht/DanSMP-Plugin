package io.github.arleycht.SMP.Factions;

import io.github.arleycht.SMP.Characters.Actor;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class FactionsManager {
    private static HashMap<String, Set<Actor>> FACTIONS = new HashMap<>();

    public static void AddMember(String factionName, Actor actor) {
        FACTIONS.get(factionName).add(actor);
    }

    public static boolean IsMember(String factionName, Actor actor) {
        return FACTIONS.get(factionName).contains(actor);
    }

    public static boolean IsMember(String factionName, UUID uuid) {
        /*for (Map.Entry<String, Set<Actor>> entry : FACTIONS.entrySet()) {

        }*/

        return false;
    }
}
