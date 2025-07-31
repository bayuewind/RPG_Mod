package rpgclasses.content.player.SkillsAndAttributes;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import org.jetbrains.annotations.NotNull;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

abstract public class Skill {
    public int id;
    public final String stringID;
    public final String color;
    public final int levelMax;
    public final int requiredClassLevel;
    public GameTexture texture;
    public PlayerClass playerClass;

    public String family = null;

    public Skill(String stringID, String color, int levelMax, int requiredClassLevel) {
        this.stringID = stringID;
        this.color = color;
        this.levelMax = levelMax;
        this.requiredClassLevel = requiredClassLevel;
    }

    public static String[] changes = new String[]{"skilllevel", "playerlevel", "endurance", "speed", "strength", "intelligence", "grace"};

    public ListGameTooltips getToolTips() {
        ListGameTooltips tooltips = new ListGameTooltips();
        for (String string : getToolTipsText()) {
            if (string.contains("<") && string.contains(">")) {
                for (String change : changes) {
                    string = string.replaceAll("<" + change + ">", Localization.translate("skillsdesckeys", change));
                }
            }
            tooltips.add(string);
        }
        return tooltips;
    }

    public Skill setFamily(String family) {
        this.family = family;
        return this;
    }

    abstract public List<String> getToolTipsText();

    abstract public void initResources();

    public int getLevel(PlayerClassData classData) {
        return classData.getPassiveLevels()[id];
    }

    abstract public void registerSkillBuffs();

    public ListGameTooltips getFinalToolTips(PlayerMob player, int skillLevel, boolean onlyChanges) {
        if (containsComplexTooltips() && skillLevel > 0) {
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            int playerLevel = playerData.getLevel();
            int endurance = playerData.getEndurance(player);
            int speed = playerData.getSpeed(player);
            int strength = playerData.getStrength(player);
            int intelligence = playerData.getIntelligence(player);
            int grace = playerData.getGrace(player);

            ListGameTooltips tooltips = new ListGameTooltips();

            List<String> stringTooltips = getToolTipsText();
            stringTooltips.set(0, stringTooltips.get(0) + " - " + Localization.translate("ui", "level", "level", skillLevel));

            for (String toolTip : stringTooltips) {
                if (toolTip.contains("<") && toolTip.contains(">")) {
                    List<String> parts = splitToolTip(toolTip);

                    for (int i = 0; i < parts.size(); i++) {
                        String part = parts.get(i);
                        if (part.startsWith("[[") && part.endsWith("]]")) {
                            String inside = part.substring(2, part.length() - 2);

                            inside = inside
                                    .replaceAll("<skilllevel>", String.valueOf(skillLevel))
                                    .replaceAll("<playerlevel>", String.valueOf(playerLevel))
                                    .replaceAll("<endurance>", String.valueOf(endurance))
                                    .replaceAll("<speed>", String.valueOf(speed))
                                    .replaceAll("<strength>", String.valueOf(strength))
                                    .replaceAll("<intelligence>", String.valueOf(intelligence))
                                    .replaceAll("<grace>", String.valueOf(grace));

                            float value = calculateValue(inside);
                            String valueText;
                            if (value == (int) value) {
                                valueText = String.valueOf((int) value);
                            } else {
                                valueText = String.format("%.1f", value);
                            }

                            parts.set(i, valueText);
                        }
                    }

                    StringBuilder modifiedToolTip = new StringBuilder();
                    for (String p : parts) {
                        modifiedToolTip.append(p);
                    }

                    tooltips.add(modifiedToolTip.toString());
                } else {
                    if (!onlyChanges) tooltips.add(toolTip);
                }
            }
            return tooltips;
        }
        return getToolTips();
    }

    private static @NotNull List<String> splitToolTip(String toolTip) {
        List<String> parts = new ArrayList<>();
        int lastIndex = 0;

        while (lastIndex < toolTip.length()) {
            int startIndex = toolTip.indexOf("[[", lastIndex);
            if (startIndex == -1) {
                parts.add(toolTip.substring(lastIndex));
                break;
            }

            if (startIndex > lastIndex) {
                parts.add(toolTip.substring(lastIndex, startIndex));
            }

            int endIndex = toolTip.indexOf("]]", startIndex);
            if (endIndex == -1) {
                parts.add(toolTip.substring(startIndex));
                break;
            }

            parts.add(toolTip.substring(startIndex, endIndex + 2));
            lastIndex = endIndex + 2;
        }

        return parts;
    }

    public boolean containsComplexTooltips() {
        return false;
    }

    private float calculateValue(String expression) {
        String[] tokens = expression.split(" ");
        List<Float> values = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (token.equals("x")) {
                float prev = values.remove(values.size() - 1);
                float next;

                try {
                    next = Float.parseFloat(tokens[++i]);
                } catch (RuntimeException e) {
                    next = 0;
                }

                values.add(prev * next);
            } else if (token.equals("+") || token.equals("-")) {
                operators.add(token);
            } else {
                try {
                    float n = Float.parseFloat(token);
                    values.add(n);
                } catch (RuntimeException e) {
                    values.add(0F);
                }
            }
        }

        float result = values.get(0);
        int opIndex = 0;

        for (int i = 1; i < values.size(); i++) {
            String op = operators.get(opIndex++);
            if (op.equals("+")) {
                result += values.get(i);
            } else if (op.equals("-")) {
                result -= values.get(i);
            }
        }

        return result;
    }

    public static Point2D.Float getDir(PlayerMob player) {
        float dirX, dirY;

        if (player.dx == 0 && player.dy == 0) {
            Point2D.Float dir = getDirFromFacing(player.getDir());
            dirX = dir.x;
            dirY = dir.y;
        } else {
            dirX = player.dx;
            dirY = player.dy;

            float magnitude = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (magnitude != 0) {
                dirX /= magnitude;
                dirY /= magnitude;
            }

            Point2D.Float expected = getDirFromFacing(player.getDir());

            if (expected.x != 0 && dirX > 0 != expected.x > 0) {
                dirX = expected.x;
            }
            if (expected.y != 0 && dirY > 0 != expected.y > 0) {
                dirY = expected.y;
            }

        }

        return new Point2D.Float(dirX, dirY);
    }

    private static Point2D.Float getDirFromFacing(int dir) {
        switch (dir) {
            case 0:
                return new Point2D.Float(0, -1);
            case 1:
                return new Point2D.Float(1, 0);
            case 2:
                return new Point2D.Float(0, 1);
            case 3:
                return new Point2D.Float(-1, 0);
            default:
                return new Point2D.Float(0, 0);
        }
    }

    public String[] getExtraTooltips() {
        return new String[0];
    }
}
