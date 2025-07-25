package rpgclasses.containers.rpgmenu.entries;

import necesse.engine.localization.Localization;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.data.PlayerDataList;
import rpgclasses.containers.rpgmenu.MenuContainer;
import rpgclasses.containers.rpgmenu.MenuContainerForm;
import rpgclasses.containers.rpgmenu.components.ClassComponent;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassesEntry extends MenuEntry {

    public ClassesEntry() {
        super("classes");
    }

    @Override
    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container) {
        playerData = PlayerDataList.getPlayerData(player);
        updateContent(mainForm, entryForm, container, playerData.getResets(), playerData.getClassLevels(), playerData.getClassLevels().clone());
    }

    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container, final int resetPoints, final int[] classes, int[] mutableClasses) {
        super.updateContent(mainForm, entryForm, container);

        int numItems = PlayerClass.classesList.size();

        int maxClasses = playerData.totalClassPoints();

        int classesTotal = Arrays.stream(classes).sum();
        AtomicInteger mutableClassesTotal = new AtomicInteger(Arrays.stream(mutableClasses).sum());

        FormLabel classPointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 20
        ));
        updateClassPoints(classPointsLabel, mutableClassesTotal.get(), 0);

        FormLabel resetPointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 38
        ));
        updateResetPoints(resetPointsLabel, classes, mutableClasses, resetPoints);


        FormButton cancelButton = entryForm.addComponent(new FormLocalTextButton("ui", "cancelbutton", 0, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> this.updateContent(mainForm, entryForm, container, resetPoints, classes, classes.clone())));
        cancelButton.setActive(false);

        FormButton confirmButton = entryForm.addComponent(new FormLocalTextButton("ui", "confirmbutton", entryForm.getWidth() / 2 + 2, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> {
                    if (!player.isInCombat()) {
                        int reducedClasses = 0;
                        for (int i = 0; i < classes.length; i++) {
                            int difference = classes[i] - mutableClasses[i];
                            if (difference > 0) reducedClasses += difference;
                        }

                        this.updateContent(mainForm, entryForm, container, resetPoints - reducedClasses * 2, mutableClasses, mutableClasses.clone());
                        mainForm.updateEntries(container, player, mutableClasses);
                    }
                    container.updateClasses.runAndSend(mutableClasses);
                }));
        confirmButton.setActive(false);

        int maxPerRow = 5;
        int numRows = (numItems + maxPerRow - 1) / maxPerRow;

        for (int i = 0; i < numItems; i++) {
            int row = i / maxPerRow;
            int col = i % maxPerRow;

            int min = Math.min(maxPerRow, numItems - row * maxPerRow);
            int totalComponentsWidth = (min - 1) * ClassComponent.width;
            int spacing = (entryForm.getWidth() - totalComponentsWidth) / (min + 1);

            int x = spacing + col * (ClassComponent.width + spacing);
            int y = (entryForm.getHeight() / (numRows + 1)) * (row + 1);

            int finalI = i;
            entryForm.addComponent(new ClassComponent(x, y, PlayerClass.classesList.get(i), classes[i],
                    c -> {
                        int currentClassesTotal = mutableClassesTotal.incrementAndGet();
                        mutableClasses[finalI]++;
                        updateFormClasses(classPointsLabel, resetPointsLabel, cancelButton, confirmButton, classesTotal, currentClassesTotal, maxClasses, resetPoints, classes, mutableClasses);
                    },
                    c -> {
                        int currentClassesTotal = mutableClassesTotal.decrementAndGet();
                        mutableClasses[finalI]--;
                        updateFormClasses(classPointsLabel, resetPointsLabel, cancelButton, confirmButton, classesTotal, currentClassesTotal, maxClasses, resetPoints, classes, mutableClasses);
                    }
            ));
        }
    }

    public void updateFormClasses(FormLabel classPointsLabel, FormLabel resetPointsLabel, FormButton cancelButton, FormButton confirmButton, int classesTotal, int currentClassesTotal, int maxClasses, final int resetPoints, final int[] classes, int[] mutableClasses) {
        int pointsDifference = currentClassesTotal - classesTotal;

        updateClassPoints(classPointsLabel, currentClassesTotal, pointsDifference);
        updateResetPoints(resetPointsLabel, classes, mutableClasses, resetPoints);

        cancelButton.setActive(cancelEnabled(classes, mutableClasses));
        confirmButton.setActive(confirmEnabled(maxClasses, currentClassesTotal, pointsDifference, resetPoints, classes, mutableClasses));
    }

    public void updateClassPoints(FormLabel classPointsLabel, int currentClassesTotal, int pointsDifference) {
        int totalPoints = playerData.totalClassPoints();
        int unassignedPoints = totalPoints - currentClassesTotal;

        String text = unassignedPoints + "/" + totalPoints;

        if (-pointsDifference != 0) {
            text = text + " (" + (-pointsDifference > 0 ? "+" + -pointsDifference : String.valueOf(-pointsDifference)) + ")";
        }

        classPointsLabel.setText(Localization.translate("ui", "classpoints", "points", text));
    }

    public void updateResetPoints(FormLabel resetPointsLabel, final int[] classes, int[] mutableClasses, final int resetPoints) {
        int reducedClasses = 0;
        for (int i = 0; i < classes.length; i++) {
            int difference = classes[i] - mutableClasses[i];
            if (difference > 0) reducedClasses += difference;
        }

        String text = String.valueOf(resetPoints);

        if (reducedClasses > 0) {
            text = text + " (" + -reducedClasses * 2 + ")";
        }

        resetPointsLabel.setText(Localization.translate("ui", "resetpoints", "points", text));
    }


    public boolean cancelEnabled(final int[] classes, int[] mutableClasses) {
        return !Arrays.equals(classes, mutableClasses);
    }

    public boolean confirmEnabled(int maxClasses, int mutableClassesTotal, int pointsDifference, int resetPoints, final int[] classes, int[] mutableClasses) {
        return !Arrays.equals(classes, mutableClasses) && mutableClassesTotal <= maxClasses && (pointsDifference >= 0 || Math.abs(pointsDifference * 2) <= resetPoints);
    }

}
