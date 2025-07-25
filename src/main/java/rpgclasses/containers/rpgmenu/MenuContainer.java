package rpgclasses.containers.rpgmenu;

import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.container.Container;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.Attribute;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.packets.UpdateClientAttributesPacket;
import rpgclasses.packets.UpdateClientClassDataPacket;
import rpgclasses.packets.UpdateClientClassesPacket;
import rpgclasses.packets.UpdateClientEquippedActiveSkillsPacket;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.containers.rpgmenu.customactions.ClassUpdateCustomAction;
import rpgclasses.containers.rpgmenu.customactions.EquippedActiveSkillsCustomAction;
import rpgclasses.containers.rpgmenu.customactions.IntArrayCustomAction;

import java.util.Arrays;

public class MenuContainer extends Container {
    public final IntArrayCustomAction updateAttributes;
    public final IntArrayCustomAction updateClasses;
    public final ClassUpdateCustomAction updateClass;
    public final EquippedActiveSkillsCustomAction updateEquippedActiveSkills;

    public MenuContainer(final NetworkClient client, int uniqueSeed) {
        super(client, uniqueSeed);

        this.updateAttributes = this.registerAction(
                new IntArrayCustomAction() {
                    @Override
                    protected void run(int[] attributeLevels) {
                        if (client.isServer()) {
                            if (client.playerMob.isInCombat()) {
                                client.getServerClient().sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                return;
                            }

                            ServerClient serverClient = client.getServerClient();

                            if (Arrays.stream(attributeLevels).allMatch(attr -> attr < 1000 && attr >= 0)) {
                                int total = Arrays.stream(attributeLevels).sum();
                                PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                                if (total <= playerData.totalAttributePoints()) {
                                    int reduced = 0;
                                    for (int i = 0; i < attributeLevels.length; i++) {
                                        int difference = playerData.getAttributeLevel(i) - attributeLevels[i];
                                        if (difference > 0) reduced += difference;
                                    }
                                    if (reduced > 0) {
                                        if (playerData.getResets() >= reduced) {
                                            playerData.modResetsSendPacket(serverClient, -reduced);
                                        } else {
                                            System.out.println("updateAttributes: Not enough Reset Points");
                                            return;
                                        }
                                    }
                                    playerData.setAttributeLevels(attributeLevels);
                                    serverClient.getServer().network.sendToAllClients(new UpdateClientAttributesPacket(PlayerDataList.getPlayerData(serverClient.playerMob)));
                                    playerData.updateModifiersBuff(serverClient.playerMob);
                                } else {
                                    System.out.println("updateAttributes: Not enough Attribute Points");
                                }
                            } else {
                                System.out.println("updateAttributes: At least one wrong assigned attribute level");
                            }
                        }
                    }

                    @Override
                    public int arrayLength() {
                        return Attribute.attributesList.size();
                    }
                }
        );

        this.updateClasses = this.registerAction(
                new IntArrayCustomAction() {
                    @Override
                    protected void run(int[] classLevels) {
                        if (client.isServer()) {
                            if (client.playerMob.isInCombat()) {
                                client.getServerClient().sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                return;
                            }

                            ServerClient serverClient = client.getServerClient();

                            if (Arrays.stream(classLevels).allMatch(pClass -> pClass < 1000 && pClass >= 0)) {
                                int total = Arrays.stream(classLevels).sum();
                                PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                                if (total <= playerData.totalClassPoints()) {
                                    int reduced = 0;
                                    for (int i = 0; i < classLevels.length; i++) {
                                        int difference = playerData.getClassLevel(i) - classLevels[i];
                                        if (difference > 0) reduced += difference;
                                    }

                                    if (reduced > 0) {
                                        if (playerData.getResets() >= reduced * 2) {
                                            playerData.modResetsSendPacket(serverClient, -reduced * 2);
                                        } else {
                                            System.out.println("updateClasses: Not enough Reset Points");
                                            return;
                                        }
                                    }

                                    playerData.setClassLevels(classLevels);

                                    for (int classID = 0; classID < classLevels.length; classID++) {
                                        boolean someChange = false;

                                        int classLevel = classLevels[classID];
                                        PlayerClassData classData = playerData.getClassesData()[classID];
                                        for (int skillID = 0; skillID < classData.getActiveSkillLevels().length; skillID++) {
                                            int activeSkillLevel = classData.getActiveSkillLevels()[skillID];
                                            if (activeSkillLevel > 0) {
                                                ActiveSkill activeSkill = classData.playerClass.activeSkillsList.get(skillID);
                                                int maxEffectiveLevel = classData.getEffectiveSkillMaxLevel(activeSkill, classLevel);
                                                if (maxEffectiveLevel < activeSkillLevel) {
                                                    classData.setActiveSkillLevel(skillID, maxEffectiveLevel);
                                                    checkActiveSkillRequirements(classData, activeSkill, maxEffectiveLevel);
                                                    someChange = true;
                                                }
                                            }
                                        }
                                        for (int skillID = 0; skillID < classData.getPassiveLevels().length; skillID++) {
                                            int passiveLevel = classData.getPassiveLevels()[skillID];
                                            if (passiveLevel > 0) {
                                                Passive passive = classData.playerClass.passivesList.get(skillID);
                                                int maxEffectiveLevel = classData.getEffectiveSkillMaxLevel(passive, classLevel);
                                                if (maxEffectiveLevel < passiveLevel) {
                                                    classData.setPassiveLevel(skillID, maxEffectiveLevel);
                                                    someChange = true;
                                                }
                                            }
                                        }

                                        if (someChange)
                                            serverClient.getServer().network.sendToAllClients(new UpdateClientClassDataPacket(classData));
                                    }
                                    serverClient.getServer().network.sendToAllClients(new UpdateClientClassesPacket(PlayerDataList.getPlayerData(serverClient.playerMob)));

                                    playerData.updateClassesBuffs(serverClient.playerMob);

                                    boolean someEquippedUpdate = false;
                                    for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                                        if (!equippedActiveSkill.isEmpty() && (playerData.getClassLevel(equippedActiveSkill.playerClass.id) < 1 || playerData.getClassesData()[equippedActiveSkill.playerClass.id].getActiveSkillLevels()[equippedActiveSkill.activeSkill.id] < 1)) {
                                            equippedActiveSkill.empty();
                                            someEquippedUpdate = true;
                                        }
                                    }
                                    if (someEquippedUpdate) {
                                        serverClient.getServer().network.sendToAllClients(new UpdateClientEquippedActiveSkillsPacket(playerData));
                                    }

                                } else {
                                    System.out.println("updateClasses: Not enough Class Points");
                                }
                            } else {
                                System.out.println("updateClasses: At least one wrong assigned class level");
                            }
                        }
                    }

                    private void checkActiveSkillRequirements(PlayerClassData classData, ActiveSkill activeSkill, int newActiveSkillLevel) {
                        for (int thisActiveSkillID = 0; thisActiveSkillID < classData.getActiveSkillLevels().length; thisActiveSkillID++) {
                            int thisActiveSkillLevel = classData.getActiveSkillLevels()[thisActiveSkillID];
                            if (thisActiveSkillLevel > 0) {
                                ActiveSkill thisActiveSkill = classData.playerClass.activeSkillsList.get(thisActiveSkillID);
                                if (!thisActiveSkill.requiredSkills.isEmpty()) {
                                    for (ActiveSkill.RequiredSkill requiredSkill : thisActiveSkill.requiredSkills) {
                                        if (requiredSkill.activeSkill.id == activeSkill.id) {
                                            if (requiredSkill.activeSkillLevel > newActiveSkillLevel) {
                                                classData.setActiveSkillLevel(thisActiveSkillID, 0);
                                                checkActiveSkillRequirements(classData, thisActiveSkill, 0);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public int arrayLength() {
                        return PlayerClass.classesList.size();
                    }
                }
        );

        this.updateClass = this.registerAction(
                new ClassUpdateCustomAction() {
                    @Override
                    protected void run(int classID, int[] passiveLevels, int[] activeSkillLevels) {
                        if (client.isServer()) {
                            if (client.playerMob.isInCombat() && !client.playerMob.buffManager.hasBuff(RPGBuffs.PASSIVES.OverlevelClass)) {
                                client.playerMob.getServerClient().sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                System.out.println("updateClass: No updates in combat");
                                return;
                            }

                            ServerClient serverClient = client.getServerClient();
                            PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                            PlayerClassData classData = playerData.getClassesData()[classID];

                            boolean valid = true;
                            for (int i = 0; i < passiveLevels.length; i++) {
                                int assignedLevel = passiveLevels[i];
                                int effectiveMax = classData.getEffectiveSkillMaxLevel(classData.playerClass.passivesList.get(i), classData.getLevel(false));

                                if (assignedLevel < 0 || assignedLevel > effectiveMax) {
                                    valid = false;
                                    break;
                                }
                            }

                            if (valid) {
                                for (int i = 0; i < activeSkillLevels.length; i++) {
                                    int assignedLevel = activeSkillLevels[i];
                                    int effectiveMax = classData.getEffectiveSkillMaxLevel(classData.playerClass.activeSkillsList.get(i), classData.getLevel(false));

                                    if (assignedLevel < 0 || assignedLevel > effectiveMax) {
                                        valid = false;
                                        break;
                                    }

                                    if (assignedLevel > 0) {
                                        ActiveSkill skill = classData.playerClass.activeSkillsList.get(i);
                                        boolean hasRequired = hasRequired(skill, activeSkillLevels);
                                        if (!hasRequired) {
                                            valid = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (valid) {
                                int totalPassive = Arrays.stream(passiveLevels).sum();
                                if (totalPassive <= classData.totalPassivePoints(false)) {
                                    int totalActiveSkill = Arrays.stream(activeSkillLevels).sum();
                                    if (totalActiveSkill <= classData.totalActiveSkillPoints(false)) {
                                        classData.setPassiveLevels(passiveLevels);
                                        classData.setActiveSkillLevels(activeSkillLevels);
                                        serverClient.getServer().network.sendToAllClients(new UpdateClientClassDataPacket(classData));
                                        playerData.updateClassesBuffs(serverClient.playerMob);

                                        boolean someEquippedUpdate = false;
                                        for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                                            if (!equippedActiveSkill.isEmpty() && equippedActiveSkill.sameClass(classData) && classData.getActiveSkillLevels()[equippedActiveSkill.activeSkill.id] < 1) {
                                                equippedActiveSkill.empty();
                                                someEquippedUpdate = true;
                                            }
                                        }
                                        if (someEquippedUpdate) {
                                            serverClient.getServer().network.sendToAllClients(new UpdateClientEquippedActiveSkillsPacket(playerData));
                                        }
                                    } else {
                                        System.out.println("updateClass: Not enough Active Skill Points");
                                    }
                                } else {
                                    System.out.println("updateClass: Not enough Passive Points");
                                }
                            } else {
                                System.out.println("updateClass: At least one wrong assigned Passive Level or Active Skill Level");
                            }
                        }
                    }

                    private boolean hasRequired(ActiveSkill skill, int[] activeSkillLevels) {
                        boolean hasRequired = true;
                        if (skill != null) {
                            for (ActiveSkill.RequiredSkill requiredSkill : skill.requiredSkills) {
                                if (activeSkillLevels[requiredSkill.activeSkill.id] < requiredSkill.activeSkillLevel) {
                                    hasRequired = false;
                                    break;
                                }
                            }
                        }
                        return hasRequired;
                    }
                }
        );

        this.updateEquippedActiveSkills = this.registerAction(
                new EquippedActiveSkillsCustomAction() {
                    @Override
                    protected void run(EquippedActiveSkill[] equippedActiveSkills) {
                        if (client.isServer()) {
                            ServerClient serverClient = client.getServerClient();

                            for (EquippedActiveSkill activeSkill : equippedActiveSkills) {
                                if (activeSkill.isEmpty()) {
                                    activeSkill.empty();
                                } else {
                                    activeSkill.playerClass = PlayerClass.classesList.get(activeSkill.playerClass.id);
                                    activeSkill.activeSkill = activeSkill.playerClass.activeSkillsList.get(activeSkill.activeSkill.id);
                                }
                            }

                            // No continue if player is in combat
                            if (client.playerMob.isInCombat()) {
                                serverClient.sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                System.out.println("updateEquippedActiveSkills: No updates in combat");
                                return;
                            }

                            PlayerMob player = client.playerMob;
                            PlayerData playerData = PlayerDataList.getPlayerData(player);

                            // No continue if player does not have some equipped skill
                            boolean allCorrect = true;
                            for (EquippedActiveSkill equippedActiveSkill : equippedActiveSkills) {
                                if (equippedActiveSkill != null && equippedActiveSkill.playerClass != null && equippedActiveSkill.activeSkill != null) {
                                    PlayerClassData playerClassData = playerData.getClassesData()[equippedActiveSkill.playerClass.id];
                                    if (playerClassData.getLevel(false) < 1 || playerClassData.getActiveSkillLevels()[equippedActiveSkill.activeSkill.id] < 1) {
                                        allCorrect = false;
                                        break;
                                    }
                                }
                            }
                            if (!allCorrect) {
                                System.out.println("updateEquippedActiveSkills: Some Active Skill is not unlocked");
                                return;
                            }

                            // No continue if any changed skill is in cooldown. Also sets lastUse to server's lastUse
                            boolean changedInCooldown = false;
                            for (int i = 0; i < 4; i++) {
                                EquippedActiveSkill equippedActiveSkill = equippedActiveSkills[i];
                                EquippedActiveSkill serverEquippedActiveSkill = playerData.equippedActiveSkills[i];
                                if (equippedActiveSkill.isEmpty()) {
                                    equippedActiveSkill.lastUse = 0;
                                } else if (serverEquippedActiveSkill.isEmpty()) {
                                    equippedActiveSkills[i].lastUse = 0;
                                } else if (equippedActiveSkill.isSameSkill(serverEquippedActiveSkill)) {
                                    equippedActiveSkill.lastUse = serverEquippedActiveSkill.lastUse;
                                } else {
                                    int activeSkillLevel = playerData.getClassesData()[serverEquippedActiveSkill.playerClass.id].getActiveSkillLevels()[serverEquippedActiveSkill.activeSkill.id];
                                    if (serverEquippedActiveSkill.isInCooldown(activeSkillLevel, player.getTime())) {
                                        changedInCooldown = true;
                                    }
                                }
                            }
                            if (changedInCooldown) {
                                System.out.println("updateEquippedActiveSkills: Can't change Active Skills in cooldown");
                                return;
                            }

                            // No continue if player have more than one different skill of the same family
                            boolean sameFamily = false;
                            for (int i = 0; i < equippedActiveSkills.length; i++) {
                                EquippedActiveSkill equippedActiveSkill = equippedActiveSkills[i];
                                if (equippedActiveSkill != null && !equippedActiveSkill.isEmpty()) {
                                    for (int j = 0; j < equippedActiveSkills.length; j++) {
                                        if (i != j && equippedActiveSkills[j] != null && !equippedActiveSkills[j].isEmpty() && equippedActiveSkills[j].isNotSameSkillButSameFamily(equippedActiveSkill)) {
                                            sameFamily = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (sameFamily) {
                                System.out.println("updateEquippedActiveSkills: Can't use different Active Skills in same family");
                                return;
                            }

                            // If any skill is in cooldown in any other slot with a later lastUse, then apply that lastUse to its slot. If not, reset it to 0
                            for (int i = 0; i < equippedActiveSkills.length; i++) {
                                EquippedActiveSkill equippedActiveSkill = equippedActiveSkills[i];
                                if (equippedActiveSkill != null) {
                                    long maxLastUse = 0;
                                    for (int j = 0; j < equippedActiveSkills.length; j++) {
                                        if (i != j) {
                                            EquippedActiveSkill equippedActiveSkill2 = equippedActiveSkills[j];
                                            if (equippedActiveSkill2 != null && !equippedActiveSkill2.isEmpty() && equippedActiveSkill.isSameSkill(equippedActiveSkill2)) {
                                                maxLastUse = Math.max(maxLastUse, equippedActiveSkill2.lastUse);
                                            }
                                        }
                                    }
                                    equippedActiveSkill.lastUse = maxLastUse;
                                }
                            }

                            playerData.equippedActiveSkills = equippedActiveSkills;
                            serverClient.getServer().network.sendToAllClients(new UpdateClientEquippedActiveSkillsPacket(playerData));
                        }
                    }
                }
        );

    }
}
