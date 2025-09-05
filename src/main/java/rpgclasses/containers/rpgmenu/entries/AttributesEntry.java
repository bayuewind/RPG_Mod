package rpgclasses.containers.rpgmenu.entries;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import rpgclasses.containers.rpgmenu.MenuContainer;
import rpgclasses.containers.rpgmenu.MenuContainerForm;
import rpgclasses.containers.rpgmenu.components.AttributeComponent;
import rpgclasses.containers.rpgmenu.components.ClassComponent;
import rpgclasses.content.player.SkillsAndAttributes.Attribute;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class AttributesEntry extends MenuEntry {

    public AttributesEntry() {
        super("attributes");
    }

    @Override
    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container) {
        playerData = PlayerDataList.getPlayerData(player);
        updateContent(mainForm, entryForm, container, playerData.getResets(), playerData.getAttributeLevels(), playerData.getAttributeLevels().clone());
    }

    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container, final int resetPoints, final int[] attributes, int[] mutableAttributes) {
        super.updateContent(mainForm, entryForm, container);
        int numItems = Attribute.attributesList.size();

        int maxAttributes = playerData.totalAttributePoints();

        int attributesTotal = Arrays.stream(attributes).sum();
        AtomicInteger mutableAttributesTotal = new AtomicInteger(Arrays.stream(mutableAttributes).sum());

        FormLabel attributePointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 20
        ));
        updateAttributePoints(attributePointsLabel, mutableAttributesTotal.get(), 0);

        FormLabel resetPointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 38
        ));
        updateResetPoints(resetPointsLabel, attributes, mutableAttributes, resetPoints);


        FormButton cancelButton = entryForm.addComponent(new FormLocalTextButton("ui", "cancelbutton", 0, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> this.updateContent(mainForm, entryForm, container, resetPoints, attributes, attributes.clone())));
        cancelButton.setActive(false);

        FormButton confirmButton = entryForm.addComponent(new FormLocalTextButton("ui", "confirmbutton", entryForm.getWidth() / 2 + 2, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> {
                    if (!player.isInCombat()) {
                        int reducedAttributes = 0;
                        for (int i = 0; i < attributes.length; i++) {
                            int difference = attributes[i] - mutableAttributes[i];
                            if (difference > 0) reducedAttributes += difference;
                        }
                        this.updateContent(mainForm, entryForm, container, resetPoints - reducedAttributes, mutableAttributes, mutableAttributes.clone());
                    }
                    container.updateAttributes.runAndSend(mutableAttributes);
                }));
        confirmButton.setActive(false);

        FormContentBox attributesForm = entryForm.addComponent(new FormContentBox(8, 52 + 8, entryForm.getWidth() - 16, entryForm.getHeight() - (52 + 8) - (32 + 8)));

        int maxPerRow = 5;
        int numRows = (numItems + maxPerRow - 1) / maxPerRow;

        boolean hasScrollbar = numRows >= 3;

        int contentWidth = attributesForm.getWidth() - (hasScrollbar ? Settings.UI.scrollbar.active.getHeight() + 2 : 0);

        float verticalSpacing;

        if (hasScrollbar) {
            verticalSpacing = ClassComponent.height + 16;
        } else {
            verticalSpacing = (float) 1 / numRows;
        }

        for (int i = 0; i < numItems; i++) {
            int row = i / maxPerRow;
            int col = i % maxPerRow;

            int numCols = Math.min(maxPerRow, numItems - row * maxPerRow);
            float horizontalSpacing = (float) 1 / numCols;

            int x = Math.round(contentWidth * (col + 0.5F) * horizontalSpacing);
            int y = hasScrollbar ? Math.round(verticalSpacing * row + ClassComponent.height / 2F) : Math.round(attributesForm.getHeight() * (row + 0.5F) * verticalSpacing);

            int finalI = i;
            AttributeComponent attributeComponent = attributesForm.addComponent(new AttributeComponent(client, x, y, Attribute.attributesList.get(i), attributes[i]));
            attributeComponent.addOnMod(c -> {
                int newLevel = attributeComponent.attributeLevel.get();
                int oldLevel = mutableAttributes[finalI];
                int mod = newLevel - oldLevel;

                if (mod != 0) {
                    mutableAttributes[finalI] = newLevel;
                    int currentAttributesTotal = mutableAttributesTotal.addAndGet(mod);
                    updateFormAttributes(attributePointsLabel, resetPointsLabel, cancelButton, confirmButton, attributesTotal, currentAttributesTotal, maxAttributes, resetPoints, attributes, mutableAttributes);
                }
            });
        }
        if (hasScrollbar) {
            attributesForm.setContentBox(new Rectangle(0, 0, attributesForm.getWidth(), Math.round(verticalSpacing * numRows)));
        }
    }

    public void updateFormAttributes(FormLabel attributePointsLabel, FormLabel resetPointsLabel, FormButton cancelButton, FormButton confirmButton, int attributesTotal, int currentAttributesTotal, int maxAttributes, final int resetPoints, final int[] attributes, int[] mutableAttributes) {
        int pointsDifference = currentAttributesTotal - attributesTotal;

        updateAttributePoints(attributePointsLabel, currentAttributesTotal, pointsDifference);
        updateResetPoints(resetPointsLabel, attributes, mutableAttributes, resetPoints);

        cancelButton.setActive(cancelEnabled(attributes, mutableAttributes));
        confirmButton.setActive(confirmEnabled(maxAttributes, currentAttributesTotal, pointsDifference, resetPoints, attributes, mutableAttributes));
    }

    public void updateAttributePoints(FormLabel attributePointsLabel, int currentAttributesTotal, int pointsDifference) {
        int totalPoints = playerData.totalAttributePoints();
        int unassignedPoints = totalPoints - currentAttributesTotal;

        String text = unassignedPoints + "/" + totalPoints;

        if (-pointsDifference != 0) {
            text = text + " (" + (-pointsDifference > 0 ? "+" + -pointsDifference : String.valueOf(-pointsDifference)) + ")";
        }

        attributePointsLabel.setText(Localization.translate("ui", "attrpoints", "points", text));
    }

    public void updateResetPoints(FormLabel resetPointsLabel, final int[] attributes, int[] mutableAttributes, final int resetPoints) {
        int reducedAttributes = 0;
        for (int i = 0; i < attributes.length; i++) {
            int difference = attributes[i] - mutableAttributes[i];
            if (difference > 0) reducedAttributes += difference;
        }

        String text = String.valueOf(resetPoints);

        if (reducedAttributes > 0) {
            text = text + " (" + -reducedAttributes + ")";
        }

        resetPointsLabel.setText(Localization.translate("ui", "resetpoints", "points", text));
    }

    public boolean cancelEnabled(final int[] attributes, int[] mutableAttributes) {
        return !Arrays.equals(attributes, mutableAttributes);
    }

    public boolean confirmEnabled(int maxAttributes, int mutableAttributesTotal, int pointsDifference, int resetPoints, final int[] attributes, int[] mutableAttributes) {
        return !Arrays.equals(attributes, mutableAttributes) && mutableAttributesTotal <= maxAttributes && (pointsDifference >= 0 || Math.abs(pointsDifference) <= resetPoints);
    }

}
