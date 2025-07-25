package rpgclasses.buffs;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.particle.Particle;

import java.awt.*;

public class MagicPoisonBuff extends Buff {
    public MagicPoisonBuff() {
        this.shouldSave = false;
        this.isImportant = true;
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
            owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(150, 0, 150)).height(16.0F);
        }

    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        activeBuff.setModifier(BuffModifiers.POISON_DAMAGE_FLAT, activeBuff.getGndData().getFloat("poisonDamage"));
    }
}
