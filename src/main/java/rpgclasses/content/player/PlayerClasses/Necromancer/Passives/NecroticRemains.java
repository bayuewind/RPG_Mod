package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;

public class NecroticRemains extends SimpleBuffPassive {
    public NecroticRemains(int levelMax, int requiredClassLevel) {
        super("necroticremains", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff();
    }
}
