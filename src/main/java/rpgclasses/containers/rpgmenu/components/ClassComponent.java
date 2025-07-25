package rpgclasses.containers.rpgmenu.components;

import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.UpcomingPlayerClass;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassComponent extends FormContentBox {
    public static int width = 96;
    public static int height = 116;

    public ClassComponent(int x, int y, PlayerClass playerClass, int currentClassLevel, FormEventListener<FormInputEvent<FormButton>> onAdd, FormEventListener<FormInputEvent<FormButton>> onRemove) {
        super(x - width / 2, y - height / 2, width, height);

        Color sameLevel = Settings.UI.activeTextColor;
        Color differentLevel;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        if (style == 1) {
            differentLevel = new Color(255, 255, 0);
        } else {
            differentLevel = new Color(102, 102, 0);
        }


        AtomicInteger classLevel = new AtomicInteger();
        classLevel.set(currentClassLevel);
        FormLocalLabel levelText = this.addComponent(new FormLocalLabel(getLevelText(classLevel.get()), new FontOptions(16), 0, width / 2, 96 + 4));
        levelText.setColor(sameLevel);

        this.addComponent(new ClassIconComponent(playerClass, 0, 0, width, 68));

        int buttonsHorizontalSpace = ((width / 2) - 24) / 2;
        FormContentIconButton buttonAdd = (FormContentIconButton) this.addComponent(new FormContentIconButton(width / 2 + buttonsHorizontalSpace, 68 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.add_icon[style])
                .onClicked(c -> {
                    if (classLevel.get() < 999 && playerClass.isAvailable()) {
                        int newLevel = classLevel.incrementAndGet();
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentClassLevel ? sameLevel : differentLevel);
                        onAdd.onEvent(c);
                    }
                }));

        FormContentIconButton buttonRemove = (FormContentIconButton) this.addComponent(new FormContentIconButton(buttonsHorizontalSpace, 68 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove_icon[style])
                .onClicked(c -> {
                    if (classLevel.get() > 0) {
                        int newLevel = classLevel.decrementAndGet();
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentClassLevel ? sameLevel : differentLevel);
                        onRemove.onEvent(c);
                    }
                }));

        if (playerClass instanceof UpcomingPlayerClass) {
            buttonAdd.setActive(false);
            buttonRemove.setActive(false);
        }
    }

    public GameMessage getLevelText(int classLevel) {
        return new LocalMessage("ui", "level", "level", classLevel);
    }
}
