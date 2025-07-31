package rpgclasses;

import necesse.engine.network.NetworkClient;
import necesse.engine.util.GameRandom;
import necesse.engine.util.gameAreaSearch.GameAreaStream;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.leaves.HumanAngerTargetAINode;
import necesse.level.maps.CollisionFilter;
import necesse.level.maps.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.MarkedBuff;

import java.awt.geom.Line2D;
import java.util.function.Predicate;

public class RPGUtils {

    // CUSTOM RUNABLES

    @FunctionalInterface
    public interface TriRunnable<T, U, V> {
        void run(T t, U u, V v);

        default TriRunnable<T, U, V> andThen(TriRunnable<? super T, ? super U, ? super V> after) {
            if (after == null) throw new NullPointerException();
            return (t, u, v) -> {
                this.run(t, u, v);
                after.run(t, u, v);
            };
        }
    }


    // STREAM MOBS
    public static GameAreaStream<Mob> streamMobs(Mob attacker, int range) {
        return streamMobs(attacker.getLevel(), attacker.x, attacker.y, range);
    }

    public static GameAreaStream<Mob> streamMobs(Level level, float x, float y, int range) {
        return level.entityManager.mobs.streamArea(x, y, range)
                .filter(isInRangeFilter(x, y, range));
    }

    // STREAM PLAYERS
    public static GameAreaStream<PlayerMob> streamPlayers(Mob attacker, int range) {
        return streamPlayers(attacker.getLevel(), attacker.x, attacker.y, range);
    }

    public static GameAreaStream<PlayerMob> streamPlayers(Level level, float x, float y, int range) {
        return level.entityManager.players.streamArea(x, y, range)
                .filter(isInRangeFilter(x, y, range));
    }

    // STREAM MOBS AND PLAYERS
    public static GameAreaStream<Mob> streamMobsAndPlayers(Mob attacker, int range) {
        return streamMobsAndPlayers(attacker.getLevel(), attacker.x, attacker.y, range);
    }

    public static GameAreaStream<Mob> streamMobsAndPlayers(Level level, float x, float y, int range) {
        return level.entityManager.streamAreaMobsAndPlayers(x, y, range)
                .filter(isInRangeFilter(x, y, range));
    }

    // VALID TARGET
    public static boolean isValidTarget(Mob attacker, Mob target) {
        return isValidTarget(attacker.isPlayer, attacker.isPlayer ? ((PlayerMob) attacker).getNetworkClient() : null, attacker, target);
    }

    public static boolean isValidTarget(boolean attackerIsPlayer, NetworkClient client, Mob attacker, Mob target) {
        if (!target.canTakeDamage()) return false;
        if (attackerIsPlayer) {
            if (!target.isHostile) {
                if (target.isHuman) {
                    HumanAngerTargetAINode<?> humanAngerHandler = (HumanAngerTargetAINode<?>) target.ai.blackboard.getObject(HumanAngerTargetAINode.class, "humanAngerHandler");
                    if (humanAngerHandler == null || !humanAngerHandler.enemies.contains(attacker)) return false;
                } else {
                    return false;
                }
            }
        }
        return target.canBeTargeted(attacker, client);
    }

    public static Predicate<Mob> isValidTargetFilter(Mob attacker) {
        boolean attackerIsPlayer = attacker.isPlayer;
        NetworkClient client = attacker.isPlayer ? ((PlayerMob) attacker).getNetworkClient() : null;

        return (m) -> isValidTarget(attackerIsPlayer, client, attacker, m);
    }

    public static Predicate<Mob> isValidAttackerFilter(Mob target) {
        return (m) -> isValidTarget(m, target);
    }


    // IN RANGE
    public static boolean isInRange(float x, float y, int maxDist, Mob target) {
        return isInRangeSq(x, y, maxDist * maxDist, target);
    }

    public static boolean isInRangeSq(float x, float y, int maxDistSq, Mob target) {
        float dx = target.getX() - x;
        float dy = target.getY() - y;
        return dx * dx + dy * dy <= maxDistSq;
    }


    public static Predicate<Mob> isInRangeFilter(float x, float y, int maxDist) {
        int maxDistanceSq = maxDist * maxDist;
        return m -> isInRangeSq(x, y, maxDistanceSq, m);
    }


    // IN VISION
    public static boolean isInVision(Level level, float x, float y, Mob target) {
        return isInVision(level, x, y, new CollisionFilter().projectileCollision(), target);
    }


    // MARKED
    public static Predicate<Mob> isMarkedFilter(PlayerMob player) {
        return m -> MarkedBuff.isMarked(player, m);
    }


    public static boolean isInVision(Level level, float x, float y, CollisionFilter collisionFilter, Mob target) {
        return !level.collides(new Line2D.Float(x, y, target.x, target.y), collisionFilter);
    }

    public static Predicate<Mob> inVisionFilter(Level level, float x, float y) {
        CollisionFilter collisionFilter = new CollisionFilter().projectileCollision();
        return m -> isInVision(level, x, y, collisionFilter, m);
    }


    // FIND BEST TARGET
    public static Mob findBestTarget(@NotNull Mob mob, int distance) {
        return findBestTarget(mob, mob.x, mob.y, distance, null);
    }

    public static Mob findBestTarget(@NotNull Mob mob, int distance, @Nullable Predicate<Mob> filter) {
        return findBestTarget(mob, mob.x, mob.y, distance, filter);
    }

    public static Mob findBestTarget(@NotNull Mob mob, float x, float y, int distance, @Nullable Predicate<Mob> filter) {
        return findBestTarget(mob.getLevel(), mob, x, y, distance, filter);
    }

    public static Mob findBestTarget(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {

        Mob[] bestHolder = new Mob[1];
        float[] bestDistSq = {Float.MAX_VALUE};
        int[] bestPriority = {-1};
        boolean attackerIsPlayer = attacker.isPlayer;

        NetworkClient client = attackerIsPlayer ? ((PlayerMob) attacker).getNetworkClient() : null;

        CollisionFilter collisionFilter = new CollisionFilter().projectileCollision();

        boolean hasFilter = filter != null;

        streamMobsAndPlayers(level, x, y, maxDistance)
                .filter(m -> {
                    if (!isValidTarget(attackerIsPlayer, client, attacker, m)) return false;
                    if (!isInVision(level, x, y, collisionFilter, m)) return false;

                    return (!hasFilter || filter.test(m));
                })
                .forEach(m -> {
                    float dx = m.getX() - x, dy = m.getY() - y;
                    float distSq = dx * dx + dy * dy;
                    int priority = 0;
                    if (attackerIsPlayer) {
                        if (MarkedBuff.isMarked((PlayerMob) attacker, m)) priority += 2;
                        if (m.isHostile) priority++;
                    }
                    if (priority > bestPriority[0] || (priority == bestPriority[0] && distSq < bestDistSq[0])) {
                        bestHolder[0] = m;
                        bestDistSq[0] = distSq;
                        bestPriority[0] = priority;
                    }
                });

        return bestHolder[0];
    }


    // GET RANDOM TARGET
    public static Mob getRandomTarget(@NotNull Mob mob, int distance) {
        return getRandomTarget(mob, distance, null);
    }

    public static Mob getRandomTarget(@NotNull Mob mob, int distance, @Nullable Predicate<Mob> filter) {
        return getRandomTarget(mob, mob.x, mob.y, distance, filter);
    }

    public static Mob getRandomTarget(@NotNull Mob mob, float x, float y, int distance, @Nullable Predicate<Mob> filter) {
        return getRandomTarget(mob.getLevel(), mob, x, y, distance, filter);
    }

    public static Mob getRandomTarget(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {

        Mob[] chosenHolder = new Mob[1];
        int[] count = {0};

        boolean attackerIsPlayer = attacker.isPlayer;
        NetworkClient client = attackerIsPlayer
                ? ((PlayerMob) attacker).getNetworkClient()
                : null;

        CollisionFilter collisionFilter = new CollisionFilter().projectileCollision();

        boolean hasFilter = filter != null;

        streamMobsAndPlayers(level, x, y, maxDistance)
                .filter(m -> {
                    if (!isValidTarget(attackerIsPlayer, client, attacker, m)) return false;
                    if (!isInVision(level, x, y, collisionFilter, m)) return false;

                    return (!hasFilter || filter.test(m));
                })
                .forEach(m -> {
                    count[0]++;
                    if (GameRandom.globalRandom.getChance(1.0f / count[0])) {
                        chosenHolder[0] = m;
                    }
                });

        return chosenHolder[0];
    }

}
