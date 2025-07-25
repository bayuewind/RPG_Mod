package rpgclasses.buffs.Skill;

import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.gfx.gameTexture.GameTexture;

import java.io.FileNotFoundException;

public class PrincipalPassiveBuff extends PassiveSkillBuff {
    public PrincipalPassiveBuff() {
    }

    @Override
    public void loadTextures() {
        try {
            this.iconTexture = GameTexture.fromFileRaw("ui/passives/" + activeSkillID());
        } catch (FileNotFoundException var2) {
            this.iconTexture = GameTexture.fromFile("buffs/positive");
        }
    }

    @Override
    public void updateLocalDisplayName() {
        this.displayName = new LocalMessage("passives", activeSkillID());
    }

    public String activeSkillID() {
        return this.getStringID().replace("passivebuff", "");
    }
}