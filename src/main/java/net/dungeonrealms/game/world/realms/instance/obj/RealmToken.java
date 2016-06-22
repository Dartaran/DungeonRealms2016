package net.dungeonrealms.game.world.realms.instance.obj;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public class RealmToken {

    @Getter
    private UUID owner;

    @Getter
    @Setter
    private RealmStatus status;

    @Getter
    private Location portalLocation;

    @Getter
    private Hologram hologram;

    @Getter
    private List<UUID> playersInRealm = new ArrayList<>();

    @Getter
    private List<UUID> builders = new ArrayList<>();

}