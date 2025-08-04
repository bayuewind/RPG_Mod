package rpgclasses.content.player;

import necesse.engine.localization.Localization;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import rpgclasses.content.player.PlayerClasses.Necromancer.NecromancerPlayerClass;
import rpgclasses.content.player.PlayerClasses.Ranger.RangerPlayerClass;
import rpgclasses.content.player.PlayerClasses.Warrior.WarriorPlayerClass;
import rpgclasses.content.player.PlayerClasses.Wizard.WizardPlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.content.player.SkillsAndAttributes.SkillsList;
import rpgclasses.data.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class PlayerClass {
    public static Map<String, PlayerClass> classes = new HashMap<>();
    public static List<PlayerClass> classesList = new ArrayList<>();

    public static void registerCore() {
        registerClass(new WarriorPlayerClass());
        registerClass(new RangerPlayerClass());
        registerClass(new WizardPlayerClass());
        registerClass(new NecromancerPlayerClass());
        registerClass(new UpcomingPlayerClass("cleric", "#FFD60A"));
        registerClass(new UpcomingPlayerClass("rogue", "#8B0000"));
        registerClass(new UpcomingPlayerClass("warlock", "#B22222"));
        registerClass(new UpcomingPlayerClass("paladin", "#F1C40F"));
        registerClass(new UpcomingPlayerClass("druid", "#27AE60"));
    }

    public static void registerClass(PlayerClass playerClass) {
        classes.put(playerClass.stringID, playerClass);
        classesList.add(playerClass);

        playerClass.activeSkillsList.each(
                activeSkill -> activeSkill.playerClass = playerClass
        );
        playerClass.passivesList.each(
                activeSkill -> activeSkill.playerClass = playerClass
        );
    }

    public final int id;
    public final String stringID;
    public final String color;
    public final SkillsList<ActiveSkill> activeSkillsList;
    public final SkillsList<Passive> passivesList;
    public GameTexture texture;

    public PlayerClass(String stringID, String color) {
        this.id = classes.size();
        this.stringID = stringID;
        this.color = color;
        this.activeSkillsList = getActiveSkillsList();
        this.passivesList = getPassivesList();
    }

    public void initResources() {
        texture = GameTexture.fromFile("ui/classes/" + stringID);
    }

    public ListGameTooltips getToolTips() {
        ListGameTooltips tooltips = new ListGameTooltips();
        tooltips.add("§" + color + Localization.translate("classes", stringID));
        return tooltips;
    }

    public int getLevel(PlayerData playerData) {
        return playerData.getClassLevel(id);
    }

    public boolean isAvailable() {
        return true;
    }

    abstract public SkillsList<ActiveSkill> getActiveSkillsList();

    abstract public SkillsList<Passive> getPassivesList();
}
