package rpgclasses.containers.rpgmenu.components;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class EquipActiveSkillComponent extends FormContentBox {
    public static int height = 54;
    public static int width = 300;

    private final FormContentIconButton[] buttons = new FormContentIconButton[4];
    public final PlayerClass playerClass;
    public final ActiveSkill activeSkill;

    public EquipActiveSkillComponent(int x, int y, ActiveSkill activeSkill, PlayerClass playerClass, PlayerMob player, EquippedActiveSkill[] newEquippedActiveSkills, int skillLevel, Runnable onClick) {
        super(x, y, width, height);

        PlayerData playerData = PlayerDataList.getPlayerData(player);

        this.playerClass = playerClass;
        this.activeSkill = activeSkill;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);

        this.addComponent(new SkillIconComponent(activeSkill, skillLevel, 0, 0, 34, 54));

        this.addComponent(new FormLocalLabel(getSkillText(activeSkill, skillLevel), new FontOptions(16), -1, 34 + 10, 0));

        for (int i = 0; i < 4; i++) {
            int finalI = i;
            buttons[i] = (FormContentIconButton) this.addComponent(new FormContentIconButton(34 + 10 + (32 + 4) * i, 16 + 6, FormInputSize.SIZE_32, ButtonColor.BASE, RPGResources.UI_TEXTURES.slot_icons[style][i])
                    .onClicked(c -> {
                        // Only if it has not equipped another skill with the same family, and it's not the same
                        boolean sameFamily = false;
                        for (int j = 0; j < 4; j++) {
                            if (finalI != j && newEquippedActiveSkills[j].isNotSameSkillButSameFamily(playerClass, activeSkill)) {
                                sameFamily = true;
                                break;
                            }
                        }
                        if (sameFamily) {
                            return;
                        }

                        EquippedActiveSkill oldEquippedActiveSkill = newEquippedActiveSkills[finalI];

                        int activeSkillLevel = playerData.getClassesData()[playerClass.id].getActiveSkillLevels()[activeSkill.id];

                        // Only if the player is not in combat nor the skill in cooldown
                        if (!player.isInCombat() && (oldEquippedActiveSkill.isEmpty() || !oldEquippedActiveSkill.isInCooldown(activeSkillLevel, player.getTime()))) {
                            if (oldEquippedActiveSkill.isSameSkill(playerClass, activeSkill)) {
                                newEquippedActiveSkills[finalI].empty();
                            } else {
                                newEquippedActiveSkills[finalI].playerClass = playerClass;
                                newEquippedActiveSkills[finalI].activeSkill = activeSkill;

                                // If this skill is in cooldown in any other slot, then apply that lastUse to this slot if higher and remove the last one
                                long maxLastUse = 0;
                                for (int j = 0; j < 4; j++) {
                                    EquippedActiveSkill equippedActiveSkill2 = newEquippedActiveSkills[j];
                                    if (finalI != j && !equippedActiveSkill2.isEmpty() && equippedActiveSkill2.isSameSkill(newEquippedActiveSkills[finalI])) {
                                        maxLastUse = Math.max(maxLastUse, equippedActiveSkill2.lastUse);
                                        newEquippedActiveSkills[j].empty();
                                    }
                                }
                                newEquippedActiveSkills[finalI].lastUse = maxLastUse;
                            }

                            onClick.run();
                        }
                    }));
        }

        update(newEquippedActiveSkills);
    }

    public void update(EquippedActiveSkill[] newEquippedActiveSkills) {
        for (int i = 0; i < newEquippedActiveSkills.length; i++) {
            boolean sameFamily = false;
            boolean sameSkill = false;
            for (int j = 0; j < 4; j++) {
                if (i != j && newEquippedActiveSkills[j] != null) {
                    if (newEquippedActiveSkills[j].isSameSkill(playerClass, activeSkill)) {
                        sameSkill = true;
                        break;
                    } else if (newEquippedActiveSkills[j].isSameFamily(playerClass, activeSkill)) {
                        sameFamily = true;
                        break;
                    }
                }
            }
            EquippedActiveSkill equippedActiveSkill = newEquippedActiveSkills[i];
            if (!equippedActiveSkill.isEmpty() && equippedActiveSkill.isSameSkill(playerClass, activeSkill)) {
                buttons[i].color = ButtonColor.GREEN;
            } else if (sameSkill) {
                buttons[i].color = ButtonColor.BASE;
            } else if (sameFamily) {
                buttons[i].color = ButtonColor.RED;
            } else if (equippedActiveSkill.isEmpty()) {
                buttons[i].color = ButtonColor.YELLOW;
            } else {
                buttons[i].color = ButtonColor.BASE;
            }
        }
    }

    public GameMessage getSkillText(ActiveSkill activeSkill, int skillLevel) {
        return new StaticMessage(Localization.translate("activeskills", activeSkill.stringID) + " - " + Localization.translate("ui", "lvl", "level", skillLevel));
    }
}
