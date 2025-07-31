package rpgclasses.containers.rpgmenu;

import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.client.Client;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormBreakLine;
import necesse.gfx.forms.components.FormComponentList;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormTextButton;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.presets.containerComponent.ContainerFormSwitcher;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.shader.FormShader;
import rpgclasses.containers.rpgmenu.components.PlayerDataComponent;
import rpgclasses.containers.rpgmenu.components.TitleComponent;
import rpgclasses.containers.rpgmenu.entries.*;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MenuContainerForm extends ContainerFormSwitcher<MenuContainer> {
    public Client client;

    private final Form principalForm;

    public static final MenuEntry[] menuEntries = new MenuEntry[]{
            new AttributesEntry(),
            new ActiveSkillsEntry(),
            new ClassesEntry()
    };

    public static MenuEntry actualEntry = menuEntries[0];
    public FormContentBox entries;
    public FormContentBox entryForm;
    PlayerDataComponent playerDataComponent;
    TitleComponent titleComponent;

    public static MenuContainerForm mainForm;

    public MenuContainerForm(Client client, final MenuContainer container) {
        super(client, container);
        mainForm = this;

        this.client = client;

        FormComponentList formComponents = this.addComponent(new FormComponentList());
        this.principalForm = formComponents.addComponent(new Form(925, 500));

        entries = this.principalForm.addComponent(new FormContentBox(0, 40, 190, 340));

        FormBreakLine playerLevelBreakLike = this.principalForm.addComponent(new FormBreakLine(FormBreakLine.ALIGN_BEGINNING, 10, 355, 170, true));
        playerLevelBreakLike.color = Settings.UI.activeTextColor;

        FormBreakLine middleVerticalLine = this.principalForm.addComponent(new FormBreakLine(FormBreakLine.ALIGN_BEGINNING, entries.getWidth() + 10, 10, this.principalForm.getHeight() - 55, false));
        middleVerticalLine.color = Settings.UI.activeTextColor;

        playerDataComponent = this.principalForm.addComponent(new PlayerDataComponent(12, 360, 250 - 24, 100));

        titleComponent = this.principalForm.addComponent(new TitleComponent(0, 10, 190, 30));

        entryForm = this.principalForm.addComponent(new FormContentBox(entries.getWidth() + 20, 10, 925 - entries.getWidth() - 20 - 4, this.principalForm.getHeight() - 60));

        this.principalForm.addComponent(new FormLocalTextButton("ui", "closebutton", 4, this.principalForm.getHeight() - 40, this.principalForm.getWidth() - 8)).onClicked((e) -> client.closeContainer(true));
        this.makeCurrent(formComponents);

        PlayerMob player = client.getPlayer();

        updateEntries(container, player, null);
        updateContent(container, player);

        playerDataComponent.updateContent(player);
        titleComponent.updateContent();
    }

    public void updateEntries(final MenuContainer container, PlayerMob player, int[] classLevels) {
        entries.clearComponents();
        PlayerData playerData = PlayerDataList.getPlayerData(player);

        ArrayList<MenuEntry> showEntries = Arrays.stream(menuEntries).collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < playerData.getClassLevels().length; i++) {
            if ((classLevels == null ? playerData.getClassLevel(i) : classLevels[i]) > 0) {
                PlayerClass playerClass = PlayerClass.classesList.get(i);
                showEntries.add(new ClassEntry("classes." + playerClass.stringID, playerClass));
            }
        }

        for (int i = 0; i < showEntries.size(); i++) {
            MenuEntry menuEntry = showEntries.get(i);
            entries.addComponent((new FormTextButton(menuEntry.getDisplayName(), 6, 6 + i * (30 + 6), 190 - 12) {
                @Override
                public void draw(TickManager tickManager, PlayerMob perspective, Rectangle renderBox) {
                    Rectangle contentRect = this.size.getContentRectangle(this.getWidth());
                    FormShader.FormShaderState textState = GameResources.formShader.startState(new Point(this.getX(), this.getY()), new Rectangle(contentRect.x, contentRect.y, contentRect.width, contentRect.height));

                    try {
                        FontOptions fontOptions = new FontOptions(16).color(this.getTextColor());

                        Color textColor = menuEntry.getTextColor(player);
                        if (textColor != null) fontOptions.color(textColor);

                        if (!Objects.equals(actualEntry.name, menuEntry.name)) {
                            if (this.isHovering()) {
                                fontOptions.alpha(204);
                            } else {
                                fontOptions.alpha(153);
                            }
                        }

                        String drawText = this.getDrawText();
                        int drawX = this.getWidth() / 2 - FontManager.bit.getWidthCeil(drawText, fontOptions) / 2;
                        FontManager.bit.drawString((float) drawX, (float) (this.size.fontDrawOffset), drawText, fontOptions);

                        GameTexture gameTexture = menuEntry.getTexture();
                        if (gameTexture != null) {
                            gameTexture.initDraw().size(17).draw(5, 9);
                        }
                    } finally {
                        textState.end();
                    }

                    if (this.isHovering()) {
                        this.addTooltips(perspective);
                    }
                }
            }).onClicked((e) -> {
                actualEntry = menuEntry;
                updateContent(container, player);
            }));
        }

        entries.setContentBox(new Rectangle(0, 0, entries.getWidth(), 12 + showEntries.size() * 36));
    }

    public void updateContent(final MenuContainer container, PlayerMob player) {
        entryForm.clearComponents();

        actualEntry.client = client;
        actualEntry.player = player;
        actualEntry.updateContent(this, entryForm, container);
    }

    public void onWindowResized(GameWindow window) {
        super.onWindowResized(window);
        this.principalForm.setPosMiddle(window.getHudWidth() / 2, window.getHudHeight() / 2);
    }

    public boolean shouldOpenInventory() {
        return false;
    }

    public boolean shouldShowToolbar() {
        return false;
    }

}
