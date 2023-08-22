package fi.dy.masa.servux.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.base.Charsets;
import fi.dy.masa.servux.network.packet.StructureDataPacketHandler;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import fi.dy.masa.servux.network.util.PacketUtils;

public class ServerPacketChannelHandler
{
    public static final ServerPacketChannelHandler INSTANCE = new ServerPacketChannelHandler();

    private ServerPacketChannelHandler()
    {
        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) ->
                channels.forEach(channel -> {
                    if (channel.equals(StructureDataPacketHandler.CHANNEL)) {
                        this.updateRegistrationForChannels(StructureDataPacketHandler.INSTANCE::subscribe, handler);
                    }}));

        S2CPlayChannelEvents.UNREGISTER.register((handler, sender, server, channels) ->
                channels.forEach(channel -> {
                    if (channel.equals(StructureDataPacketHandler.CHANNEL)) {
                        this.updateRegistrationForChannels(StructureDataPacketHandler.INSTANCE::unsubscribe, handler);
                    }}));
    }

    public void registerServerChannelHandler()
    {
        StructureDataPacketHandler.INSTANCE.isEnable = true;
    }

    public void unregisterServerChannelHandler()
    {
        StructureDataPacketHandler.INSTANCE.isEnable = false;
    }

    private void updateRegistrationForChannels(Consumer<ServerPlayNetworkHandler> action,
                                               ServerPlayNetworkHandler netHandler)
    {
        action.accept(netHandler);
    }

    private void schedule(Runnable task, ServerPlayNetworkHandler netHandler)
    {
        netHandler.getPlayer().getServer().execute(task);
    }

    private static List<Identifier> getChannels(PacketByteBuf buf)
    {
        buf.readerIndex(0);
        buf = PacketUtils.slice(buf);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String channelString = new String(bytes, Charsets.UTF_8);
        List<Identifier> channels = new ArrayList<>();

        for (String channel : channelString.split("\0"))
        {
            try
            {
                Identifier id = new Identifier(channel);
                channels.add(id);
            }
            catch (Exception ignore) {}
        }

        return channels;
    }
}
