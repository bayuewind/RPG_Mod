package rpgclasses.data;


import necesse.entity.mobs.PlayerMob;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataList {
    public static Map<String, PlayerData> playersClient = new HashMap<>();
    public static Map<String, PlayerData> playersServer = new HashMap<>();

    public static PlayerData getPlayerData(PlayerMob player) {
        return getPlayerData(player.getUniqueID(), player.playerName, player.isServer());
    }

    public static PlayerData getPlayerData(int uniqueID, String playerName, boolean isServer) {
        PlayerData playerData = (isServer ? playersServer : playersClient).get(uniqueID + "-" + playerName);
        if (playerData == null) {
            return null;
        }
        return playerData;
    }

    public static void setPlayerData(int uniqueID, String playerName, PlayerData playerData, boolean isServer) {
        (isServer ? playersServer : playersClient).put(uniqueID + "-" + playerName, playerData);
    }

    public static PlayerData initPlayerData(PlayerMob player) {
        return initPlayerData(player.getUniqueID(), player.playerName, player.getServer().world.getUniqueID(), player.isServer());
    }

    public static PlayerData initPlayerData(int uniqueID, String playerName, long worldUniqueID, boolean isServer) {
        PlayerData playerData = new PlayerData(uniqueID, playerName, worldUniqueID);
        (isServer ? playersServer : playersClient).put(uniqueID + "-" + playerName, playerData);
        return playerData;
    }

}
