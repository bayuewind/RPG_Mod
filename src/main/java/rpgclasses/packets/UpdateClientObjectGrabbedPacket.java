package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.registries.ObjectRegistry;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.ui.CustomUIManager;

import java.util.Objects;

public class UpdateClientObjectGrabbedPacket extends Packet {

    public final int uniqueID;
    public final int grabbedObjectID;

    public UpdateClientObjectGrabbedPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        uniqueID = reader.getNextInt();
        grabbedObjectID = reader.getNextInt();
    }

    public UpdateClientObjectGrabbedPacket(PlayerData playerData) {
        this.uniqueID = playerData.playerUniqueID;
        this.grabbedObjectID = playerData.grabbedObject == null ? -1 : playerData.grabbedObject.getID();

        PacketWriter writer = new PacketWriter(this);

        writer.putNextInt(uniqueID);
        writer.putNextInt(grabbedObjectID);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(uniqueID, false);
        playerData.grabbedObject = grabbedObjectID == -1 ? null : ObjectRegistry.getObject(grabbedObjectID);
        if (Objects.equals(client.getPlayer().getUniqueID(), uniqueID)) {
            CustomUIManager.expBar.updateExpBar(playerData);
        }
    }
}