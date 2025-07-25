package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.ui.RPGSkillUIManager;

import java.util.Objects;

public class UpdateClientEquippedActiveSkillsPacket extends Packet {

    public final String name;
    public final EquippedActiveSkill[] equippedActiveSkills;

    public UpdateClientEquippedActiveSkillsPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        name = reader.getNextString();
        equippedActiveSkills = new EquippedActiveSkill[4];

        for (int i = 0; i < 4; i++) {
            equippedActiveSkills[i] = EquippedActiveSkill.applySpawnPacket(reader);
        }
    }

    public UpdateClientEquippedActiveSkillsPacket(PlayerData playerData) {
        this.name = playerData.playerName;
        this.equippedActiveSkills = playerData.equippedActiveSkills;

        PacketWriter writer = new PacketWriter(this);
        writer.putNextString(name);
        for (EquippedActiveSkill activeSkill : equippedActiveSkills) {
            activeSkill.setupSpawnPacket(writer);
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(name, false);
        playerData.equippedActiveSkills = equippedActiveSkills;
        if (Objects.equals(client.getPlayer().playerName, name)) {
            RPGSkillUIManager.updateContent(playerData);
        }
    }
}