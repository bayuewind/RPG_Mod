package rpgclasses.ui;

import necesse.engine.Settings;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.gfx.forms.MainGameFormManager;
import rpgclasses.data.PlayerData;

public class RPGSkillUIManager extends CustomUIManager<RPGSkillUIForm> {
    public final int skillSlot;

    public RPGSkillUIManager(int skillSlot) {
        super();
        this.skillSlot = skillSlot;
    }

    public void setup(MainGameFormManager mainGameFormManager) {
        mainForm = mainGameFormManager.addComponent(new RPGSkillUIForm(skillSlot, 48, 48));
    }

    @Override
    public void frameTick(MainGameFormManager mainGameFormManager) {
        mainForm.setHidden(mainGameFormManager.toolbar.isHidden());
    }

    public void updatePosition(MainGameFormManager mainGameFormManager) {
        GameWindow window = WindowManager.getWindow();
        mainForm.setPosition(
                window.getWidth() - mainForm.getWidth() - (Settings.UI.formSpacing + 20),
                window.getHeight() - mainForm.getHeight() - (Settings.UI.formSpacing + 20) - (3 - skillSlot) * (48 + 11)
        );
    }

    public static void updateContent(PlayerData playerData) {
        for (RPGSkillUIManager rpgSkill : CustomUIManager.rpgSkills) {
            rpgSkill.mainForm.updateContent(playerData);
        }
    }

    public static void updateLevels(PlayerData playerData) {
        for (RPGSkillUIManager rpgSkill : CustomUIManager.rpgSkills) {
            rpgSkill.mainForm.updateLevel(playerData);
        }
    }
}
