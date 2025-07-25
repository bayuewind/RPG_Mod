package rpgclasses.methodpatches;


import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.ServerClient;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.hostile.SandwormBody;
import necesse.entity.mobs.hostile.SlimeWormBody;
import necesse.entity.mobs.hostile.bosses.PestWardenBody;
import necesse.entity.mobs.hostile.bosses.SwampGuardianBody;
import net.bytebuddy.asm.Advice;
import rpgclasses.Config;
import rpgclasses.data.MobData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.util.HashSet;

public class MobPatches {

    @ModMethodPatch(target = Mob.class, name = "init", arguments = {})
    public static class init {
        @Advice.OnMethodExit
        static void onExit(@Advice.This Mob This) {
            MobData.initMob(This, This.getLevel());
        }
    }

    @ModMethodPatch(target = Mob.class, name = "addSaveData", arguments = {SaveData.class})
    public static class addSaveData {

        @Advice.OnMethodExit
        static void onExit(@Advice.This Mob This, @Advice.Argument(0) SaveData save) {
            MobData mobData = MobData.getMob(This);
            if (mobData != null) {
                mobData.saveData(save);
            }
        }

    }

    @ModMethodPatch(target = Mob.class, name = "applyLoadData", arguments = {LoadData.class})
    public static class applyLoadData {

        @Advice.OnMethodExit
        static void onExit(@Advice.This Mob This, @Advice.Argument(0) LoadData save) {
            MobData.loadData(save, This);
        }

    }

    @ModMethodPatch(target = Mob.class, name = "setupSpawnPacket", arguments = {PacketWriter.class})
    public static class setupSpawnPacket {

        @Advice.OnMethodExit
        static void onExit(@Advice.This Mob This, @Advice.Argument(0) PacketWriter writer) {
            MobData mobData = MobData.getMob(This);
            if (mobData != null) {
                mobData.setupSpawnPacket(writer);
            }
        }

    }

    @ModMethodPatch(target = Mob.class, name = "applySpawnPacket", arguments = {PacketReader.class})
    public static class applySpawnPacket {

        @Advice.OnMethodExit
        static void onExit(@Advice.This Mob This, @Advice.Argument(0) PacketReader reader) {
            MobData.applySpawnPacket(reader, This);
        }

    }

    @ModMethodPatch(target = Mob.class, name = "onDeath", arguments = {Attacker.class, HashSet.class})
    public static class MobDeathPatch {

        @Advice.OnMethodEnter
        public static void onExit(@Advice.This Mob This, @Advice.Argument(0) Attacker attacker, @Advice.Argument(1) HashSet<Attacker> attackers) {
            MobData mobData = MobData.getMob(This);
            if (This.isServer() && mobData != null) {

                float exp = mobData.level * 2 * mobData.mobClass.expMod * GameRandom.globalRandom.getFloatBetween(0.9F, 1.1F) * Config.getExperienceMod();
                exp = (float) Math.pow(exp, 1.2F);

                HashSet<PlayerMob> processedPlayers = new HashSet<>();
                for (Attacker attackerMob : attackers) {
                    if (attackerMob.getAttackOwner() != null && attackerMob.getAttackOwner().isPlayer) {
                        PlayerMob player = (PlayerMob) attackerMob.getAttackOwner();

                        if (processedPlayers.contains(player)) {
                            continue;
                        }

                        processedPlayers.add(player);

                        ServerClient serverClient = player.getServerClient();

                        final int finalExp;
                        if (serverClient.characterStats().mob_kills.getKills(This.getStringID()) == 0) {
                            finalExp = (int) (exp * Config.getFirstKillBonus());
                            serverClient.sendChatMessage(new LocalMessage("message", "newmobkill", "mob", mobData.realName(), "exp", finalExp));
                        } else {
                            finalExp = (int) exp;
                        }

                        if (finalExp > 0) {
                            PlayerData playerData = PlayerDataList.getPlayerData(player);
                            playerData.modExpSendPacket(serverClient, finalExp);
                        }
                    }
                }
            }
        }
    }

    @ModMethodPatch(target = Mob.class, name = "getLocalization", arguments = {})
    public static class getLocalization {
        @Advice.OnMethodExit
        static void onExit(@Advice.This Mob This, @Advice.Return(readOnly = false) GameMessage gameMessage) {
            MobData mobData = MobData.getMob(This);
            if (mobData != null) {
                gameMessage = new LocalMessage("mob", This.isBoss() ? "rpgmobname" : "rpgmobnamecolor", "name", gameMessage.translate(), "level", mobData.level, "class", mobData.mobClass.getName(), "color", mobData.mobClass.color);
            }
        }
    }

    @ModMethodPatch(target = SwampGuardianBody.class, name = "getLocalization", arguments = {})
    public static class getLocalizationSwampGuardianBody {
        @Advice.OnMethodExit
        static void onExit(@Advice.This SwampGuardianBody This, @Advice.Return(readOnly = false) GameMessage gameMessage) {
            MobData mobData = MobData.getMob(This);
            if (mobData != null) {
                gameMessage = new LocalMessage("mob", This.isBoss() ? "rpgmobname" : "rpgmobnamecolor", "name", gameMessage.translate(), "level", mobData.level, "class", mobData.mobClass.getName(), "color", mobData.mobClass.color);
            }
        }
    }

    @ModMethodPatch(target = SlimeWormBody.class, name = "getLocalization", arguments = {})
    public static class getLocalizationSlimeWormBody {
        @Advice.OnMethodExit
        static void onExit(@Advice.This SlimeWormBody This, @Advice.Return(readOnly = false) GameMessage gameMessage) {
            MobData mobData = MobData.getMob(This);
            if (mobData != null) {
                gameMessage = new LocalMessage("mob", This.isBoss() ? "rpgmobname" : "rpgmobnamecolor", "name", gameMessage.translate(), "level", mobData.level, "class", mobData.mobClass.getName(), "color", mobData.mobClass.color);
            }
        }
    }

    @ModMethodPatch(target = SandwormBody.class, name = "getLocalization", arguments = {})
    public static class getLocalizationSandwormBody {
        @Advice.OnMethodExit
        static void onExit(@Advice.This SandwormBody This, @Advice.Return(readOnly = false) GameMessage gameMessage) {
            MobData mobData = MobData.getMob(This);
            if (mobData != null) {
                gameMessage = new LocalMessage("mob", This.isBoss() ? "rpgmobname" : "rpgmobnamecolor", "name", gameMessage.translate(), "level", mobData.level, "class", mobData.mobClass.getName(), "color", mobData.mobClass.color);
            }
        }
    }

    @ModMethodPatch(target = PestWardenBody.class, name = "getLocalization", arguments = {})
    public static class getLocalizationPestWardenBody {
        @Advice.OnMethodExit
        static void onExit(@Advice.This PestWardenBody This, @Advice.Return(readOnly = false) GameMessage gameMessage) {
            MobData mobData = MobData.getMob(This);
            if (mobData != null) {
                gameMessage = new LocalMessage("mob", This.isBoss() ? "rpgmobname" : "rpgmobnamecolor", "name", gameMessage.translate(), "level", mobData.level, "class", mobData.mobClass.getName(), "color", mobData.mobClass.color);
            }
        }
    }
}
