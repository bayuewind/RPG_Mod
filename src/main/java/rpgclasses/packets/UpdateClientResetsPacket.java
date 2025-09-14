package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class UpdateClientResetsPacket extends Packet {

    public final int uniqueID;
    public final int resets;

    public UpdateClientResetsPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        uniqueID = reader.getNextInt();
        resets = reader.getNextInt();
    }

    public UpdateClientResetsPacket(PlayerData playerData) {
        this.uniqueID = playerData.playerUniqueID;
        this.resets = playerData.getResets();

        PacketWriter writer = new PacketWriter(this);

        writer.putNextInt(uniqueID);
        writer.putNextInt(resets);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(uniqueID, false);
        playerData.loadDataResets(resets);
    }
}