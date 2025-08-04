package rpgclasses.mobs.ai;

import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.ai.behaviourTree.AINode;
import necesse.entity.mobs.ai.behaviourTree.AINodeResult;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.composites.SelectorAINode;
import necesse.entity.mobs.ai.behaviourTree.composites.SequenceAINode;
import necesse.entity.mobs.ai.behaviourTree.leaves.CollisionChaserAINode;
import necesse.entity.mobs.ai.behaviourTree.leaves.FollowerBaseSetterAINode;
import necesse.entity.mobs.ai.behaviourTree.leaves.PlayerFollowerAINode;
import necesse.entity.mobs.ai.behaviourTree.leaves.SummonTargetFinderAINode;
import necesse.entity.mobs.itemAttacker.ItemAttackerMob;

public class PassiveSummonCollisionChaserAI<T extends Mob> extends SelectorAINode<T> {
    public GameDamage damage;
    public int knockback;

    public PassiveSummonCollisionChaserAI(int searchDistance, GameDamage damage, int knockback, int hitCooldown, int teleportDistance, int stoppingDistance, boolean defaultTargets) {
        this.damage = damage;
        this.knockback = knockback;
        SequenceAINode<T> chaserSequence = new SequenceAINode<>();
        chaserSequence.addChild(new FollowerBaseSetterAINode<>());
        chaserSequence.addChild(new SkillSummonFocusTargetSetterAINode<T>(defaultTargets, searchDistance) {
            public Mob getCustomFocus(T mob, int searchDistance) {
                return PassiveSummonCollisionChaserAI.this.getCustomFocus(mob, searchDistance);
            }
        });
        if (defaultTargets) chaserSequence.addChild(new SummonTargetFinderAINode<>(searchDistance));
        CollisionChaserAINode<T> chaser = new CollisionChaserAINode<T>() {
            public boolean attackTarget(T mob, Mob target) {
                return PassiveSummonCollisionChaserAI.this.attackTarget(mob, target);
            }
        };
        chaser.hitCooldowns.hitCooldown = hitCooldown;
        chaser.attackMoveCooldown = 0;
        chaserSequence.addChild(chaser);
        this.addChild(chaserSequence);
        this.addChild(new PlayerFollowerAINode<>(teleportDistance, stoppingDistance));
    }

    public boolean attackTarget(T mob, Mob target) {
        return CollisionChaserAINode.simpleAttack(mob, target, this.damage, this.knockback);
    }

    public Mob getCustomFocus(T mob, int searchDistance) {
        return null;
    }

    public static class SkillSummonFocusTargetSetterAINode<T extends Mob> extends AINode<T> {
        public String focusTargetKey;
        public boolean defaultTargets;
        public int searchDistance;

        public SkillSummonFocusTargetSetterAINode(String focusTargetKey, boolean defaultTargets, int searchDistance) {
            this.focusTargetKey = focusTargetKey;
            this.defaultTargets = defaultTargets;
            this.searchDistance = searchDistance;
        }

        public SkillSummonFocusTargetSetterAINode(boolean defaultTargets, int searchDistance) {
            this("currentTarget", defaultTargets, searchDistance);
        }

        protected void onRootSet(AINode<T> root, T mob, Blackboard<T> blackboard) {
        }

        public void init(T mob, Blackboard<T> blackboard) {
        }

        public AINodeResult tick(T mob, Blackboard<T> blackboard) {
            Mob customFocus = this.getCustomFocus(mob, searchDistance);
            if (customFocus != null) {
                blackboard.put(this.focusTargetKey, customFocus);
            } else {
                blackboard.put(this.focusTargetKey, null);
                if (defaultTargets) {
                    ItemAttackerMob followingAttacker = mob.getFollowingItemAttacker();
                    if (followingAttacker != null) {
                        blackboard.put(this.focusTargetKey, followingAttacker.getSummonFocusMob());
                    }
                }
            }

            return AINodeResult.SUCCESS;
        }

        public Mob getCustomFocus(T mob, int searchDistance) {
            return null;
        }
    }
}
