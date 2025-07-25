package rpgclasses.content.player.SkillsAndAttributes.Passives;

import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.data.PlayerData;

abstract public class SimpleBuffPassive extends Passive {
    public boolean containsComplexTooltips;

    public SimpleBuffPassive(String stringID, String color, int levelMax, int requiredClassLevel, boolean containsComplexTooltips) {
        super(stringID, color, levelMax, requiredClassLevel);
        this.containsComplexTooltips = containsComplexTooltips;
    }

    public SimpleBuffPassive(String stringID, String color, int levelMax, int requiredClassLevel) {
        this(stringID, color, levelMax, requiredClassLevel, true);
    }

    @Override
    public void givePassiveBuff(PlayerMob player, PlayerData playerData, int passiveLevel) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(getBuffStringID()), player, 1000, null);
        ab.getGndData().setInt("skillLevel", passiveLevel);
        ab.getGndData().setInt("endurance", playerData.getEndurance(player));
        ab.getGndData().setInt("speed", playerData.getSpeed(player));
        ab.getGndData().setInt("strength", playerData.getStrength(player));
        ab.getGndData().setInt("intelligence", playerData.getIntelligence(player));
        ab.getGndData().setInt("grace", playerData.getGrace(player));
        player.buffManager.addBuff(ab, true, true);
    }

    public void giveDatalessSecondaryPassiveBuff(PlayerMob player, int duration) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(getSecondaryBuffStringID()), player, duration, null);
        player.buffManager.addBuff(ab, true, true);
    }

    public void giveSecondaryPassiveBuff(PlayerMob player, PlayerData playerData, int passiveLevel, int duration) {
        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(getSecondaryBuffStringID()), player, duration, null);
        ab.getGndData().setInt("skillLevel", passiveLevel);
        ab.getGndData().setInt("endurance", playerData.getEndurance(player));
        ab.getGndData().setInt("speed", playerData.getSpeed(player));
        ab.getGndData().setInt("strength", playerData.getStrength(player));
        ab.getGndData().setInt("intelligence", playerData.getIntelligence(player));
        ab.getGndData().setInt("grace", playerData.getGrace(player));
        player.buffManager.addBuff(ab, true, true);
    }

    @Override
    public void removePassiveBuffs(PlayerMob player) {
        player.buffManager.removeBuff(getBuffStringID(), true);
    }

    @Override
    public void registerSkillBuffs() {
        BuffRegistry.registerBuff(getBuffStringID(), getBuff());
        SecondaryPassiveBuff secondaryPassiveBuff = getSecondaryBuff();
        if (secondaryPassiveBuff != null) BuffRegistry.registerBuff(getSecondaryBuffStringID(), secondaryPassiveBuff);
    }

    abstract public PrincipalPassiveBuff getBuff();

    public SecondaryPassiveBuff getSecondaryBuff() {
        return null;
    }

    public String getBuffStringID() {
        return stringID + "passivebuff";
    }

    public String getSecondaryBuffStringID() {
        return stringID + "2passivebuff";
    }

    @Override
    public boolean containsComplexTooltips() {
        return containsComplexTooltips;
    }
}
