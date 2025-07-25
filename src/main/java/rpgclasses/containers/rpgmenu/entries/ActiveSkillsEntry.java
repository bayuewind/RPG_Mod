package rpgclasses.containers.rpgmenu.entries;

import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.gameFont.FontOptions;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerClassData;
import rpgclasses.containers.rpgmenu.MenuContainer;
import rpgclasses.containers.rpgmenu.MenuContainerForm;
import rpgclasses.containers.rpgmenu.components.EquipActiveSkillComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ActiveSkillsEntry extends MenuEntry {
    public ActiveSkillsEntry() {
        super("activeskills");
    }

    @Override
    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container) {
        super.updateContent(mainForm, entryForm, container);

        EquippedActiveSkill[] newEquippedActiveSkills = playerData.equippedActiveSkills.clone();

        entryForm.addComponent(new FormLocalLabel(
                "ui", "activeskillsuse",
                new FontOptions(14), -1, 10, 20
        ));
        entryForm.addComponent(new FormLocalLabel(
                "ui", "activeskillsusetip1",
                new FontOptions(14), -1, 10, 20 + 18
        ));
        entryForm.addComponent(new FormLocalLabel(
                "ui", "activeskillsusetip2",
                new FontOptions(14), -1, 10, 20 + 18 + 18
        ));
        entryForm.addComponent(new FormLocalLabel(
                "ui", "activeskillsequipinstructions",
                new FontOptions(14), -1, 10, 20 + 18 + 18 + 18 + 8
        ));


        List<EquipableActiveSkill> equipableActiveSkills = new ArrayList<>();
        for (PlayerClassData classesDatum : playerData.getClassesData()) {
            for (int i = 0; i < classesDatum.getActiveSkillLevels().length; i++) {
                int activeSkillLevel = classesDatum.getActiveSkillLevels()[i];
                if (classesDatum.getLevel(false) > 0 && activeSkillLevel > 0) {
                    equipableActiveSkills.add(new EquipableActiveSkill(
                            classesDatum.playerClass,
                            classesDatum.playerClass.activeSkillsList.get(i),
                            activeSkillLevel
                    ));
                }
            }
        }

        int startY = 20 + 18 + 18 + 18 + 8 + 18 + 16;
        if (equipableActiveSkills.isEmpty()) {
            entryForm.addComponent(new FormLocalLabel(
                    "ui", "noactiveskills",
                    new FontOptions(14), -1, 10, startY
            ));
        } else {

            FormContentBox activeSkills = entryForm.addComponent(new FormContentBox(16, startY, entryForm.getWidth() - 32, entryForm.getHeight() - startY - 8));

            int columns = 2;
            int spacingY = 8;
            int itemHeight = EquipActiveSkillComponent.height;

            int leftX = 16;
            int rightX = activeSkills.getWidth() / 2;

            for (int i = 0; i < equipableActiveSkills.size(); i++) {
                EquipableActiveSkill equipableActiveSkill = equipableActiveSkills.get(i);
                PlayerClass playerClass = equipableActiveSkill.playerClass;
                ActiveSkill activeSkill = equipableActiveSkill.activeSkill;
                int activeSkillLevel = equipableActiveSkill.activeSkillLevel;

                int column = i % columns;
                int row = i / columns;

                int x = (column == 0) ? leftX : rightX;
                int y = spacingY + row * (itemHeight + spacingY);

                equipableActiveSkill.component = activeSkills.addComponent(
                        new EquipActiveSkillComponent(x, y, activeSkill, playerClass, player, newEquippedActiveSkills, activeSkillLevel, () -> {
                            for (EquipableActiveSkill skill : equipableActiveSkills) {
                                skill.component.update(newEquippedActiveSkills);
                            }
                            container.updateEquippedActiveSkills.runAndSend(newEquippedActiveSkills);
                        })
                );
            }

            int totalRows = (int) Math.ceil(equipableActiveSkills.size() / 2F);
            activeSkills.setContentBox(new Rectangle(0, 0, activeSkills.getWidth(), spacingY + totalRows * (itemHeight + spacingY)));
        }
    }

    public static class EquipableActiveSkill {
        public final PlayerClass playerClass;
        public final ActiveSkill activeSkill;
        public final int activeSkillLevel;
        public EquipActiveSkillComponent component;

        public EquipableActiveSkill(PlayerClass playerClass, ActiveSkill activeSkill, int activeSkillLevel) {
            this.playerClass = playerClass;
            this.activeSkill = activeSkill;
            this.activeSkillLevel = activeSkillLevel;
        }
    }

}
