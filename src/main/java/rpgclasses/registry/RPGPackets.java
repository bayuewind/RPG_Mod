package rpgclasses.registry;

import necesse.engine.registries.PacketRegistry;
import rpgclasses.packets.*;
import rpgclasses.containers.rpgmenu.RPGMenuPacket;

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

    }

}
