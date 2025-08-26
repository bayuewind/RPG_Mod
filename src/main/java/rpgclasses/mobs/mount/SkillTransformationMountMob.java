package rpgclasses.mobs.mount;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

abstract public class SkillTransformationMountMob extends TransformationMountMob {
    public int skillLevel;

    public SkillTransformationMountMob() {
        super();
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt("skillLevel", this.skillLevel);
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        this.skillLevel = save.getInt("skillLevel");
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(skillLevel);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        skillLevel = reader.getNextInt();
    }
}
