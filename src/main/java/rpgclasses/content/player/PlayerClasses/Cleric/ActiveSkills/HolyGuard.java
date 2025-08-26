package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.registry.AphModifiers;
import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.HumanDrawBuff;
import necesse.gfx.GameResources;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class HolyGuard extends SimpleBuffActiveSkill {

    public HolyGuard(int levelMax, int requiredClassLevel) {
        super("holyguard", "#ffff00", levelMax, requiredClassLevel);
    }

    @Override
    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        super.giveBuff(player, player, playerData, activeSkillLevel);

        GameUtils.streamServerClients(player.getLevel()).forEach(targetPlayer -> {
            if (targetPlayer.isSameTeam(player.getTeam()))
                super.giveBuff(player, targetPlayer.playerMob, playerData, activeSkillLevel);
        });

        RPGUtils.streamMobsAndPlayers(player, 200)
                .filter(m -> m == player || m.isSameTeam(player))
                .forEach(
                        target -> super.giveBuff(player, target, playerData, activeSkillLevel)
                );
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.cling, SoundEffect.effect(player.x, player.y).volume(2F).pitch(1F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(200, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 40 + activeSkillLevel * 8;
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 30000;
    }

    @Override
    public int getBaseCooldown() {
        return 60000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -6000;
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new HolyBuff();
    }

    public class HolyBuff extends ActiveSkillBuff implements HumanDrawBuff {
        public GameTexture starBarrierTexture;

        public void loadTextures() {
            super.loadTextures();
            this.starBarrierTexture = GameTexture.fromFile("particles/starbarrier");
        }

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            int level = getLevel(activeBuff);
            activeBuff.setModifier(AphModifiers.MAGIC_HEALING_RECEIVED, level * 0.1F);
        }

        @Override
        public void onBeforeHitCalculated(ActiveBuff activeBuff, MobBeforeHitCalculatedEvent event) {
            super.onBeforeHitCalculated(activeBuff, event);
            if (!event.isPrevented()) {
                activeBuff.owner.buffManager.removeBuff(activeBuff.buff, false);
            }
        }

        @Override
        public void addHumanDraw(ActiveBuff activeBuff, HumanDrawOptions humanDrawOptions) {
            if (activeBuff.getGndData().getBoolean("ready")) {
                humanDrawOptions.addTopDraw(
                        (player, dir, spriteX, spriteY, spriteRes, drawX, drawY, width, height, mirrorX, mirrorY, light, alpha, mask) ->
                                this.starBarrierTexture.initDraw().sprite((int) (player.getLocalTime() / 100L) % 4, 0, 64).color(getColor()).size(width, height).addMaskShader(mask).pos(drawX, drawY).alpha(0.4F)
                );
            }
        }
    }
}
