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

    public final int uniqueID;
    public final String playerName;
    public final EquippedActiveSkill[] equippedActiveSkills;

    public UpdateClientEquippedActiveSkillsPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        uniqueID = reader.getNextInt();
        playerName = reader.getNextString();
        equippedActiveSkills = new EquippedActiveSkill[PlayerData.EQUIPPED_SKILLS_MAX];

        for (int i = 0; i < PlayerData.EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i] = EquippedActiveSkill.applyPacket(reader);
        }
    }

    public UpdateClientEquippedActiveSkillsPacket(PlayerData playerData) {
        this.uniqueID = playerData.playerUniqueID;
        this.equippedActiveSkills = playerData.equippedActiveSkills;
        this.playerName = playerData.playerName;

        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(uniqueID);
        writer.putNextString(playerName);
        for (EquippedActiveSkill activeSkill : equippedActiveSkills) {
            activeSkill.setupPacket(writer);
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(uniqueID, playerName, false);
        if(playerData != null) {
            playerData.equippedActiveSkills = equippedActiveSkills;
            if (Objects.equals(client.getPlayer().getUniqueID(), uniqueID)) {
                RPGSkillUIManager.updateContent(playerData);
            }
        }
    }
}