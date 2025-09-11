package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import rpgclasses.buffs.IgnitedBuff;
import rpgclasses.content.player.Logic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

import java.awt.*;

public class Inferno extends ActiveSkill {

    public Inferno(int levelMax, int requiredClassLevel) {
        super("inferno", "#990000", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        float damage = 0.5F * playerData.getLevel() + 0.5F * playerData.getIntelligence(player) * activeSkillLevel;
        RPGUtils.streamMobsAndPlayers(player, 200)
                .filter(RPGUtils.isValidTargetFilter(player))
                .forEach(
                        target -> IgnitedBuff.apply(player, target, damage, 10F, false)
                );
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 40 + activeSkillLevel * 8;
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.firespell1, SoundEffect.effect(player.x, player.y).volume(1.5F).pitch(0.5F));
        SoundManager.playSound(GameResources.croneLaugh, SoundEffect.effect(player.x, player.y).volume(1.5F).pitch(0.8F));

        AphAreaList areaList = new AphAreaList(
                new AphArea(300,
                        new Color(255, 51, 0),
                        new Color(255, 102, 0),
                        new Color(255, 153, 0)
                )
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyTarget(player, 300) ? null : "notarget";
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

}