package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.GameUtils;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.ui.CustomUIManager;
import rpgclasses.ui.RPGSkillUIManager;

import java.util.Objects;

public class LoadPlayerDataPacket extends Packet {

    public final String name;
    public final PlayerData playerData;

    public LoadPlayerDataPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        name = reader.getNextString();

        if (reader.hasNext()) {
            playerData = PlayerData.applySpawnPacket(reader);
        } else {
            playerData = null;
        }
    }

    public LoadPlayerDataPacket(String name, PlayerData playerData) {
        this.name = name;
        this.playerData = playerData;

        PacketWriter writer = new PacketWriter(this);

        writer.putNextString(name);
        if (playerData != null) {
            playerData.setupSpawnPacket(writer);
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerDataList.setPlayerData(name, playerData, false);
        if (Objects.equals(client.getPlayer().playerName, name)) {
            CustomUIManager.expBar.updateExpBar(playerData);
            RPGSkillUIManager.updateContent(playerData);
        }
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        PlayerData playerData = PlayerDataList.getPlayerData(name, true);
        GameUtils.streamServerClients(server, client.getLevel()).forEach(c -> c.sendPacket(new LoadPlayerDataPacket(name, playerData)));
        if (Objects.equals(client.playerMob.playerName, name)) {
            playerData.updateAllBuffs(client.playerMob);
        }
    }
}