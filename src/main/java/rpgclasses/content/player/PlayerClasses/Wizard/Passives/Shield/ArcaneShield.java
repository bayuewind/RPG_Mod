package rpgclasses.content.player.PlayerClasses.Wizard.Passives.Shield;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.HumanDrawBuff;
import necesse.gfx.GameResources;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.registry.RPGPackets;
import rpgclasses.utils.RPGUtils;

import java.awt.*;

public class ArcaneShield extends SimpleBuffPassive {
    public ArcaneShield(int levelMax, int requiredClassLevel) {
        super("arcaneshield", "#6633ff", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new MagicShieldBuff(getColor(), 22000, 1000, null);
    }

    public static class MagicShieldBuff extends PrincipalPassiveBuff implements HumanDrawBuff {
        public GameTexture starBarrierTexture;

        public void loadTextures() {
            super.loadTextures();
            this.starBarrierTexture = GameTexture.fromFile("particles/starbarrier");
        }

        public Color shieldColor;
        public int baseCooldown;
        public int cooldownReductionPerLevel;
        public RPGUtils.TriRunnable<ActiveBuff, Integer, PlayerData> onPrevent;

        public MagicShieldBuff(Color shieldColor, int baseCooldown, int cooldownReductionPerLevel, RPGUtils.TriRunnable<ActiveBuff, Integer, PlayerData> onPrevent) {
            this.shieldColor = shieldColor;
            this.baseCooldown = baseCooldown;
            this.cooldownReductionPerLevel = cooldownReductionPerLevel;
            this.onPrevent = onPrevent;
        }

        public int getCooldown(ActiveBuff activeBuff) {
            return baseCooldown - (cooldownReductionPerLevel > 0 ? cooldownReductionPerLevel * getLevel(activeBuff) : 0);
        }

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            activeBuff.getGndData().setInt("time", 50);
        }

        @Override
        public void serverTick(ActiveBuff activeBuff) {
            super.serverTick(activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;
                if (time >= getCooldown(activeBuff)) {
                    activeBuff.getGndData().setBoolean("ready", true);
                }
                activeBuff.getGndData().setInt("time", time);
            }
        }

        @Override
        public void clientTick(ActiveBuff activeBuff) {
            super.clientTick(activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0);
                if (time == 0) {
                    SoundManager.playSound(GameResources.shatter2, SoundEffect.effect(activeBuff.owner).volume(2.0F).pitch(0.8F));
                }
                time += 50;
                if (time >= getCooldown(activeBuff)) {
                    activeBuff.getGndData().setBoolean("ready", true);
                    SoundManager.playSound(GameResources.cling, SoundEffect.effect(activeBuff.owner).volume(1F));
                    SoundManager.playSound(GameResources.jingle, SoundEffect.effect(activeBuff.owner).volume(1F));
                }
                activeBuff.getGndData().setInt("time", time);
            }
        }

        @Override
        public void onBeforeHitCalculated(ActiveBuff activeBuff, MobBeforeHitCalculatedEvent event) {
            super.onBeforeHitCalculated(activeBuff, event);
            if (activeBuff.getGndData().getBoolean("ready") && !event.isPrevented()) {
                event.prevent();
                event.showDamageTip = false;
                event.playHitSound = false;

                PlayerMob player = (PlayerMob) activeBuff.owner;

                activeBuff.getGndData().setBoolean("ready", false);
                activeBuff.getGndData().setInt("time", 0);
                player.getServer().network.sendToClientsAtEntireLevel(new RPGPackets.ResetSkillTime(player.getPlayerSlot(), activeBuff.buff.getStringID()), player.getLevel());

                if (onPrevent != null)
                    onPrevent.run(activeBuff, getLevel(activeBuff), PlayerDataList.getPlayerData(player));
            }
        }

        @Override
        public void addHumanDraw(ActiveBuff activeBuff, HumanDrawOptions humanDrawOptions) {
            if (activeBuff.getGndData().getBoolean("ready")) {
                humanDrawOptions.addTopDraw(
                        (player, dir, spriteX, spriteY, spriteRes, drawX, drawY, width, height, mirrorX, mirrorY, light, alpha, mask) ->
                                this.starBarrierTexture.initDraw().sprite((int) (player.getLocalTime() / 100L) % 4, 0, 64).color(shieldColor).size(width, height).addMaskShader(mask).pos(drawX, drawY).alpha(0.4F)
                );
            }
        }

        @Override
        public void drawIcon(int x, int y, ActiveBuff activeBuff) {
            super.drawIcon(x, y, activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0) - 50;
                String text = EquippedActiveSkill.getTimeLeftString(getCooldown(activeBuff) - time);
                int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
                FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
            }
        }
    }
}
