package rpgclasses.data;

import aphorea.biomes.InfectedFieldsBiome;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.MobRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.LevelMob;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.WormMobBody;
import necesse.entity.mobs.WormMobHead;
import necesse.entity.mobs.hostile.bosses.CrystalDragonBody;
import necesse.entity.mobs.hostile.bosses.CrystalDragonHead;
import necesse.entity.mobs.hostile.bosses.NightSwarmBatMob;
import necesse.level.maps.IncursionLevel;
import necesse.level.maps.Level;
import necesse.level.maps.biomes.Biome;
import necesse.level.maps.biomes.desert.DesertBiome;
import necesse.level.maps.biomes.dungeon.DungeonLevel;
import necesse.level.maps.biomes.plains.PlainsBiome;
import necesse.level.maps.biomes.snow.SnowBiome;
import necesse.level.maps.biomes.swamp.SwampBiome;
import necesse.level.maps.biomes.temple.TempleLevel;
import necesse.level.maps.incursion.DesertDeepCaveIncursionLevel;
import necesse.level.maps.incursion.SnowDeepCaveIncursionLevel;
import rpgclasses.content.MobClass;

import java.util.HashMap;
import java.util.Map;

public class MobData {
    public static String prefixDataName = "rpgmod_";
    public static String levelDataName = prefixDataName + "level";
    public static String classDataName = prefixDataName + "class";

    public Mob mob;
    public int level;
    public MobClass mobClass;

    public static Map<Integer, MobData> mobsData = new HashMap<>();

    public static MobData getMob(Mob mob) {
        if(mob instanceof WormMobBody) {
            WormMobBody<?, ?> body = (WormMobBody<?, ?>) mob;
            WormMobHead<?, ?> master = body.master.get(mob.getLevel());
            return master == null ? null : getMob(master.getUniqueID());
        } else {
            return getMob(mob.getUniqueID());
        }
    }

    public static MobData getMob(int uniqueID) {
        return mobsData.getOrDefault(uniqueID, null);
    }

    public static boolean shouldInitMob(Mob mob) {
        return ((mob.isHostile && !mob.isPlayer) || mob.isBoss()) && mob.getClass() != WormMobBody.class && mob.getClass() != NightSwarmBatMob.class;
    }

    public static void initMob(Mob mob, Level mapLevel) {
        if (mapLevel != null && shouldInitMob(mob) && getMob(mob) == null) {
            MobData mobData = new MobData();
            mobData.mob = mob;

            boolean isBoss = mob.isBoss();

            mobData.level = isBoss ? 5 : GameRandom.globalRandom.getIntBetween(1, 5);

            Biome biome = mapLevel.biome;

            if (mapLevel.isIncursionLevel) {
                IncursionLevel incursionLevel = (IncursionLevel) mapLevel;
                mobData.level += 20 + 5 * incursionLevel.incursionData.getTabletTier();
            } else {
                int dimension = mapLevel.getIslandDimension();

                if (mapLevel instanceof DungeonLevel) {
                    mobData.level += 5;
                } else if (mapLevel instanceof TempleLevel) {
                    mobData.level += 25;
                } else if (dimension != -2) {
                    if (biome instanceof SnowBiome) {
                        mobData.level += 3;
                    } else if (biome instanceof PlainsBiome) {
                        mobData.level += 8;
                    } else if (biome instanceof SwampBiome) {
                        mobData.level += 10;
                    } else if (biome instanceof DesertBiome) {
                        mobData.level += 12;
                    } else if (biome instanceof InfectedFieldsBiome) {
                        mobData.level += 16;
                    }
                } else {
                    if (biome instanceof PlainsBiome) {
                        mobData.level += 15;
                    } else if (biome instanceof SnowBiome) {
                        mobData.level += 18;
                    } else if (biome instanceof SwampBiome) {
                        mobData.level += 20;
                    } else if (biome instanceof DesertBiome) {
                        mobData.level += 22;
                    } else {
                        mobData.level += 14;
                    }
                }
            }

            mobData.mobClass = isBoss ? MobClass.bossClass : MobClass.getRandomClass();

            if ((mapLevel instanceof SnowDeepCaveIncursionLevel || biome instanceof SnowBiome) && mobData.mobClass.is("explosive")) {
                mobData.mobClass = MobClass.allClasses.get("glacial");
            } else if ((mapLevel instanceof DesertDeepCaveIncursionLevel || biome instanceof DesertBiome) && mobData.mobClass.is("glacial")) {
                mobData.mobClass = MobClass.allClasses.get("explosive");
            }

            mobsData.put(mob.getUniqueID(), mobData);

            mobData.mobClass.giveBuff(mob, mobData.level);
            mobData.mobClass.setMaxHealth(mob, mobData.level);
        }
    }

    public static void loadData(LoadData loadData, Mob mob) {
        if (shouldInitMob(mob)) {
            MobData mobData = new MobData();
            mobData.mob = mob;

            mobData.level = loadData.getInt(levelDataName, 0);
            int mobClassID = loadData.getInt(classDataName, -1);
            if (mobData.level > 0 && mobClassID != -1) {
                mobData.mobClass = MobClass.allClassesList.get(mobClassID);

                mobsData.put(mob.getUniqueID(), mobData);
            }
        }
    }

    public void saveData(SaveData saveData) {
        saveData.addInt(levelDataName, level);
        saveData.addInt(classDataName, mobClass.id);
    }

    public static void applySpawnPacket(PacketReader reader, Mob mob) {
        if (shouldInitMob(mob)) {
            MobData mobData = new MobData();
            mobData.level = reader.getNextInt();
            int mobClassID = reader.getNextInt();

            if (mobData.level > 0 && (mob.isBoss() == (mobClassID == 0))) {
                mobData.mobClass = MobClass.allClassesList.get(mobClassID);
                mobData.mob = mob;
                mobsData.put(mob.getUniqueID(), mobData);
            }
        }
    }

    public void setupSpawnPacket(PacketWriter writer) {
        writer.putNextInt(level);
        writer.putNextInt(mobClass.id);
    }

    public String realName() {
        return MobRegistry.getLocalization(mob.getID()).translate();
    }
}
