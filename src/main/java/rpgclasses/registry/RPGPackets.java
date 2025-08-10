package rpgclasses.registry;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.registries.PacketRegistry;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Passive.HolyDamageDealtBuff;
import rpgclasses.containers.rpgmenu.RPGMenuPacket;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.Stormbound;
import rpgclasses.packets.*;

public class RPGPackets {

    public static void registerCore() {

        PacketRegistry.registerPacket(RPGMenuPacket.class);

        PacketRegistry.registerPacket(ActiveAbilityRunPacket.class);

        PacketRegistry.registerPacket(ShowModExpPacket.class);
        PacketRegistry.registerPacket(ShowDodgePacket.class);

        PacketRegistry.registerPacket(LoadPlayerDataPacket.class);

        PacketRegistry.registerPacket(UpdateClientExpPacket.class);
        PacketRegistry.registerPacket(UpdateClientResetsPacket.class);
        PacketRegistry.registerPacket(UpdateClientAttributesPacket.class);
        PacketRegistry.registerPacket(UpdateClientClassesPacket.class);
        PacketRegistry.registerPacket(UpdateClientEquippedActiveSkillsPacket.class);

        PacketRegistry.registerPacket(UpdateClientObjectGrabbedPacket.class);

        PacketRegistry.registerPacket(UpdateClientClassDataPacket.class);

        PacketRegistry.registerPacket(HolyDamageDealtBuff.ModClientHolyDamageDealtPacket.class);

        // Skills
        PacketRegistry.registerPacket(ResetSkillTime.class);

        PacketRegistry.registerPacket(Stormbound.LightningPacket.class);

    }

    public static class ResetSkillTime extends Packet {
        public final int slot;
        public final String buffStringID;

        public ResetSkillTime(byte[] data) {
            super(data);
            PacketReader reader = new PacketReader(this);
            this.slot = reader.getNextInt();
            this.buffStringID = reader.getNextString();
        }

        public ResetSkillTime(int slot, String buffStringID) {
            this.slot = slot;
            this.buffStringID = buffStringID;

            PacketWriter writer = new PacketWriter(this);
            writer.putNextInt(slot);
            writer.putNextString(buffStringID);
        }

        public void processClient(NetworkPacket packet, Client client) {
            if (client.getSlot() == slot) {
                ActiveBuff ab = client.getPlayer().buffManager.getBuff(buffStringID);
                if (ab != null) {
                    ab.getGndData().setInt("time", 0);
                    ab.getGndData().setBoolean("ready", false);
                }
            }
        }
    }

}
