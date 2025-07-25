package rpgclasses.containers.rpgmenu.entries;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.containers.rpgmenu.MenuContainer;
import rpgclasses.containers.rpgmenu.MenuContainerForm;
import rpgclasses.containers.rpgmenu.components.SkillComponent;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassEntry extends MenuEntry {
    final PlayerClass playerClass;

    public ClassEntry(String name, PlayerClass playerClass) {
        super(name);
        this.playerClass = playerClass;
    }

    @Override
    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container) {
        playerData = PlayerDataList.getPlayerData(player);
        PlayerClassData playerClassData = playerData.getClassesData()[playerClass.id];
        updateContent(mainForm, entryForm, container, playerClassData, playerClassData.getPassiveLevels(), playerClassData.getPassiveLevels().clone(), playerClassData.getActiveSkillLevels(), playerClassData.getActiveSkillLevels().clone());
    }

    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container, PlayerClassData playerClassData, final int[] passiveLevels, int[] mutablePassiveLevels, final int[] activeSkillLevels, int[] mutableActiveSkillLevels) {
        super.updateContent(mainForm, entryForm, container);

        int totalPassivePoints = playerClassData.totalPassivePoints(false);
        int usedPassivePoints = Arrays.stream(passiveLevels).sum();
        AtomicInteger mutableUsedPassivePoints = new AtomicInteger(Arrays.stream(mutablePassiveLevels).sum());

        entryForm.addComponent(new FormLocalLabel(
                new LocalMessage("ui", "classlevel", "class", Localization.translate("classes", playerClassData.playerClass.stringID), "level", playerClassData.getLevel(false)),
                new FontOptions(16), -1, 10, 4
        ));

        FormLabel passivePointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(12), -1, 12, 25
        ));
        updatePassivePoints(passivePointsLabel, totalPassivePoints, mutableUsedPassivePoints.get(), 0);

        int totalActiveSkillPoints = playerClassData.totalActiveSkillPoints(false);
        int usedActiveSkillsPoints = Arrays.stream(activeSkillLevels).sum();
        AtomicInteger mutableUsedActiveSkillsPoints = new AtomicInteger(Arrays.stream(mutableActiveSkillLevels).sum());

        FormLabel activeSkillPointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(12), -1, 12, 43
        ));
        updateActiveSkillPoints(activeSkillPointsLabel, totalActiveSkillPoints, mutableUsedActiveSkillsPoints.get(), 0);


        FormButton cancelButton = entryForm.addComponent(new FormLocalTextButton("ui", "cancelbutton", 0, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> this.updateContent(mainForm, entryForm, container, playerClassData, passiveLevels, passiveLevels.clone(), activeSkillLevels, activeSkillLevels.clone())));
        cancelButton.setActive(false);

        FormButton confirmButton = entryForm.addComponent(new FormLocalTextButton("ui", "confirmbutton", entryForm.getWidth() / 2 + 2, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> {
                    if (!player.isInCombat()) {
                        this.updateContent(mainForm, entryForm, container, playerClassData, mutablePassiveLevels, mutablePassiveLevels.clone(), mutableActiveSkillLevels, mutableActiveSkillLevels.clone());
                    }
                    container.updateClass.runAndSend(playerClass.id, mutablePassiveLevels, mutableActiveSkillLevels);
                }));
        confirmButton.setActive(false);

        // PASSIVES

        int maxPassiveColumns = 4;

        int numPassiveItems = playerClass.passivesList.size();

        int passivesContainerWidth = 24 + maxPassiveColumns * (SkillComponent.width + 12);
        int startPassivesContainerX = entryForm.getWidth() - passivesContainerWidth;

        entryForm.addComponent(new FormLocalLabel("passives", "passives", new FontOptions(18), -1, startPassivesContainerX + 16, 6));

        int passiveRows = 0;
        int actualColumn = 0;
        int lastPassiveLevel = 0;
        int passiveSeparations = 0;

        FormContentBox passives = entryForm.addComponent(new FormContentBox(startPassivesContainerX, 30, passivesContainerWidth, entryForm.getHeight() - 32 - 30 - 4));
        for (int i = 0; i < numPassiveItems; i++) {
            Passive passive = playerClass.passivesList.get(i);

            boolean newSeparation = false;
            if (i != 0) {
                if (passive.requiredClassLevel != lastPassiveLevel) {
                    passiveRows++;
                    actualColumn = 0;
                    lastPassiveLevel = passive.requiredClassLevel;
                    newSeparation = true;
                } else if (maxPassiveColumns > actualColumn) {
                    actualColumn++;
                } else {
                    passiveRows++;
                    actualColumn = 0;
                }
            }
            int yPosition = 12 + passiveRows * (SkillComponent.height + 12);

            if (newSeparation && lastPassiveLevel != 0) {
                passives.addComponent(new FormLocalLabel(new LocalMessage("ui", "classlevelseparation", "level", lastPassiveLevel), new FontOptions(12), -1, 8, yPosition + passiveSeparations * 24 + 8));
                passiveSeparations++;
            }

            int finalI = i;
            SkillComponent passiveSkillComponent = passives.addComponent(new SkillComponent(12 + actualColumn * (SkillComponent.width + 12), yPosition + passiveSeparations * 24, playerClass.passivesList.get(i), player, playerClassData, passiveLevels[i], mutablePassiveLevels));
            passiveSkillComponent.setOnAdd(
                    c -> {
                        int currentUsedPassives = mutableUsedPassivePoints.incrementAndGet();
                        mutablePassiveLevels[finalI]++;
                        passiveSkillComponent.addSkillLevel(1);

                        updateForm(
                                passivePointsLabel, activeSkillPointsLabel, cancelButton, confirmButton,
                                usedPassivePoints, currentUsedPassives, totalPassivePoints, passiveLevels, mutablePassiveLevels,
                                usedActiveSkillsPoints, mutableUsedActiveSkillsPoints.get(), totalActiveSkillPoints, activeSkillLevels, mutableActiveSkillLevels
                        );
                    }
            );
            passiveSkillComponent.setOnRemove(
                    c -> {
                        int currentUsedPassives = mutableUsedPassivePoints.decrementAndGet();
                        mutablePassiveLevels[finalI]--;
                        passiveSkillComponent.addSkillLevel(-1);

                        updateForm(
                                passivePointsLabel, activeSkillPointsLabel, cancelButton, confirmButton,
                                usedPassivePoints, currentUsedPassives, totalPassivePoints, passiveLevels, mutablePassiveLevels,
                                usedActiveSkillsPoints, mutableUsedActiveSkillsPoints.get(), totalActiveSkillPoints, activeSkillLevels, mutableActiveSkillLevels
                        );
                    }
            );
        }

        passives.setContentBox(new Rectangle(0, 0, passives.getWidth(), 12 + (passiveRows + 1) * (SkillComponent.height + 12) + passiveSeparations * 24));


        // ACTIVE SKILLS

        int numActiveItems = playerClass.activeSkillsList.size();
        int activeRows = 1;
        int activeColumns = 1;
        actualColumn = 0;
        int lastActiveLevel = 0;

        int activeSkillsStartY = 56 + 4;

        int activeSeparations = 0;

        FormContentBox activeSkills = entryForm.addComponent(new FormContentBox(0, activeSkillsStartY, entryForm.getWidth() - 16 - 4 - passivesContainerWidth, entryForm.getHeight() - 32 - 4 - activeSkillsStartY));
        for (int i = 0; i < numActiveItems; i++) {
            ActiveSkill activeSkill = playerClass.activeSkillsList.get(i);

            int requiredLevel = activeSkill.requiredClassLevel;
            if (requiredLevel != 0 && (activeSkill.newRow || lastActiveLevel != requiredLevel)) {
                activeRows++;
                actualColumn = 1;
            } else {
                actualColumn++;
                if (activeColumns < actualColumn) {
                    activeColumns = actualColumn;
                }
            }
            int yPosition = 12 + (activeRows - 1) * (SkillComponent.height + 12);

            if (requiredLevel != 0 && requiredLevel != lastActiveLevel) {
                lastActiveLevel = requiredLevel;
                activeSkills.addComponent(new FormLocalLabel(new LocalMessage("ui", "classlevelseparation", "level", lastActiveLevel), new FontOptions(12), -1, 8, yPosition + activeSeparations * 24 + 8));
                activeSeparations++;
            }

            int finalI = i;
            SkillComponent activeSkillComponent = activeSkills.addComponent(new SkillComponent(12 + (actualColumn - 1) * (SkillComponent.width + 12), yPosition + activeSeparations * 24, activeSkill, player, playerClassData, activeSkillLevels[i], mutableActiveSkillLevels));
            activeSkillComponent.setOnAdd(
                    c -> {
                        int currentUsedActiveSkills = mutableUsedActiveSkillsPoints.incrementAndGet();
                        mutableActiveSkillLevels[finalI]++;
                        activeSkillComponent.addSkillLevel(1);

                        updateForm(
                                passivePointsLabel, activeSkillPointsLabel, cancelButton, confirmButton,
                                usedPassivePoints, mutableUsedPassivePoints.get(), totalPassivePoints, passiveLevels, mutablePassiveLevels,
                                usedActiveSkillsPoints, currentUsedActiveSkills, totalActiveSkillPoints, activeSkillLevels, mutableActiveSkillLevels
                        );
                    }
            );
            activeSkillComponent.setOnRemove(
                    c -> {
                        int currentUsedActiveSkills = mutableUsedActiveSkillsPoints.decrementAndGet();
                        mutableActiveSkillLevels[finalI]--;
                        activeSkillComponent.addSkillLevel(-1);

                        updateForm(
                                passivePointsLabel, activeSkillPointsLabel, cancelButton, confirmButton,
                                usedPassivePoints, mutableUsedPassivePoints.get(), totalPassivePoints, passiveLevels, mutablePassiveLevels,
                                usedActiveSkillsPoints, currentUsedActiveSkills, totalActiveSkillPoints, activeSkillLevels, mutableActiveSkillLevels
                        );
                    }
            );
        }

        activeSkills.setContentBox(new Rectangle(0, 0, 12 + activeColumns * (SkillComponent.width + 12), 12 + activeRows * (SkillComponent.height + 12) + activeSeparations * 24));
    }

    public void updateForm(FormLabel passivePointsLabel, FormLabel activeSkillPointsLabel, FormButton cancelButton, FormButton confirmButton, int passiveLevelsTotal, int currentUsedPassives, int totalPassivePoints, final int[] passiveLevels, int[] mutablePassiveLevels, int activeSkillLevelsTotal, int currentUsedActiveSkill, int totalActiveSkillPoints, final int[] activeSkillLevels, int[] mutableActiveSkillLevels) {
        int passivePointsDifference = currentUsedPassives - passiveLevelsTotal;
        int activeSkillPointsDifference = currentUsedActiveSkill - activeSkillLevelsTotal;

        updatePassivePoints(passivePointsLabel, totalPassivePoints, currentUsedPassives, passivePointsDifference);
        updateActiveSkillPoints(activeSkillPointsLabel, totalActiveSkillPoints, currentUsedActiveSkill, activeSkillPointsDifference);

        cancelButton.setActive(cancelEnabledPassives(passiveLevels, mutablePassiveLevels) || cancelEnabledActiveSkills(activeSkillLevels, mutableActiveSkillLevels));
        confirmButton.setActive(
                confirmEnabledPassives(totalPassivePoints, currentUsedPassives) && confirmEnabledActiveSkills(totalActiveSkillPoints, currentUsedActiveSkill)
                        && (confirmEnabledPassivesOptional(passiveLevels, mutablePassiveLevels) || confirmEnabledActiveSkillsOptional(activeSkillLevels, mutableActiveSkillLevels))
        );
    }

    public void updatePassivePoints(FormLabel passivePointsLabel, int totalPassivePoints, int currentUsedPassives, int pointsDifference) {
        int unassignedPoints = totalPassivePoints - currentUsedPassives;

        String text = unassignedPoints + "/" + totalPassivePoints;

        if (-pointsDifference != 0) {
            text = text + " (" + (-pointsDifference > 0 ? "+" + -pointsDifference : String.valueOf(-pointsDifference)) + ")";
        }

        passivePointsLabel.setText(Localization.translate("ui", "passivepoints", "points", text));
    }

    public void updateActiveSkillPoints(FormLabel activeSkillPointsLabel, int totalActiveSkillPoints, int currentUsedActiveSkills, int pointsDifference) {
        int unassignedPoints = totalActiveSkillPoints - currentUsedActiveSkills;

        String text = unassignedPoints + "/" + totalActiveSkillPoints;

        if (-pointsDifference != 0) {
            text = text + " (" + (-pointsDifference > 0 ? "+" + -pointsDifference : String.valueOf(-pointsDifference)) + ")";
        }

        activeSkillPointsLabel.setText(Localization.translate("ui", "activeskillpoints", "points", text));
    }

    public boolean cancelEnabledPassives(final int[] passiveLevels, int[] mutablePassiveLevels) {
        return !Arrays.equals(passiveLevels, mutablePassiveLevels);
    }

    public boolean confirmEnabledPassivesOptional(final int[] passiveLevels, int[] mutablePassiveLevels) {
        return !Arrays.equals(passiveLevels, mutablePassiveLevels);
    }

    public boolean confirmEnabledPassives(int totalPassivePoints, int mutablePassiveLevelsTotal) {
        return mutablePassiveLevelsTotal <= totalPassivePoints;
    }

    public boolean cancelEnabledActiveSkills(final int[] activeSkillLevels, int[] mutableActiveSkillLevels) {
        return !Arrays.equals(activeSkillLevels, mutableActiveSkillLevels);
    }

    public boolean confirmEnabledActiveSkillsOptional(final int[] activeSkillLevels, int[] mutableActiveSkillLevels) {
        return !Arrays.equals(activeSkillLevels, mutableActiveSkillLevels);
    }

    public boolean confirmEnabledActiveSkills(int totalActiveSkillPoints, int mutableActiveSkillLevelsTotal) {
        return mutableActiveSkillLevelsTotal <= totalActiveSkillPoints;
    }

    @Override
    public Color getTextColor(PlayerMob player) {
        playerData = PlayerDataList.getPlayerData(player);
        PlayerClassData classData = playerData.getClassesData()[playerClass.id];

        if (classData.totalActiveSkillPoints(false) < classData.usedActiveSkillPoints() || classData.totalPassivePoints(false) < classData.usedPassivePoints()) {
            int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
            if (style == 1) {
                return new Color(255, 0, 0);
            } else {
                return new Color(102, 0, 0);
            }
        }

        return null;
    }

    @Override
    public GameTexture getTexture() {
        return playerClass.texture;
    }
}
