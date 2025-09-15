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

    public final int uniqueID;
    public final String playerName;
    public final PlayerData playerData;

    public LoadPlayerDataPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        uniqueID = reader.getNextInt();
        playerName = reader.getNextString();

        if (reader.hasNext()) {
            playerData = PlayerData.applyPacket(reader);
        } else {
            playerData = null;
        }
    }

    public LoadPlayerDataPacket(int uniqueID, String playerName, PlayerData playerData) {
        this.uniqueID = uniqueID;
        this.playerName = playerName;
        this.playerData = playerData;


        PacketWriter writer = new PacketWriter(this);

        writer.putNextInt(uniqueID);
        writer.putNextString(playerName);
        if (playerData != null) {
            playerData.setupPacket(writer);
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerDataList.setPlayerData(uniqueID, playerName, playerData, false);
        if (Objects.equals(client.getPlayer().getUniqueID(), uniqueID)) {
            CustomUIManager.expBar.updateExpBar(playerData);
            RPGSkillUIManager.updateContent(playerData);
        }
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        PlayerData playerData = PlayerDataList.getPlayerData(uniqueID, playerName, true);

        long worldUniqueID = server.world.getUniqueID();

        if (playerData == null || playerData.worldUniqueID != worldUniqueID) {
            playerData = PlayerDataList.initPlayerData(uniqueID, playerName, worldUniqueID, true);
        }

        final PlayerData finalPlayerData = playerData;
        GameUtils.streamServerClients(server, client.getLevel()).forEach(c -> c.sendPacket(new LoadPlayerDataPacket(uniqueID, playerName, finalPlayerData)));
        if (Objects.equals(client.playerMob.getUniqueID(), uniqueID)) {
            finalPlayerData.updateAllBuffs(client.playerMob);
        }
    }
}