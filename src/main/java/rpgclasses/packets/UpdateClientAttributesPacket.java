package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.content.player.Attribute;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class UpdateClientAttributesPacket extends Packet {

    public final String name;
    public final int[] attributes;

    public UpdateClientAttributesPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        name = reader.getNextString();
        attributes = new int[Attribute.attributes.size()];
        for (int i = 0; i < Attribute.attributes.size(); i++) {
            attributes[i] = reader.getNextInt();
        }
    }

    public UpdateClientAttributesPacket(PlayerData playerData) {
        this.name = playerData.playerName;
        this.attributes = playerData.getAttributePointsUsed();

        PacketWriter writer = new PacketWriter(this);
        writer.putNextString(name);
        for (int i = 0; i < Attribute.attributes.size(); i++) {
            writer.putNextInt(playerData.getAttributePoints(i));
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(name, false);
        playerData.setAttributes(attributes);
    }
}