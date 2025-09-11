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

    public final String name;
    public final int[] classes;

    public UpdateClientClassesPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        name = reader.getNextString();
        classes = new int[PlayerClass.classes.size()];
        for (int i = 0; i < PlayerClass.classes.size(); i++) {
            classes[i] = reader.getNextInt();
        }
    }

    public UpdateClientClassesPacket(PlayerData playerData) {
        this.name = playerData.playerName;
        this.classes = playerData.getClassLevels();

        PacketWriter writer = new PacketWriter(this);
        writer.putNextString(name);
        for (int i = 0; i < PlayerClass.classes.size(); i++) {
            writer.putNextInt(playerData.getClassLevel(i));
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(name, false);
        playerData.setClassLevels(classes);
    }
}