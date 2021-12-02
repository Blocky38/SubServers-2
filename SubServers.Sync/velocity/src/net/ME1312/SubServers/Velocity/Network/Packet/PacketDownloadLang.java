package net.ME1312.SubServers.Velocity.Network.Packet;

import net.ME1312.Galaxi.Library.Container.ContainedPair;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.SubData.Client.Protocol.PacketObjectIn;
import net.ME1312.SubData.Client.Protocol.PacketOut;
import net.ME1312.SubData.Client.SubDataSender;
import net.ME1312.SubServers.Velocity.ExProxy;
import net.ME1312.SubServers.Velocity.Library.Compatibility.Logger;

import java.util.Calendar;

/**
 * Download Lang Packet
 */
public class PacketDownloadLang implements PacketObjectIn<Integer>, PacketOut {
    private ExProxy plugin;

    /**
     * New PacketDownloadLang (In)
     *
     * @param plugin SubServers.Client
     */
    public PacketDownloadLang(ExProxy plugin) {
        Util.nullpo(plugin);
        this.plugin = plugin;
    }

    /**
     * New PacketDownloadLang (Out)
     */
    public PacketDownloadLang() {}

    @Override
    public void receive(SubDataSender client, ObjectMap<Integer> data) {
        try {
            Util.reflect(ExProxy.class.getDeclaredField("lang"), plugin, new ContainedPair<>(Calendar.getInstance().getTime().getTime(), data.getObject(0x0001)));
            Logger.get("SubData").info("Lang Settings Downloaded");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
