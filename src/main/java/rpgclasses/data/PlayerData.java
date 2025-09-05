package rpgclasses.data;

import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.objectEntity.interfaces.OEInventory;
import necesse.inventory.item.toolItem.ToolType;
import necesse.level.gameObject.*;
import necesse.level.gameObject.furniture.RoomFurniture;
import necesse.level.maps.Level;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.Attribute;
import rpgclasses.content.player.SkillsAndAttributes.Passives.BasicPassive;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.packets.ShowModExpPacket;
import rpgclasses.packets.UpdateClientExpPacket;
import rpgclasses.packets.UpdateClientResetsPacket;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGModifiers;
import rpgclasses.settings.RPGSettings;

import java.util.Arrays;

public class PlayerData {
    public static int EQUIPPED_SKILLS_MAX = 6;

    public static String prefixDataName = "rpgmod_";
    public static String expDataName = prefixDataName + "exp";
    public static String resetsDataName = prefixDataName + "resets";
    public static String attributesDataName = prefixDataName + "attributes";
    public static String classesDataName = prefixDataName + "classes";
    public static String equippedActiveSkillsDataName = prefixDataName + "equippedactives";

    public static String grabbedObjectDataName = prefixDataName + "grabbedobject";

    public final String playerName;
    private int exp = 0;
    private int resets = 0;
    private int[] attributeLevels = new int[Attribute.attributesList.size()];
    private int[] classLevels = new int[PlayerClass.classesList.size()];
    private PlayerClassData[] classesData = new PlayerClassData[PlayerClass.classesList.size()];
    public EquippedActiveSkill[] equippedActiveSkills = new EquippedActiveSkill[EQUIPPED_SKILLS_MAX];

    public GameObject grabbedObject;

    public PlayerData(String playerName) {
        this.playerName = playerName;
        for (int i = 0; i < classesData.length; i++) {
            classesData[i] = new PlayerClassData(i, playerName);
        }
        for (int i = 0; i < EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i] = new EquippedActiveSkill();
        }
    }

    public void loadData(PlayerMob player, LoadData loadData) {
        loadData(
                loadData.getInt(expDataName, 0),
                loadData.getInt(resetsDataName, 0)
        );
        loadDataAttributes(loadData);
        loadDataClasses(loadData);
        loadDataClassesData(classLevels, loadData);
        loadDataEquippedActiveSkills(player, loadData);
        loadDataMisc(loadData);
    }

    public void loadData(int exp, int resets) {
        loadDataExp(exp);
        loadDataResets(resets);
    }

    public void loadDataExp(int exp) {
        this.exp = exp;
    }

    public void loadDataResets(int resets) {
        this.resets = resets;
    }

    public void loadDataAttributes(LoadData loadData) {
        for (Attribute attribute : Attribute.attributesList) {
            this.attributeLevels[attribute.id] = loadData.getInt(attributesDataName + "_" + attribute.stringID, 0);
        }
    }

    public void loadDataClasses(LoadData loadData) {
        for (PlayerClass playerClass : PlayerClass.classesList) {
            classLevels[playerClass.id] = loadData.getInt(classesDataName + "_" + playerClass.stringID, 0);
        }
    }

    public void loadDataAttributes(int[] attributeLevels) {
        this.attributeLevels = attributeLevels.length != Attribute.attributesList.size() ? new int[Attribute.attributesList.size()] : attributeLevels;
    }

    public void loadDataClasses(int[] classLevels) {
        this.classLevels = classLevels.length != PlayerClass.classesList.size() ? new int[PlayerClass.classesList.size()] : classLevels;
    }

    public void loadDataClassesData(int[] classLevels, LoadData loadData) {
        boolean update = classLevels.length == PlayerClass.classesList.size();
        this.classesData = new PlayerClassData[classLevels.length];
        for (int i = 0; i < this.classesData.length; i++) {
            PlayerClassData classData = new PlayerClassData(i, playerName);
            if (update) classData.loadData(loadData);
            classesData[i] = classData;
        }
    }

    public void loadDataEquippedActiveSkills(PlayerMob player, LoadData loadData) {
        equippedActiveSkills = new EquippedActiveSkill[EQUIPPED_SKILLS_MAX];
        for (int i = 0; i < PlayerData.EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i] = EquippedActiveSkill.loadData(player, loadData, i);
        }
    }

    public void loadDataMisc(LoadData loadData) {
        try {
            int grabbedObjectID = loadData.getInt(grabbedObjectDataName, -1);
            if (grabbedObjectID == -1) {
                grabbedObject = null;
            } else {
                grabbedObject = ObjectRegistry.getObject(grabbedObjectID);
            }
        } catch (NullPointerException ignored) {
            grabbedObject = null;
        }
    }

    public void saveData(SaveData saveData) {
        saveData.addInt(expDataName, exp);
        saveData.addInt(resetsDataName, resets);

        for (Attribute attribute : Attribute.attributesList) {
            saveData.addInt(attributesDataName + "_" + attribute.stringID, attributeLevels[attribute.id]);
        }

        for (PlayerClass playerClass : PlayerClass.classesList) {
            saveData.addInt(classesDataName + "_" + playerClass.stringID, classLevels[playerClass.id]);
        }

        for (PlayerClassData classesDatum : this.classesData) {
            classesDatum.saveData(saveData);
        }

        for (int i = 0; i < EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i].saveData(saveData, i);
        }

        saveData.addInt(grabbedObjectDataName, grabbedObject == null ? -1 : grabbedObject.getID());
    }

    public int getBaseExp() {
        return this.exp;
    }

    public int getExp() {
        return this.exp + RPGSettings.startingExperience();
    }

    public int[] getAttributeLevels() {
        return attributeLevels;
    }

    public int getEndurance(PlayerMob player) {
        return getAttributeLevel(0) + player.buffManager.getModifier(RPGModifiers.ENDURANCE_ATTR_FLAT);
    }

    public int getSpeed(PlayerMob player) {
        return getAttributeLevel(1) + player.buffManager.getModifier(RPGModifiers.SPEED_ATTR_FLAT);
    }

    public int getStrength(PlayerMob player) {
        return getAttributeLevel(2) + player.buffManager.getModifier(RPGModifiers.STRENGTH_ATTR_FLAT);
    }

    public int getIntelligence(PlayerMob player) {
        return getAttributeLevel(3) + player.buffManager.getModifier(RPGModifiers.INTELLIGENCE_ATTR_FLAT);
    }

    public int getGrace(PlayerMob player) {
        return getAttributeLevel(4) + player.buffManager.getModifier(RPGModifiers.GRACE_ATTR_FLAT);
    }

    public void setAttributeLevels(int[] attributeLevels) {
        this.attributeLevels = attributeLevels;
    }

    public int getAttributeLevel(int id) {
        return (id < 0 || id >= attributeLevels.length) ? 0 : attributeLevels[id];
    }

    public int[] getClassLevels() {
        int[] trueClassLevels = new int[classLevels.length];
        for (int i = 0; i < classLevels.length; i++) {
            trueClassLevels[i] = getClassLevel(i);
        }
        return trueClassLevels;
    }

    public void setClassLevels(int[] classLevels) {
        this.classLevels = classLevels;
    }

    public int getClassLevel(int id) {
        return (id < 0 || id >= classLevels.length) ? 0 : (PlayerClass.classesList.get(id).isEnabled() ? classLevels[id] : 0);
    }

    public PlayerClassData[] getClassesData() {
        return classesData;
    }

    public int getResets() {
        return resets;
    }

    public void modResetsSendPacket(ServerClient serverClient, int resets) {
        this.resets += resets;
        serverClient.getServer().network.sendToAllClients(new UpdateClientResetsPacket(PlayerDataList.getPlayerData(serverClient.playerMob)));
    }

    public int getExpActual() {
        return this.getExp() - getExpRequiredForLevel(this.getLevel());
    }

    public int getExpNext() {
        return getExpRequiredForLevel(this.getLevel() + 1) - getExpRequiredForLevel(getLevel());
    }

    public int getExpRequiredForLevel(int level) {
        return level * RPGSettings.firstExperienceReq() + RPGSettings.experienceReqInc() * (level * (level - 1)) / 2 + RPGSettings.squareExperienceReqInc() * (level * (level - 1) * (2 * level - 1)) / 6 + RPGSettings.cubeExperienceReqInc() * (int) Math.pow((double) (level * (level - 1)) / 2, 2);
    }

    public int getLevel() {
        int level = 0;
        while (getExpRequiredForLevel(level + 1) <= this.getExp()) {
            level++;
        }
        return level;
    }

    public void updateAllBuffs(PlayerMob player) {
        updateModifiersBuff(player);

        if (!player.buffManager.hasBuff(RPGBuffs.PASSIVES.HOLY_DAMAGE))
            player.buffManager.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.HOLY_DAMAGE, player, 1000, null), true);

        boolean someOverlevel = Arrays.stream(getClassLevels()).sum() > totalClassPoints() || Arrays.stream(getAttributeLevels()).sum() > totalAttributePoints();

        if (!someOverlevel)
            for (PlayerClassData classesDatum : classesData) {
                boolean isServer = player.isServer();
                boolean validClass = classesDatum.getLevel(isServer) > 0;
                if (validClass) {
                    if (!someOverlevel) {
                        if (classesDatum.usedPassivePoints() > classesDatum.totalPassivePoints(isServer) || classesDatum.usedActiveSkillPoints() > classesDatum.totalActiveSkillPoints(isServer)) {
                            someOverlevel = true;
                        }
                    }
                }

                for (int i = 0; i < classesDatum.getPassiveLevels().length; i++) {
                    Passive passive = classesDatum.playerClass.passivesList.get(i);
                    if (!(passive instanceof BasicPassive)) {
                        if (validClass) {
                            int level = classesDatum.getPassiveLevels()[i];
                            if (level > 0) {
                                passive.givePassiveBuff(player, this, level);
                            } else {
                                passive.removePassiveBuffs(player);
                            }
                        } else {
                            passive.removePassiveBuffs(player);
                        }
                    }
                }
            }

        boolean hasOverlevelBuff = player.buffManager.hasBuff(RPGBuffs.PASSIVES.OVER_LEVEL);
        if (someOverlevel && !hasOverlevelBuff) {
            player.buffManager.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.OVER_LEVEL, player, 1000, null), true);
        } else if (!someOverlevel && hasOverlevelBuff) {
            player.buffManager.removeBuff(RPGBuffs.PASSIVES.OVER_LEVEL, true);
        }
    }

    public void updateModifiersBuff(PlayerMob player) {
        player.buffManager.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.MODIFIERS, player, 1000, null), true, true);
    }

    public static int MAX_EXP = 2000000000;

    public void modExpSendPacket(ServerClient serverClient, int amount) {
        if (amount == 0) return;

        int oldLevel = this.getLevel();

        int maxExp = MAX_EXP - RPGSettings.startingExperience();
        if (amount > 0) {
            if (this.exp > maxExp - amount) {
                amount = maxExp - this.exp;
                if (amount <= 0) return;
                this.exp = maxExp;
            } else {
                this.exp += amount;
            }
        } else {
            if (this.exp + amount < 0) {
                amount = -this.exp;
                if (amount == 0) return;
                this.exp = 0;
            } else {
                this.exp += amount;
            }
        }

        int newLevel = this.getLevel();
        boolean levelUp = newLevel > oldLevel;

        if (levelUp) {
            int levelsGained = newLevel - oldLevel;
            this.modResetsSendPacket(serverClient, levelsGained * 5);

            for (int level = oldLevel + 1; level <= newLevel; level++) {
                serverClient.sendChatMessage(new LocalMessage("message", "newlevel", "level", level));
            }
        }

        serverClient.getServer().network.sendToClientsAtEntireLevel(new ShowModExpPacket(serverClient.playerMob.getX(), serverClient.playerMob.getY(), amount, levelUp), serverClient.getLevel());
        serverClient.getServer().network.sendToAllClients(new UpdateClientExpPacket(PlayerDataList.getPlayerData(serverClient.playerMob)));
    }

    public int totalAttributePoints() {
        return getLevel() * 2;
    }

    public int totalClassPoints() {
        return getLevel();
    }

    public void setupSpawnPacket(PacketWriter writer) {
        writer.putNextString(playerName);

        // Exp
        writer.putNextInt(exp);

        // Resets
        writer.putNextInt(resets);

        // Attribute Levels
        writer.putNextInts(attributeLevels);

        // Class Levels
        writer.putNextInts(classLevels);

        // Classes Data
        for (PlayerClassData classesDatum : classesData) {
            classesDatum.setupSpawnPacket(writer);
        }

        // Equipped Active Skills
        for (EquippedActiveSkill equippedActiveSkill : equippedActiveSkills) {
            equippedActiveSkill.setupSpawnPacket(writer);
        }
    }

    public static PlayerData applySpawnPacket(PacketReader reader) {
        PlayerData playerData = new PlayerData(reader.getNextString());

        // Exp
        playerData.exp = reader.getNextInt();

        // Resets
        playerData.resets = reader.getNextInt();

        // Attribute Levels
        playerData.attributeLevels = reader.getNextInts(Attribute.attributes.size());

        // Class Levels
        playerData.classLevels = reader.getNextInts(PlayerClass.classesList.size());

        // Classes Data
        for (int i = 0; i < PlayerClass.classesList.size(); i++) {
            playerData.classesData[i] = PlayerClassData.applySpawnPacket(reader);
        }

        // Equipped Active Skills
        for (int i = 0; i < EQUIPPED_SKILLS_MAX; i++) {
            playerData.equippedActiveSkills[i] = EquippedActiveSkill.applySpawnPacket(reader);
        }

        return playerData;
    }

    public static GameObject isGrabbableObject(Level level, int tileX, int tileY) {
        GameObject object = level.getObject(tileX, tileY);

        if (object == null) return null;
        if (object.getID() <= 0) return null;
        if (object.toolType == ToolType.UNBREAKABLE) return null;
        if (object.isMultiTile()) return null;
        if (level.entityManager.getObjectEntity(tileX, tileY) instanceof OEInventory) return null;

        return (object instanceof WallObject || object instanceof RockObject || object instanceof ColumnObject || object instanceof RoomFurniture || object instanceof CraftingStationObject || object instanceof StreetlampObject || object instanceof CandlePedestalObject || object instanceof TrainingDummyObject || object instanceof SnowManTrainingDummyObject || object instanceof TreeObject)
                ? object : null;
    }

    public int getInUseActiveSkillSlotIndex() {
        for (int i = 0; i < equippedActiveSkills.length; i++) {
            if (equippedActiveSkills[i].isInUse()) return i;
        }
        return -1;
    }

    public EquippedActiveSkill getInUseActiveSkillSlot() {
        int index = getInUseActiveSkillSlotIndex();
        return index != -1 ? equippedActiveSkills[index] : null;
    }

    public ActiveSkill getInUseActiveSkill() {
        EquippedActiveSkill equippedActiveSkill = getInUseActiveSkillSlot();
        return equippedActiveSkill == null ? null : equippedActiveSkill.getActiveSkill();
    }

    public void removeInUseActiveSkillSlot() {
        EquippedActiveSkill equippedActiveSkill = getInUseActiveSkillSlot();
        if (equippedActiveSkill != null) {
            equippedActiveSkill.restartCooldown();
        }
    }


}
