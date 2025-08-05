package rpgclasses.containers.rpgmenu.components;

import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.components.*;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsAndAttributes.Attribute;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AttributeComponent extends FormContentBox {
    public static int width = 104;
    public static int height = 124;

    public final AtomicInteger attributeLevel;
    public final Color sameLevel;
    public final Color differentLevel;

    public final FormLocalLabel levelText;

    public final int currentAttributeLevel;

    public AttributeComponent(Client client, int x, int y, Attribute attribute, int currentAttributeLevel) {
        super(x - width / 2, y - height / 2, width, height);

        this.currentAttributeLevel = currentAttributeLevel;

        sameLevel = Settings.UI.activeTextColor;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        if (style == 1) {
            differentLevel = new Color(255, 255, 0);
        } else {
            differentLevel = new Color(102, 102, 0);
        }

        attributeLevel = new AtomicInteger();
        attributeLevel.set(currentAttributeLevel);
        levelText = this.addComponent(new FormLocalLabel(getLevelText(attributeLevel.get()), new FontOptions(16), 0, width / 2, 88 + 4));
        levelText.setColor(sameLevel);

        int extraPoints = client.getPlayer().buffManager.getModifier(attribute.ownModifier);
        if (extraPoints > 0) {
            this.addComponent(new FormLabel("(+" + extraPoints + ")", new FontOptions(12), 0, width / 2, 88 + 4 + 16 + 4));
        }

        this.addComponent(new AttributeIconComponent(attribute, 0, 0, width, 60));
    }

    public void addOnMod(FormEventListener<FormInputEvent<FormButton>> onMod) {
        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);

        int center = width / 2;
        this.addComponent(new FormContentIconButton(center + 4, 60 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.add_icon[style])
                .onClicked(c -> {
                    if (attributeLevel.get() < 999) {
                        int newLevel = attributeLevel.incrementAndGet();
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentAttributeLevel ? sameLevel : differentLevel);
                        onMod.onEvent(c);
                    }
                }));

        this.addComponent(new FormContentIconButton(center - 4 - 24, 60 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove_icon[style])
                .onClicked(c -> {
                    if (attributeLevel.get() > 0) {
                        int newLevel = attributeLevel.decrementAndGet();
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentAttributeLevel ? sameLevel : differentLevel);
                        onMod.onEvent(c);
                    }
                }));

        this.addComponent(new FormContentIconButton(center + 4 + 24 + 2, 60 + 4 + 4, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.add10_icon[style])
                .onClicked(c -> {
                    if (attributeLevel.get() < 999) {
                        int mod = Math.min(10, 999 - attributeLevel.get());
                        int newLevel = attributeLevel.addAndGet(mod);
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentAttributeLevel ? sameLevel : differentLevel);
                        onMod.onEvent(c);
                    }
                }));

        this.addComponent(new FormContentIconButton(center - 4 - 24 - 2 - 16, 60 + 4 + 4, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove10_icon[style])
                .onClicked(c -> {
                    if (attributeLevel.get() > 0) {
                        int mod = -Math.min(10, attributeLevel.get());
                        int newLevel = attributeLevel.addAndGet(mod);
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentAttributeLevel ? sameLevel : differentLevel);
                        onMod.onEvent(c);
                    }
                }));
    }

    public GameMessage getLevelText(int attributeLevel) {
        return new LocalMessage("ui", "level", "level", attributeLevel);
    }
}
