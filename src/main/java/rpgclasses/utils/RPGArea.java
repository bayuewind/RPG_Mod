package rpgclasses.utils;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaType;
import aphorea.utils.magichealing.AphMagicHealing;
import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Predicate;

public class RPGArea extends AphArea {
    float attackerHealthMod = 1F;

    public Predicate<Mob> canApplyDebuff;

    public RPGArea(float range, Color... colors) {
        super(range, colors);
    }

    public RPGArea(float range, float alpha, Color... colors) {
        super(range, alpha, colors);
    }

    public RPGArea setAttackerHealthMod(float attackerHealthMod) {
        this.attackerHealthMod = attackerHealthMod;
        return this;
    }

    public RPGArea setDebuffFilter(Predicate<Mob> canApplyDebuff) {
        this.canApplyDebuff = canApplyDebuff;
        return this;
    }

    public void executeServer(Mob attacker, @NotNull Mob target, float x, float y, float modRange, InventoryItem item, ToolItem toolItem) {
        float distance = target.getDistance(x, y);
        if (this.position == 0 == isCenter(attacker, target, distance) || this.inRange(distance, modRange) && inVision(target, x, y)) {
            if (this.areaTypes.contains(AphAreaType.DAMAGE) && target != attacker && canAreaAttack(attacker, target)) {
                target.isServerHit(this.areaDamage, target.x - attacker.x, target.y - attacker.y, 0.0F, attacker);
            }

            if (this.areaTypes.contains(AphAreaType.HEALING) && (target == attacker || AphMagicHealing.canHealMob(attacker, target))) {
                int healing = this.areaHealing;
                if (target == attacker) healing = (int) (healing * attackerHealthMod);

                if (this.directExecuteHealing) {
                    AphMagicHealing.healMobExecute(attacker, target, healing, item, toolItem);
                } else {
                    AphMagicHealing.healMob(attacker, target, healing, item, toolItem);
                }
            }

            if (attacker.isServer()) {
                if (this.areaTypes.contains(AphAreaType.BUFF) && (target == attacker || target.isSameTeam(attacker))) {
                    Arrays.stream(this.buffs).forEach((buffID) -> {
                        target.buffManager.addBuff(new ActiveBuff(BuffRegistry.getBuff(buffID), target, this.buffDuration, attacker), true);
                    });
                }

                if (this.areaTypes.contains(AphAreaType.DEBUFF) && target != attacker && canApplyDebuff.test(target) && canAreaAttack(attacker, target)) {
                    Arrays.stream(this.debuffs).forEach((debuffID) -> {
                        target.buffManager.addBuff(new ActiveBuff(BuffRegistry.getBuff(debuffID), target, this.debuffDuration, attacker), true);
                    });
                }
            }
        }

    }
}
