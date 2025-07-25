package rpgclasses.buffs.Skill;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.staticBuffs.Buff;

abstract public class SkillBuff extends Buff {
    public int getLevel(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("skillLevel");
    }

    public int getEndurance(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("endurance");
    }

    public int getSpeed(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("speed");
    }

    public int getStrength(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("strength");
    }

    public int getIntelligence(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("intelligence");
    }

    public int getGrace(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("grace");
    }
}