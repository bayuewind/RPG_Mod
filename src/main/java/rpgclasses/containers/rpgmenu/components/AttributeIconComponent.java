package rpgclasses.containers.rpgmenu.components;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import rpgclasses.content.player.SkillsAndAttributes.Attribute;
import rpgclasses.containers.rpgmenu.BorderFormGameBackground;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AttributeIconComponent extends FormButton {
    final Attribute attribute;
    final int x;
    final int y;
    final int width;
    final int height;

    public AttributeIconComponent(Attribute attribute, int x, int y, int width, int height) {
        this.attribute = attribute;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob playerMob, Rectangle rectangle) {
        int textureWidth = attribute.texture.getWidth();
        int textureHeight = attribute.texture.getWidth();
        attribute.texture.initDraw().draw(this.x + (this.width - textureWidth) / 2, this.y + (this.height - textureHeight) / 2);
        if (isHovering()) {
            ListGameTooltips tooltips = attribute.getToolTips();
            GameTooltipManager.addTooltip(tooltips, new BorderFormGameBackground(12), TooltipLocation.FORM_FOCUS);
        }
    }

    @Override
    public List<Rectangle> getHitboxes() {
        List<Rectangle> hitBoxes = new ArrayList<>();
        hitBoxes.add(new Rectangle(x, y, width, height));
        return hitBoxes;
    }
}
