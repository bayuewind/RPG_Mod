package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class UpdateClientClassesPacket extends Packet {

    public final int uniqueID;
    public final int[] classes;

    public UpdateClientClassesPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        uniqueID = reader.getNextInt();
        classes = new int[PlayerClass.classes.size()];
        for (int i = 0; i < PlayerClass.classes.size(); i++) {
            classes[i] = reader.getNextInt();
        }
    }

    public UpdateClientClassesPacket(PlayerData playerData) {
        this.uniqueID = playerData.playerUniqueID;
        this.classes = playerData.getClassLevels();

        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(uniqueID);
        for (int i = 0; i < PlayerClass.classes.size(); i++) {
            writer.putNextInt(playerData.getClassLevel(i));
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(uniqueID, false);
        playerData.setClassLevels(classes);
    }
}