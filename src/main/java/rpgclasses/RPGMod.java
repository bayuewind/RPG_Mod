package rpgclasses;

import necesse.engine.modLoader.annotations.ModEntry;
import rpgclasses.content.MobClass;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.Attribute;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.registry.*;

@ModEntry
public class RPGMod {

    public static String currentVersion = "v0.3.3";

    static {
        new RPGModifiers();
    }

    public void preInit() {

        Config.startConfig();

    }

    public void init() {

        // Mod content
        MobClass.registerCore();

        Attribute.registerCore();

        PlayerClass.registerCore();

        PlayerClass.classesList.forEach(playerClass -> playerClass.passivesList.each(Passive::registerSkillBuffs));
        PlayerClass.classesList.forEach(playerClass -> playerClass.activeSkillsList.each(activeSkill -> {
            activeSkill.registerSkillBuffs();
            activeSkill.registerSkillLevelEvents();
        }));

        // Containers
        RPGContainers.registerCore();

        // Controls
        RPGControls.registerCore();

        // Packets
        RPGPackets.registerCore();

        // Commands
        RPGCommands.registerCore();

        // Level Events
        RPGLevelEvents.registerCore();

        // Items
        RPGItems.registerCore();

        // Buffs
        RPGBuffs.registerCore();

        // Mobs
        RPGMobs.registerCore();

        // Projectiles
        RPGProjectiles.registerCore();

        // Enchantments
        RPGEnchantments.registerCore();

    }

    public void postInit() {

        // Loot Tables
        RPGLootTables.modifyLootTables();

        // Recipes
        RPGRecipes.initRecipes();

    }

    public void initResources() {

        RPGResources.initResources();

    }

}
