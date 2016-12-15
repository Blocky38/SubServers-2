package net.ME1312.SubServers.Proxy.Network.Packet;

import net.ME1312.SubServers.Proxy.Library.UniversalFile;
import net.ME1312.SubServers.Proxy.Library.Version.Version;
import net.ME1312.SubServers.Proxy.Network.Client;
import net.ME1312.SubServers.Proxy.Network.PacketIn;
import net.ME1312.SubServers.Proxy.Network.PacketOut;
import net.ME1312.SubServers.Proxy.SubPlugin;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;

public class PacketDownloadBuildScript implements PacketIn, PacketOut {
    private SubPlugin plugin;
    private String id;

    public PacketDownloadBuildScript(SubPlugin plugin) {
        this.plugin = plugin;
    }
    public PacketDownloadBuildScript(SubPlugin plugin, String id) {
        this.plugin = plugin;
        this.id = id;
    }

    @Override
    public JSONObject generate() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        try {
            LinkedList<String> list = new LinkedList<String>();
            BufferedReader script = new BufferedReader(new FileReader(new UniversalFile(plugin.dir, "SubServers:build.sh")));
            String line;
            while ((line = script.readLine()) != null) {
                list.add(line);
            }
            script.close();
            json.put("Script", list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void execute(Client client, JSONObject data) {
        client.sendPacket(new PacketDownloadBuildScript(plugin, (data != null && data.keySet().contains("id"))?data.getString("id"):null));
    }

    @Override
    public Version getVersion() {
        return new Version("2.11.0a");
    }
}