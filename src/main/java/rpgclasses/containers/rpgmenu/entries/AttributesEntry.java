package rpgclasses.containers.rpgmenu.entries;

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
import rpgclasses.content.player.SkillsAndAttributes.Attribute;
import rpgclasses.data.PlayerDataList;

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

        int totalComponentsWidth = (numItems - 1) * AttributeComponent.width;
        int spacing = (entryForm.getWidth() - totalComponentsWidth) / (numItems + 1);
        for (int i = 0; i < numItems; i++) {
            int x = spacing + i * (AttributeComponent.width + spacing);
            int finalI = i;
            entryForm.addComponent(new AttributeComponent(client, x, entryForm.getHeight() / 2, Attribute.attributesList.get(i), attributes[i],
                    c -> {
                        int currentAttributesTotal = mutableAttributesTotal.incrementAndGet();
                        mutableAttributes[finalI]++;
                        updateFormAttributes(attributePointsLabel, resetPointsLabel, cancelButton, confirmButton, attributesTotal, currentAttributesTotal, maxAttributes, resetPoints, attributes, mutableAttributes);
                    },
                    c -> {
                        int currentAttributesTotal = mutableAttributesTotal.decrementAndGet();
                        mutableAttributes[finalI]--;
                        updateFormAttributes(attributePointsLabel, resetPointsLabel, cancelButton, confirmButton, attributesTotal, currentAttributesTotal, maxAttributes, resetPoints, attributes, mutableAttributes);
                    }
            ));
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
