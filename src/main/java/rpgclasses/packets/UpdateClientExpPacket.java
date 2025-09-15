package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.ui.CustomUIManager;

import java.util.Objects;

public class UpdateClientExpPacket extends Packet {

    public final int uniqueID;
    public final String playerName;
    public final int exp;

    public UpdateClientExpPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        uniqueID = reader.getNextInt();
        playerName = reader.getNextString();
        exp = reader.getNextInt();
    }

    public UpdateClientExpPacket(PlayerData playerData) {
        this.uniqueID = playerData.playerUniqueID;
        this.playerName = playerData.playerName;
        this.exp = playerData.getBaseExp();

        PacketWriter writer = new PacketWriter(this);

        writer.putNextInt(uniqueID);
        writer.putNextString(playerName);
        writer.putNextInt(exp);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        if (client.getPlayer() != null) {
            PlayerData playerData = PlayerDataList.getPlayerData(uniqueID, playerName, false);
            if(playerData != null) {
                playerData.loadDataExp(exp);
                if (Objects.equals(client.getPlayer().getUniqueID(), uniqueID)) {
                    CustomUIManager.expBar.updateExpBar(playerData);
                }
            }
        }
    }
}