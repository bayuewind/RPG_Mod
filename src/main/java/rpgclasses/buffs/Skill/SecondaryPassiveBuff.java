package rpgclasses.buffs.Skill;

import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.gfx.gameTexture.GameTexture;

import java.io.FileNotFoundException;

public class SecondaryPassiveBuff extends SkillBuff {
    public SecondaryPassiveBuff() {
        this.canCancel = false;
        this.isImportant = true;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
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
        return this.getStringID().replace("2passivebuff", "");
    }
}