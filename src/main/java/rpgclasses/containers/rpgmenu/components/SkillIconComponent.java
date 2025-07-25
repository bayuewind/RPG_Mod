package rpgclasses.containers.rpgmenu.components;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.InputEvent;
import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.position.FormFixedPosition;
import necesse.gfx.forms.position.FormPosition;
import necesse.gfx.forms.position.FormPositionContainer;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.SpriteTooltip;
import necesse.gfx.gameTooltips.TooltipLocation;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsAndAttributes.Skill;
import rpgclasses.containers.rpgmenu.BorderFormGameBackground;

import java.awt.*;
import java.util.List;

public class SkillIconComponent extends FormButton implements FormPositionContainer {
    private FormPosition position;

    final Skill skill;
    private int skillLevel;
    final int width;
    final int height;
    boolean showLevelVersion;

    public SkillIconComponent(Skill skill, int skillLevel, int x, int y, int width, int height) {
        this.position = new FormFixedPosition(x, y);
        this.skill = skill;
        this.skillLevel = skillLevel;
        this.width = width;
        this.height = height;
        this.showLevelVersion = true;

        this.onClicked(c -> {
            if (getSkillLevel() > 0) this.showLevelVersion = !this.showLevelVersion;
        });
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob playerMob, Rectangle rectangle) {
        int textureWidth = skill.texture.getWidth();
        int textureHeight = skill.texture.getWidth();

        int drawX = this.getX() + (this.width - textureWidth) / 2;
        int drawY = this.getY() + (this.height - textureHeight) / 2;

        skill.texture.initDraw().draw(drawX, drawY);

        for (int i = Math.max(0, skillLevel - 5); i < skillLevel && i < stars.length; i++) {
            stars[i].draw(drawX, drawY);
        }

        if (isHovering()) {
            ListGameTooltips tooltips;
            if (skill.containsComplexTooltips() && skillLevel > 0) {
                if (showLevelVersion) {
                    tooltips = skill.getFinalToolTips(playerMob, skillLevel);
                    tooltips.add(" ");
                    tooltips.add(Localization.translate("ui", "clicktoseebase"));
                } else {
                    tooltips = skill.getToolTips();
                    tooltips.add(" ");
                    tooltips.add(Localization.translate("ui", "clicktoseelevel", "level", skillLevel));
                }
            } else {
                tooltips = skill.getToolTips();
            }
            GameTooltipManager.addTooltip(tooltips, new BorderFormGameBackground(12), TooltipLocation.FORM_FOCUS);

            String[] extraTooltipsString = skill.getExtraTooltips();
            for (String extraTooltip : extraTooltipsString) {
                GameTooltipManager.addTooltip(new ListGameTooltips(Localization.translate("extraskilldesc", extraTooltip)), new BorderFormGameBackground(12), TooltipLocation.FORM_FOCUS);
            }

            GameTooltipManager.addTooltip(new SpriteTooltip(skill.texture), new BorderFormGameBackground(4), TooltipLocation.FORM_FOCUS);

        }
    }

    public static void drawStar(int drawX, int drawY, int iconDrawX, int iconDrawY) {
        RPGResources.UI_TEXTURES.star_texture.initDraw().draw(drawX + iconDrawX, drawY + iconDrawY);
    }

    @Override
    public List<Rectangle> getHitboxes() {
        return singleBox(new Rectangle(this.getX(), this.getY(), this.width, this.height));
    }

    @Override
    public void handleInputEvent(InputEvent event, TickManager tickManager, PlayerMob perspective) {
        super.handleInputEvent(event, tickManager, perspective);
    }

    public FormPosition getPosition() {
        return this.position;
    }

    public void setPosition(FormPosition position) {
        this.position = position;
    }

    public static Star[] stars = {
            new Star(0, 26, false),   // 1
            new Star(6, 18, false),   // 2
            new Star(12, 26, false),  // 3
            new Star(18, 18, false),  // 4
            new Star(24, 26, false),  // 5

            new Star(0, 26, true),    // 6
            new Star(6, 18, true),    // 7
            new Star(12, 26, true),   // 8
            new Star(18, 18, true),   // 9
            new Star(24, 26, true)    // 10
    };

    public static class Star {
        int x, y;
        boolean upgraded;

        public Star(int x, int y, boolean upgraded) {
            this.x = x;
            this.y = y;
            this.upgraded = upgraded;
        }

        public void draw(int iconDrawX, int iconDrawY) {
            (upgraded ? RPGResources.UI_TEXTURES.star2_texture : RPGResources.UI_TEXTURES.star_texture).initDraw().draw(x + iconDrawX, y + iconDrawY);
        }
    }

}
