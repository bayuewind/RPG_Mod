package rpgclasses.containers.rpgmenu.customactions;

import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.inventory.container.customAction.ContainerCustomAction;
import rpgclasses.data.EquippedActiveSkill;

public abstract class EquippedActiveSkillsCustomAction extends ContainerCustomAction {
    public EquippedActiveSkillsCustomAction() {
    }

    public void runAndSend(EquippedActiveSkill[] equippedActiveSkills) {
        Packet content = new Packet();
        PacketWriter writer = new PacketWriter(content);
        for (EquippedActiveSkill equippedActiveSkill : equippedActiveSkills) {
            equippedActiveSkill.setupSpawnPacket(writer);
        }

        this.runAndSendAction(content);
    }

    public void executePacket(PacketReader reader) {
        EquippedActiveSkill[] equippedActiveSkills = new EquippedActiveSkill[4];
        for (int i = 0; i < equippedActiveSkills.length; i++) {
            equippedActiveSkills[i] = EquippedActiveSkill.applySpawnPacket(reader);
        }
        this.run(equippedActiveSkills);
    }

    protected abstract void run(EquippedActiveSkill[] equippedActiveSkills);
}
