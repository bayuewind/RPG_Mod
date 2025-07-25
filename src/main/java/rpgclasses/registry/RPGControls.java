package rpgclasses.registry;

import necesse.engine.GlobalData;
import necesse.engine.input.Control;
import necesse.engine.input.InputEvent;
import necesse.engine.input.InputID;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.containers.rpgmenu.RPGMenuPacket;

public class RPGControls {

    public static void registerCore() {

        Control.addModControl(new Control(InputID.KEY_K, "openrpgmenu") {
            @Override
            public void activate(InputEvent event) {
                super.activate(event);
                if (isPressed() && GlobalData.getCurrentState() instanceof MainGame) {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    client.network.sendPacket(new RPGMenuPacket());
                }
            }
        });

        Control.addModControl(new Control(-97, "activeskillslot1") {
            @Override
            public void activate(InputEvent event) {
                super.activate(event);
                if (isPressed() && GlobalData.getCurrentState() instanceof MainGame) {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    PlayerData playerData = PlayerDataList.getPlayerData(client.getPlayer());
                    playerData.equippedActiveSkills[0].tryRun(client.getPlayer(), 0);
                }
            }
        });
        Control.addModControl(new Control(-96, "activeskillslot2") {
            @Override
            public void activate(InputEvent event) {
                super.activate(event);
                if (isPressed() && GlobalData.getCurrentState() instanceof MainGame) {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    PlayerData playerData = PlayerDataList.getPlayerData(client.getPlayer());
                    playerData.equippedActiveSkills[1].tryRun(client.getPlayer(), 1);
                }
            }
        });
        Control.addModControl(new Control(-1, "activeskillslot3") {
            @Override
            public void activate(InputEvent event) {
                super.activate(event);
                if (isPressed() && GlobalData.getCurrentState() instanceof MainGame) {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    PlayerData playerData = PlayerDataList.getPlayerData(client.getPlayer());
                    playerData.equippedActiveSkills[2].tryRun(client.getPlayer(), 2);
                }
            }
        });
        Control.addModControl(new Control(-1, "activeskillslot4") {
            @Override
            public void activate(InputEvent event) {
                super.activate(event);
                if (isPressed() && GlobalData.getCurrentState() instanceof MainGame) {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    PlayerData playerData = PlayerDataList.getPlayerData(client.getPlayer());
                    playerData.equippedActiveSkills[3].tryRun(client.getPlayer(), 3);
                }
            }
        });

        if (Control.EXPRESSION_WHEEL.getKey() == -1) {
            Control.EXPRESSION_WHEEL.changeKey(Control.EXPRESSION_WHEEL.defaultKey);
        }

    }

}
