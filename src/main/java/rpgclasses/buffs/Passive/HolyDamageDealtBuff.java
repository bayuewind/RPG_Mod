package rpgclasses.buffs.Passive;

import aphorea.utils.magichealing.AphMagicHealingBuff;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.registries.BuffRegistry;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobHealthChangeEvent;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rpgclasses.data.MobData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGDamageType;

public class HolyDamageDealtBuff extends PassiveBuff implements AphMagicHealingBuff {
    public HolyDamageDealtBuff() {
        isVisible = true;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
        isVisible = false;
    }

    @Override
    public void serverTick(ActiveBuff activeBuff) {
        super.serverTick(activeBuff);
        int used = getUsedDamageDealt(activeBuff);
        if (used > 0) {
            setUsedDamageDealt(activeBuff, 0);
            addDamageDealt(activeBuff, -used, true);
        } else {
            float passiveReduction = activeBuff.getGndData().getFloat("passiveReduction", 0);
            passiveReduction += 0.1F;
            if (passiveReduction >= 1) {
                int reduction = (int) passiveReduction;
                passiveReduction -= reduction;
                addDamageDealt(activeBuff, -reduction, true);
            }
            activeBuff.getGndData().setFloat("passiveReduction", passiveReduction);
        }
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        boolean b = getDamageDealt(activeBuff) > 0;
        this.isVisible = b;
        this.isImportant = b;
    }

    @Override
    public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
        super.onHasAttacked(activeBuff, event);
        int holyDamage = event.damage / 4;
        if (holyDamage > 0 && !event.wasPrevented) {
            if (event.damageType == RPGDamageType.HOLY) {
                addDamageDealt(activeBuff, holyDamage, false);
                Mob target = event.target;
                if (MobData.isWeakToHoly(target, activeBuff.owner)) {
                    target.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.ON_FIRE, target, 10F, activeBuff.owner), activeBuff.owner.isServer());
                }
            }
        }
    }

    @Override
    public void onMagicalHealing(ActiveBuff activeBuff, Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
        int extraHeal = Math.min(target.getMaxHealth() - target.getHealth(), getDamageDealt(activeBuff));
        if (extraHeal > 0) {
            target.getLevel().entityManager.addLevelEvent(new MobHealthChangeEvent(target, extraHeal));
            addUsedDamageDealt(activeBuff, extraHeal);
        }
    }

    @Override
    public void drawIcon(int x, int y, ActiveBuff activeBuff) {
        GameTexture drawIcon = this.getDrawIcon(activeBuff);
        drawIcon.initDraw().size(32, 32).draw(x, y);
        int numberDisplay = getDamageDealt(activeBuff);
        if (numberDisplay > 0) {
            String text = getString(numberDisplay);
            int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
            FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
        }
    }

    private static @NotNull String getString(float numberDisplay) {
        String postN = "";
        if (numberDisplay >= 1_000_000) {
            numberDisplay /= 1_000_000;
            postN = "M";
        } else if (numberDisplay >= 1000) {
            numberDisplay /= 1000;
            postN = "K";
        }

        String text;
        if (postN.isEmpty()) {
            text = String.valueOf(numberDisplay);
        } else {
            String withDecimal = String.format("%.1f%s", numberDisplay, postN).replace(".0", "");
            if (withDecimal.length() <= 4) {
                text = withDecimal;
            } else {
                String withoutDecimal = String.format("%.0f%s", numberDisplay, postN);
                if (withoutDecimal.length() <= 4) {
                    text = withoutDecimal;
                } else {
                    text = "999" + postN;
                }
            }
        }
        return text;
    }

    public static ActiveBuff getActiveBuff(Mob mob) {
        return mob.buffManager.getBuff(RPGBuffs.PASSIVES.HOLY_DAMAGE);
    }

    public static void addDamageDealt(ActiveBuff activeBuff, int amount, boolean sendPacket) {
        if (activeBuff != null) {
            setDamageDealt(activeBuff, getDamageDealt(activeBuff) + amount, sendPacket);
        }
    }

    public static void setDamageDealt(ActiveBuff activeBuff, int amount, boolean sendPacket) {
        if (activeBuff != null) {
            int set = Math.max(0, amount);
            activeBuff.getGndData().setInt("damageDealt", set);
            if (sendPacket && activeBuff.owner.isPlayer && activeBuff.owner.isServer()) {
                PlayerMob player = (PlayerMob) activeBuff.owner;
                player.getServerClient().sendPacket(new ModClientHolyDamageDealtPacket(player.getPlayerSlot(), set));
            }
        }
    }

    public static int getDamageDealt(ActiveBuff activeBuff) {
        if (activeBuff != null) {
            return activeBuff.getGndData().getInt("damageDealt", 0);
        }
        return 0;
    }

    public static void addUsedDamageDealt(ActiveBuff activeBuff, int amount) {
        if (activeBuff != null) {
            setUsedDamageDealt(activeBuff, getUsedDamageDealt(activeBuff) + amount);
        }
    }

    public static void setUsedDamageDealt(ActiveBuff activeBuff, int amount) {
        if (activeBuff != null) {
            activeBuff.getGndData().setInt("usedDamageDealt", Math.max(0, amount));
        }
    }

    public static int getUsedDamageDealt(ActiveBuff activeBuff) {
        if (activeBuff != null) {
            return activeBuff.getGndData().getInt("usedDamageDealt", 0);
        }
        return 0;
    }

    public static class ModClientHolyDamageDealtPacket extends Packet {
        public final int slot;
        public final int amount;

        public ModClientHolyDamageDealtPacket(byte[] data) {
            super(data);
            PacketReader reader = new PacketReader(this);
            this.slot = reader.getNextInt();
            this.amount = reader.getNextInt();
        }

        public ModClientHolyDamageDealtPacket(int slot, int amount) {
            this.slot = slot;
            this.amount = amount;

            PacketWriter writer = new PacketWriter(this);
            writer.putNextInt(slot);
            writer.putNextInt(amount);
        }

        public void processClient(NetworkPacket packet, Client client) {
            if (client.getSlot() == slot) {
                ActiveBuff ab = getActiveBuff(client.getPlayer());
                setDamageDealt(ab, amount, false);
            }
        }
    }
}
