package rpgclasses.mobs.summons.pasivesummon;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.summon.summonFollowingMob.attackingFollowingMob.AttackingFollowingMob;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

abstract public class PassiveSummonedMob extends AttackingFollowingMob {
    public static String prefixDataName = "rpgmod_summon_";
    public static String classDataName = prefixDataName + "class";
    public static String passiveDataName = prefixDataName + "passive";

    public Passive skill;
    public PlayerClass playerClass;

    public PassiveSummonedMob(int health) {
        super(health);
    }

    public void setPassive(Passive skill) {
        this.skill = skill;
        this.playerClass = skill.playerClass;
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        playerClass = PlayerClass.classesList.get(load.getInt(classDataName));
        skill = playerClass.passivesList.get(load.getInt(passiveDataName));
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(classDataName, playerClass.id);
        save.addInt(passiveDataName, skill.id);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        playerClass = PlayerClass.classesList.get(reader.getNextInt());
        skill = playerClass.passivesList.get(reader.getNextInt());
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(playerClass.id);
        writer.putNextInt(skill.id);
    }

    public PlayerData getPlayerData() {
        NetworkClient client = this.getFollowingClient();
        if (client == null) return null;
        return PlayerDataList.getPlayerData(client.playerMob);
    }

    public PlayerClassData getClassData(PlayerData playerData) {
        if (playerData == null) return null;
        return playerData.getClassesData()[playerClass.id];
    }

    public int getPassiveLevel() {
        return this.getPassiveLevel(getPlayerData());
    }

    public int getPassiveLevel(PlayerData playerData) {
        PlayerClassData classData = getClassData(playerData);
        if (classData == null) return 0;
        return classData.getPassiveLevels()[skill.id];
    }
}
