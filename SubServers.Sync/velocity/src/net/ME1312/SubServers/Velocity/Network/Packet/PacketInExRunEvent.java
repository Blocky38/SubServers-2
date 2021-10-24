package net.ME1312.SubServers.Velocity.Network.Packet;

import net.ME1312.Galaxi.Library.Container.ContainedPair;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.SubData.Client.Protocol.PacketObjectIn;
import net.ME1312.SubData.Client.SubDataSender;
import net.ME1312.SubServers.Velocity.Event.*;
import net.ME1312.SubServers.Velocity.ExProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Run Event Packet
 */
public class PacketInExRunEvent implements PacketObjectIn<Integer> {
    private static HashMap<String, List<Consumer<ObjectMap<String>>>> callbacks = new HashMap<String, List<Consumer<ObjectMap<String>>>>();

    /**
     * New PacketInExRunEvent
     */
    public PacketInExRunEvent(ExProxy plugin) {
        callback("SubAddHostEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubAddHostEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host")));
                callback("SubAddHostEvent", this);
            }
        });
        callback("SubAddProxyEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubAddProxyEvent(data.getRawString("proxy")));
                callback("SubAddProxyEvent", this);
            }
        });
        callback("SubAddServerEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubAddServerEvent((data.contains("player"))?data.getUUID("player"):null, (data.contains("host"))?data.getRawString("host"):null, data.getRawString("server")));
                callback("SubAddServerEvent", this);
            }
        });
        callback("SubCreateEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubCreateEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host"), data.getRawString("name"),
                        data.getRawString("template"), data.getVersion("version"), data.getInt("port"), data.getBoolean("update")));
                callback("SubCreateEvent", this);
            }
        });
        callback("SubCreatedEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubCreatedEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host"), data.getRawString("name"),
                        data.getRawString("template"), data.getVersion("version"), data.getInt("port"), data.getBoolean("update"), data.getBoolean("success")));
                callback("SubCreatedEvent", this);
            }
        });
        callback("SubSendCommandEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubSendCommandEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), data.getRawString("command")));
                callback("SubSendCommandEvent", this);
            }
        });
        callback("SubEditServerEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubEditServerEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), new ContainedPair<String, Object>(data.getRawString("edit"), data.get("value"))));
                callback("SubEditServerEvent", this);
            }
        });
        callback("SubStartEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubStartEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server")));
                callback("SubStartEvent", this);
            }
        });
        callback("SubStartedEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubStartedEvent(data.getRawString("server")));
                callback("SubStartedEvent", this);
            }
        });
        callback("SubStopEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubStopEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("server"), data.getBoolean("force")));
                callback("SubStopEvent", this);
            }
        });
        callback("SubStoppedEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubStoppedEvent(data.getRawString("server")));
                callback("SubStoppedEvent", this);
            }
        });
        callback("SubRemoveServerEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubRemoveServerEvent((data.contains("player"))?data.getUUID("player"):null, (data.contains("host"))?data.getRawString("host"):null, data.getRawString("server")));
                callback("SubRemoveServerEvent", this);
            }
        });
        callback("SubRemoveProxyEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubAddProxyEvent(data.getRawString("proxy")));
                callback("SubRemoveProxyEvent", this);
            }
        });
        callback("SubRemoveHostEvent", new Consumer<ObjectMap<String>>() {
            @Override
            public void accept(ObjectMap<String> data) {
                ExProxy.getInstance().getEventManager().fire(new SubRemoveHostEvent((data.contains("player"))?data.getUUID("player"):null, data.getRawString("host")));
                callback("SubRemoveHostEvent", this);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void receive(SubDataSender client, ObjectMap<Integer> data) {
        if (callbacks.keySet().contains(data.getString(0x0000))) {
            List<Consumer<ObjectMap<String>>> callbacks = PacketInExRunEvent.callbacks.get(data.getString(0x0000));
            PacketInExRunEvent.callbacks.remove(data.getString(0x0000));
            for (Consumer<ObjectMap<String>> callback : callbacks) {
                callback.accept(new ObjectMap<>((Map<String, ?>) data.getObject(0x0001)));
            }
        }
    }

    @Override
    public int version() {
        return 0x0001;
    }

    public static void callback(String event, Consumer<ObjectMap<String>> callback) {
        List<Consumer<ObjectMap<String>>> callbacks = (PacketInExRunEvent.callbacks.keySet().contains(event))? PacketInExRunEvent.callbacks.get(event):new ArrayList<Consumer<ObjectMap<String>>>();
        callbacks.add(callback);
        PacketInExRunEvent.callbacks.put(event, callbacks);
    }
}
