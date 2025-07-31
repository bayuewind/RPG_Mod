package rpgclasses;

import necesse.engine.sound.gameSound.GameSound;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.ui.ButtonIcon;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.Attribute;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.mobs.summons.pasivesummon.RangerWolfMob;

public class RPGResources {

    public static void initResources() {
        initUIResources();
        initParticleResources();
        initMobResources();
        SOUNDS.initSoundResources();
    }

    public static class UI_TEXTURES {
        public static GameTexture emptyActiveSkill_texture;
        public static GameTexture rpgMenu_texture;
        public static GameTexture star_texture;
        public static GameTexture star2_texture;
        public static ButtonIcon[] add_icon;
        public static ButtonIcon[] remove_icon;
        public static ButtonIcon[] addSmall_icon;
        public static ButtonIcon[] removeSmall_icon;
        public static ButtonIcon[][] slot_icons;
    }

    public static void initUIResources() {
        UI_TEXTURES.emptyActiveSkill_texture = GameTexture.fromFile("ui/activeskills/empty");
        UI_TEXTURES.rpgMenu_texture = GameTexture.fromFile("ui/misc/rpgmenu");
        UI_TEXTURES.star_texture = GameTexture.fromFile("ui/misc/star");
        UI_TEXTURES.star2_texture = GameTexture.fromFile("ui/misc/star2");

        int styles = GameInterfaceStyle.styles.size();

        Attribute.attributesList.forEach(Attribute::initResources);
        PlayerClass.classesList.forEach(playerClass -> {
            playerClass.initResources();
            playerClass.passivesList.each(Passive::initResources);
            playerClass.activeSkillsList.each(ActiveSkill::initResources);
        });

        UI_TEXTURES.add_icon = new ButtonIcon[styles];
        UI_TEXTURES.remove_icon = new ButtonIcon[styles];

        UI_TEXTURES.addSmall_icon = new ButtonIcon[styles];
        UI_TEXTURES.removeSmall_icon = new ButtonIcon[styles];

        UI_TEXTURES.slot_icons = new ButtonIcon[styles][];

        for (int i = 0; i < styles; i++) {
            GameInterfaceStyle style = GameInterfaceStyle.styles.get(i);

            UI_TEXTURES.add_icon[i] = new ButtonIcon(style, "add");
            UI_TEXTURES.remove_icon[i] = new ButtonIcon(style, "remove");

            UI_TEXTURES.addSmall_icon[i] = new ButtonIcon(style, "add_small");
            UI_TEXTURES.removeSmall_icon[i] = new ButtonIcon(style, "remove_small");

            UI_TEXTURES.slot_icons[i] = new ButtonIcon[]{
                    new ButtonIcon(style, "slot1"),
                    new ButtonIcon(style, "slot2"),
                    new ButtonIcon(style, "slot3"),
                    new ButtonIcon(style, "slot4")
            };
        }

    }


    public static class PARTICLE_TEXTURES {
        public static GameTexture bearTrapOpen;
        public static GameTexture bearTrapClosed;
    }

    public static void initParticleResources() {
        PARTICLE_TEXTURES.bearTrapOpen = GameTexture.fromFile("particles/beartrap_open");
        PARTICLE_TEXTURES.bearTrapClosed = GameTexture.fromFile("particles/beartrap_closed");
    }

    public static void initMobResources() {
        RangerWolfMob.texture = GameTexture.fromFile("mobs/rangerwolf");
    }

    public static class SOUNDS {
        public static GameSound Zap;

        public static void initSoundResources() {
            Zap = GameSound.fromFile("zap");
        }
    }
}
