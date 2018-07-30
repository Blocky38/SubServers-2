package net.ME1312.SubServers.Client.Sponge.Event;

import net.ME1312.SubServers.Client.Sponge.Library.Config.YAMLSection;
import net.ME1312.SubServers.Client.Sponge.Library.Config.YAMLValue;
import net.ME1312.SubServers.Client.Sponge.Library.NamedContainer;
import net.ME1312.SubServers.Client.Sponge.Library.SubEvent;
import net.ME1312.SubServers.Client.Sponge.Library.Util;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.UUID;

/**
 * Server Edit Event
 */
public class SubEditServerEvent extends AbstractEvent implements SubEvent {
    private UUID player;
    private String server;
    private NamedContainer<String, YAMLValue> edit;
    private boolean perm;

    /**
     * Server Edit Event
     *
     * @param player Player Adding Server
     * @param server Server to be Edited
     * @param edit Edit to make
     * @param permanent If the change is permanent
     */
    public SubEditServerEvent(UUID player, String server, NamedContainer<String, ?> edit, boolean permanent) {
        if (Util.isNull(server, edit)) throw new NullPointerException();
        YAMLSection section = new YAMLSection();
        section.set(".", edit.get());
        this.player = player;
        this.server = server;
        this.edit = new NamedContainer<String, YAMLValue>(edit.name(), section.contains(".")?section.get("."):null);
        this.perm = permanent;
    }

    /**
     * Gets the Server to be Edited
     *
     * @return The Server to be Edited
     */
    public String getServer() { return server; }

    /**
     * Gets the player that triggered the Event
     *
     * @return The Player that triggered this Event or null if Console
     */
    public UUID getPlayer() { return player; }

    /**
     * Gets the edit to be made
     *
     * @return Edit to be made
     */
    public NamedContainer<String, YAMLValue> getEdit() {
        return edit;
    }

    /**
     * Gets if the edit is permanent
     *
     * @return Permanent Status
     */
    public boolean isPermanent() {
        return perm;
    }

    /**
     * Gets the cause of this Event
     *
     * @deprecated Use simplified methods where available
     * @return The player who triggered this event under the key: player
     */
    @Override
    @Deprecated
    public Cause getCause() {
        return Cause.builder().named("player", player).build();
    }
}
