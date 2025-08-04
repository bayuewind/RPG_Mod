package rpgclasses.data;

import necesse.engine.localization.Localization;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormExpressionWheel;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.item.Item;
import necesse.level.maps.hudManager.floatText.FloatTextFade;
import necesse.level.maps.hudManager.floatText.UniqueFloatText;
import org.jetbrains.annotations.NotNull;
import rpgclasses.RPGResources;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.SimplePassiveBuffActiveSkill;
import rpgclasses.packets.ActiveAbilityRunPacket;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.BiConsumer;

public class EquippedActiveSkill {
    public PlayerClass playerClass;
    public ActiveSkill activeSkill;
    public long lastUse;

    public EquippedActiveSkill() {
        this.playerClass = null;
        this.activeSkill = null;
    }

    public EquippedActiveSkill(int classID, int activeSkillID) {
        if (classID != -1 && activeSkillID != -1) {
            this.playerClass = PlayerClass.classesList.get(classID);
            this.activeSkill = playerClass.activeSkillsList.get(activeSkillID);
        }
    }

    public void modifyForm(FormExpressionWheel.Expression expression, Field drawIcon, Field displayName) throws IllegalAccessException {
        displayName.set(expression, new LocalMessage("activeskills", activeSkill == null ? "empty" : activeSkill.stringID));
        drawIcon.set(expression, new ActiveSkillDrawOptionsModifier());
    }

    public void saveData(SaveData saveData, int position) {
        String dataKeyPrefix = PlayerData.prefixDataName + "equippedactives" + position + "_";

        saveData.addInt(dataKeyPrefix + "class", playerClass == null ? -1 : playerClass.id);
        saveData.addInt(dataKeyPrefix + "activeSkill", activeSkill == null ? -1 : activeSkill.id);
        saveData.addLong(dataKeyPrefix + "lastUse", lastUse);
    }

    public static EquippedActiveSkill loadData(PlayerMob player, LoadData loadData, int position) {
        String dataKeyPrefix = PlayerData.equippedActiveSkillsDataName + position + "_";

        int classID = loadData.getInt(dataKeyPrefix + "class", -1);
        int activeSkillID = loadData.getInt(dataKeyPrefix + "activeSkill", -1);
        long lastUse = loadData.getLong(dataKeyPrefix + "lastUse", 0);

        EquippedActiveSkill equippedActiveSkill = new EquippedActiveSkill(classID, activeSkillID);

        if (lastUse == -100 && equippedActiveSkill.activeSkill instanceof SimplePassiveBuffActiveSkill) {
            equippedActiveSkill.lastUse = player.getTime();
        } else {
            equippedActiveSkill.lastUse = lastUse;
        }

        return equippedActiveSkill;
    }

    public void setupSpawnPacket(PacketWriter writer) {
        writer.putNextInt(playerClass == null ? -1 : playerClass.id);
        writer.putNextInt(activeSkill == null ? -1 : activeSkill.id);
        writer.putNextLong(lastUse);
    }

    public static EquippedActiveSkill applySpawnPacket(PacketReader reader) {
        int classID = reader.getNextInt();
        int activeSkillID = reader.getNextInt();
        long lastUse = reader.getNextLong();

        EquippedActiveSkill equippedActiveSkill = new EquippedActiveSkill(classID, activeSkillID);
        equippedActiveSkill.lastUse = lastUse;

        return equippedActiveSkill;
    }

    public class ActiveSkillDrawOptionsModifier implements BiConsumer<Point, PlayerMob> {
        @Override
        public void accept(Point pos, PlayerMob player) {
            int size = 34;
            GameTexture gameTexture;
            if (activeSkill == null) {
                gameTexture = RPGResources.UI_TEXTURES.emptyActiveSkill_texture;
            } else {
                gameTexture = activeSkill.texture;
            }

            gameTexture.initDraw().pos(pos.x - size / 2, pos.y - size / 2 + 5).draw();

            if (playerClass != null && activeSkill != null) {
                PlayerData playerData = PlayerDataList.getPlayerData(player);
                int activeSkillLevel = playerData.getClassesData()[playerClass.id].getActiveSkillLevels()[activeSkill.id];
                int cooldownLeft = getCooldownLeft(activeSkillLevel, player.getTime());
                if (cooldownLeft > 0) {
                    String cooldownLeftString = getCooldownLeftString(cooldownLeft);

                    FontOptions options = new FontOptions(Item.tipFontOptions);
                    options.color(new Color(255, 102, 102));
                    int width = FontManager.bit.getWidthCeil(cooldownLeftString, options);
                    FontManager.bit.drawString(pos.x + (float) size / 2 - width, pos.y + (float) size / 2 - 5 + 12, cooldownLeftString, options);
                }
            }

        }

        @NotNull
        @Override
        public BiConsumer<Point, PlayerMob> andThen(@NotNull BiConsumer<? super Point, ? super PlayerMob> after) {
            return BiConsumer.super.andThen(after);
        }
    }

    public boolean isInUse() {
        return lastUse == -100;
    }

    public String getCooldownLeftString(int cooldownLeft) {
        if (isInUse()) {
            return Localization.translate("ui", "inuse");
        } else {
            return getTimeLeftString(cooldownLeft);
        }
    }

    public static String getTimeLeftString(int cooldownLeft) {
        String amountString;
        if (cooldownLeft < 1000) {
            amountString = String.format("%.1fs", (float) cooldownLeft / 1000);
        } else {
            amountString = (cooldownLeft / 1000) + "s";
        }
        return amountString;

    }

    public int getCooldownLeft(int activeSkillLevel, long currentTime) {
        long lastUse = this.lastUse;
        long cooldown = activeSkill.getCooldown(activeSkillLevel);

        if (isInUse()) {
            return 1;
        }

        long cooldownLeftLong = (lastUse == 0 || currentTime == 0) ? 0 : (cooldown - (currentTime - lastUse));
        int cooldownLeft;
        if (cooldownLeftLong > Integer.MAX_VALUE) {
            // Extremely high cooldown - prefer to allow the player to use the active skill instead of punishing them
            cooldownLeft = 0;
        } else if (cooldownLeftLong < Integer.MIN_VALUE) {
            cooldownLeft = Integer.MIN_VALUE;
        } else {
            cooldownLeft = (int) cooldownLeftLong;
        }
        return cooldownLeft;
    }

    public boolean isInCooldown(int activeSkillLevel, long currentTime) {
        long lastUse = this.lastUse;
        long cooldown = activeSkill.getCooldown(activeSkillLevel);

        return (cooldown - (currentTime - lastUse)) > 0;
    }

    public boolean isSameSkill(EquippedActiveSkill equippedActiveSkill) {
        return !equippedActiveSkill.isEmpty() && this.isSameSkill(equippedActiveSkill.playerClass, equippedActiveSkill.activeSkill);
    }

    public boolean isSameSkill(ActiveSkill activeSkill) {
        return this.isSameSkill(activeSkill.playerClass, activeSkill);
    }

    public boolean isSameSkill(PlayerClass playerClass, ActiveSkill activeSkill) {
        return !this.isEmpty() && this.playerClass.id == playerClass.id && this.activeSkill.id == activeSkill.id;
    }

    public boolean isSameFamily(EquippedActiveSkill equippedActiveSkill) {
        return !equippedActiveSkill.isEmpty() && this.isSameFamily(equippedActiveSkill.playerClass, equippedActiveSkill.activeSkill);
    }

    public boolean isSameFamily(PlayerClass playerClass, ActiveSkill activeSkill) {
        return !this.isEmpty() && activeSkill.family != null && this.playerClass.id == playerClass.id && Objects.equals(this.activeSkill.family, activeSkill.family);
    }

    public boolean isNotSameSkillButSameFamily(EquippedActiveSkill equippedActiveSkill) {
        return isSameFamily(equippedActiveSkill) && !isSameSkill(equippedActiveSkill);
    }

    public boolean isNotSameSkillButSameFamily(PlayerClass playerClass, ActiveSkill activeSkill) {
        return isSameFamily(playerClass, activeSkill) && !isSameSkill(playerClass, activeSkill);
    }

    public boolean sameClass(PlayerClassData classData) {
        return sameClass(classData.playerClass);
    }

    public boolean sameClass(PlayerClass playerClass) {
        return playerClass.id == this.playerClass.id;
    }

    public boolean isEmpty() {
        return playerClass == null || activeSkill == null;
    }

    public void empty() {
        playerClass = null;
        activeSkill = null;
        lastUse = 0;
    }

    public void tryRun(PlayerMob player, int skillSlot) {
        if (player.isClient()) {
            String canActiveError = this.canActive(player);

            if (canActiveError != null) {
                FloatTextFade text = new UniqueFloatText(player.getX(), player.getY() - 20, Localization.translate("message", canActiveError), (new FontOptions(16)).outline().color(new Color(200, 100, 100)), "activeerror") {
                    public int getAnchorX() {
                        return player.getX();
                    }

                    public int getAnchorY() {
                        return player.getY() - 20;
                    }
                };
                player.getLevel().hudManager.addElement(text);
                return;
            }

            int seed = Item.getRandomAttackSeed(GameRandom.globalRandom);

            PlayerData playerData = PlayerDataList.getPlayerData(player);
            PlayerClassData playerClassData = playerData.getClassesData()[playerClass.id];
            int activeSkillLevel = playerClassData.getActiveSkillLevels()[activeSkill.id];

            activeSkill.runClient(player, playerData, activeSkillLevel, seed, isInUse());
            player.getClient().network.sendPacket(new ActiveAbilityRunPacket(player.getClient().getSlot(), skillSlot));
        }
    }

    public String canActive(PlayerMob player) {
        if (this.isEmpty()) return "equipfirst";
        ActiveSkill activeSkill = this.activeSkill;

        PlayerData playerData = PlayerDataList.getPlayerData(player);
        PlayerClassData playerClassData = playerData.getClassesData()[playerClass.id];
        int activeSkillLevel = playerClassData.getActiveSkillLevels()[activeSkill.id];

        if (playerClassData.getLevel(player.isServer()) < 1 || activeSkillLevel < 1) return "noactiveskill";

        boolean isInUseSkill = activeSkill.isInUseSkill();
        if (isInUseSkill) {
            boolean otherInUse = false;
            for (EquippedActiveSkill equippedActiveSkillP : playerData.equippedActiveSkills) {
                if (!equippedActiveSkillP.isEmpty() && !equippedActiveSkillP.isSameSkill(this)) {
                    if (equippedActiveSkillP.isInUse()) {
                        otherInUse = true;
                        break;
                    }
                }
            }
            if (otherInUse) return "anotheractiveskillinuse";
        }

        boolean isInUse = isInUseSkill && isInUse();

        if (isInUse && !activeSkill.canUseIfIsInUse()) return "activeskillinuse";

        long currentTime = player.getLevel().getTime();

        if (!isInUse) {
            int cooldownLeft = getCooldownLeft(activeSkillLevel, currentTime);
            if (cooldownLeft > 0) return "incooldown";
        }

        return activeSkill.canActive(player, playerData, isInUse);
    }
}
