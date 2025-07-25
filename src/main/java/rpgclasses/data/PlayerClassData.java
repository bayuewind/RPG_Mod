package rpgclasses.data;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.content.player.SkillsAndAttributes.Skill;
import rpgclasses.content.player.SkillsAndAttributes.SkillsList;

import java.util.Arrays;

public class PlayerClassData {
    public final String prefixDataName;
    public final String passivesDataName;
    public final String activeSkillsDataName;

    public final PlayerClass playerClass;
    public final String playerName;

    private int[] passiveLevels;
    private int[] activeSkillLevels;

    public PlayerClassData(int classID, String playerName) {
        this.playerClass = PlayerClass.classesList.get(classID);
        this.playerName = playerName;

        this.passiveLevels = new int[playerClass.passivesList.size()];
        this.activeSkillLevels = new int[playerClass.activeSkillsList.size()];

        prefixDataName = "rpgmod_" + playerClass.stringID + "_";
        passivesDataName = prefixDataName + "passives";
        activeSkillsDataName = prefixDataName + "actives";
    }

    public PlayerData getPlayerData(boolean isServer) {
        return PlayerDataList.getPlayerData(playerName, isServer);
    }

    public int getLevel(boolean isServer) {
        return playerClass.getLevel(getPlayerData(isServer));
    }

    public int totalPassivePoints(boolean isServer) {
        return getLevel(isServer) * 2;
    }

    public int totalActiveSkillPoints(boolean isServer) {
        return getLevel(isServer);
    }

    public int usedPassivePoints() {
        return Arrays.stream(passiveLevels).sum();
    }

    public int usedActiveSkillPoints() {
        return Arrays.stream(activeSkillLevels).sum();
    }

    public void loadData(LoadData loadData) {
        loadData(
                loadData.getIntArray(passivesDataName, new int[playerClass.passivesList.size()]),
                loadData.getIntArray(activeSkillsDataName, new int[playerClass.activeSkillsList.size()])
        );
    }

    public void loadData(int[] passiveLevels, int[] activeSkillLevels) {
        loadDataPassives(passiveLevels);
        loadDataActiveSkills(activeSkillLevels);
    }

    public void loadDataPassives(int[] passiveLevels) {
        this.passiveLevels = passiveLevels.length != playerClass.passivesList.size() ? new int[playerClass.passivesList.size()] : passiveLevels;
    }

    public void loadDataActiveSkills(int[] activeSkillLevels) {
        this.activeSkillLevels = activeSkillLevels.length != playerClass.activeSkillsList.size() ? new int[playerClass.activeSkillsList.size()] : activeSkillLevels;
    }

    public int[] getPassiveLevels() {
        return passiveLevels;
    }

    public void setPassiveLevels(int[] passiveLevels) {
        this.passiveLevels = passiveLevels;
    }

    public void setPassiveLevel(int skillID, int level) {
        passiveLevels[skillID] = level;
    }

    public int[] getActiveSkillLevels() {
        return activeSkillLevels;
    }

    public void setActiveSkillLevels(int[] activeSkillLevels) {
        this.activeSkillLevels = activeSkillLevels;
    }

    public void setActiveSkillLevel(int skillID, int level) {
        activeSkillLevels[skillID] = level;
    }

    public void saveData(SaveData saveData) {
        saveData.addIntArray(passivesDataName, passiveLevels);
        saveData.addIntArray(activeSkillsDataName, activeSkillLevels);
    }

    public static PlayerClassData applySpawnPacket(PacketReader reader) {
        int id = reader.getNextInt();
        String playerName = reader.getNextString();

        PlayerClassData classData = new PlayerClassData(id, playerName);

        classData.passiveLevels = reader.getNextInts(classData.playerClass.passivesList.size());
        classData.activeSkillLevels = reader.getNextInts(classData.playerClass.activeSkillsList.size());

        return classData;
    }

    public void setupSpawnPacket(PacketWriter writer) {
        writer.putNextInt(playerClass.id);
        writer.putNextString(playerName);

        writer.putNextInts(passiveLevels);
        writer.putNextInts(activeSkillLevels);
    }

    public int getEffectiveSkillMaxLevel(Skill skill, int classLevel) {
        if (skill.requiredClassLevel == 0) return skill.levelMax;

        int levelMax = skill.levelMax;
        int requiredLevel = Math.max(1, skill.requiredClassLevel);

        int usedPointsAtLevel = 0;
        SkillsList<? extends Skill> skillsList;
        int[] skillLevels;
        if (skill instanceof Passive) {
            skillsList = playerClass.getPassivesList();
            skillLevels = getPassiveLevels();
        } else {
            skillsList = playerClass.getActiveSkillsList();
            skillLevels = getActiveSkillLevels();
        }

        for (int i = 0; i < skillsList.size(); i++) {
            Skill otherSkill = skillsList.get(i);
            if (skill.id != otherSkill.id && otherSkill.requiredClassLevel >= skill.requiredClassLevel)
                usedPointsAtLevel += skillLevels[i];
        }
        if (skill instanceof Passive) usedPointsAtLevel /= 2;

        int allowedByLevel = Math.max(0, classLevel - requiredLevel - usedPointsAtLevel + 1);
        return Math.min(levelMax, allowedByLevel);
    }

}
