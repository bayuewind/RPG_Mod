package rpgclasses.data;


import necesse.entity.mobs.PlayerMob;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataList {
    public static Map<String, PlayerData> playersClient = new HashMap<>();
    public static Map<String, PlayerData> playersServer = new HashMap<>();

    public static PlayerData getPlayerData(PlayerMob player) {
        return getPlayerData(player.playerName, player.isServer());
    }

    public static PlayerData getPlayerData(String playerName, boolean isServer) {
        PlayerData playerData = (isServer ? playersServer : playersClient).get(playerName);
        if (playerData == null) {
            playerData = initPlayerData(playerName, isServer);
        }
        return playerData;
    }

    public static void setPlayerData(String playerName, PlayerData playerData, boolean isServer) {
        (isServer ? playersServer : playersClient).put(playerName, playerData);
    }

    public static PlayerData initPlayerData(String playerName, boolean isServer) {
        PlayerData playerData = new PlayerData(playerName);
        (isServer ? playersServer : playersClient).put(playerName, playerData);
        return playerData;
    }

}
