package net.ME1312.SubServers.Host.API.Event;

import net.ME1312.SubServers.Host.Executable.SubCreator;
import net.ME1312.SubServers.Host.Library.Event.Event;
import net.ME1312.SubServers.Host.Library.Util;
import net.ME1312.SubServers.Host.Library.Version.Version;

import java.util.UUID;

/**
 * Server Create Event
 */
public class SubCreateEvent extends Event {
    private UUID player;
    private String host;
    private String name;
    private SubCreator.ServerType type;
    private Version version;
    private int memory;
    private int port;

    /**
     * Server Create Event
     *
     * @param player Player Creating
     * @param host Potential Host
     * @param name Server Name
     * @param type Server Type
     * @param version Server Version
     * @param memory Server RAM Amount
     * @param port Server Port Number
     */
    public SubCreateEvent(UUID player, String host, String name, SubCreator.ServerType type, Version version, int memory, int port) {
        if (Util.isNull(host, name, type, version, memory, port)) throw new NullPointerException();
        this.player = player;
        this.host = host;
        this.name = name;
        this.type = type;
        this.version = version;
        this.memory = memory;
        this.port = port;
    }

    /**
     * Get the Host the SubServer will run on
     *
     * @return Potential Host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the name the SubServer will use
     *
     * @return SubServer Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of Server to create
     *
     * @return Server Type
     */
    public SubCreator.ServerType getType() {
        return type;
    }

    /**
     * Set the Type of Server to Create
     *
     * @param value Value
     */
    public void setType(SubCreator.ServerType value) {
        this.type = value;
    }

    /**
     * Get the Version the Server will use
     *
     * @return Server Version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Set the Version the Server will use
     *
     * @param value Value
     */
    public void setVersion(Version value) {
        this.version = value;
    }

    /**
     * Get the Server RAM Amount (in MB)
     *
     * @return RAM Amount
     */
    public int getMemory() {
        return memory;
    }

    /**
     * Set the Server RAM Amount (in MB)
     *
     * @param value Value
     */
    public void setMemory(int value) {
        this.memory = value;
    }

    /**
     * Get the Port the Server will use
     *
     * @return Port Number
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the player that triggered the Event
     *
     * @return The Player that triggered this Event or null if Console
     */
    public UUID getPlayer() { return player; }
}
