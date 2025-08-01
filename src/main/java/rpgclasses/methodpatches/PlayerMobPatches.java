package rpgclasses.methodpatches;

import necesse.engine.GlobalData;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.gfx.forms.components.FormExpressionWheel;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.placeableItem.objectItem.ObjectItem;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.util.HashSet;

public class PlayerMobPatches {
    @ModMethodPatch(target = PlayerMob.class, name = "applyLoadData", arguments = {LoadData.class})
    public static class applyLoadData {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This, @Advice.Argument(0) LoadData loadData) {
            PlayerData playerData = PlayerDataList.getPlayerData(This);
            playerData.loadData(This, loadData);
            playerData.updateAllBuffs(This);
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "addSaveData", arguments = {SaveData.class})
    public static class addSaveData {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This, @Advice.Argument(0) SaveData saveData) {
            PlayerData player = PlayerDataList.getPlayerData(This);
            player.saveData(saveData);
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "restore", arguments = {})
    public static class restore {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This) {
            PlayerData playerData = PlayerDataList.getPlayerData(This);
            playerData.updateAllBuffs(This);
            for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                equippedActiveSkill.lastUse = 0;
            }
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "onDeath", arguments = {Attacker.class, HashSet.class})
    public static class onDeath {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This) {
            PlayerData playerData = PlayerDataList.getPlayerData(This);
            if (playerData.grabbedObject != null) {
                Level level = This.getLevel();
                if (!level.isProtected && playerData.grabbedObject.canPlace(level, This.getTileX(), This.getTileY(), 2, true) == null) {
                    playerData.grabbedObject.placeObject(level, This.getTileX(), This.getTileY(), 2, true);
                } else {
                    ObjectItem objectItem = playerData.grabbedObject.getObjectItem();
                    if (objectItem != null) {
                        level.entityManager.pickups.add(
                                new ItemPickupEntity(level, new InventoryItem(objectItem), This.x, This.y, 0, 0)
                        );
                    }
                }
            }
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "startExpression", arguments = {FormExpressionWheel.Expression.class})
    public static class startExpression {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.This PlayerMob This, @Advice.Argument(0) FormExpressionWheel.Expression expression) {
            if (This.isClient()) {
                Client client = ((MainGame) GlobalData.getCurrentState()).getClient();

                if (client.getPlayer() == This) {
                    int skillSlot;
                    if (expression == FormExpressionWheel.Expression.SAD) {
                        skillSlot = 0;
                    } else if (expression == FormExpressionWheel.Expression.SURPRISED) {
                        skillSlot = 1;
                    } else if (expression == FormExpressionWheel.Expression.ANGRY) {
                        skillSlot = 2;
                    } else {
                        skillSlot = 3;
                    }

                    PlayerData playerData = PlayerDataList.getPlayerData(This);
                    EquippedActiveSkill equippedActiveSkill = playerData.equippedActiveSkills[skillSlot];
                    equippedActiveSkill.tryRun(This, skillSlot);
                }
            }

            return true;
        }
    }
}
