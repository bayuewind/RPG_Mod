package rpgclasses.containers.rpgmenu.customactions;

import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.inventory.container.customAction.ContainerCustomAction;

public abstract class IntArrayCustomAction extends ContainerCustomAction {
    public IntArrayCustomAction() {
    }

    public void runAndSend(int[] values) {
        Packet content = new Packet();
        PacketWriter writer = new PacketWriter(content);
        for (int value : values) {
            writer.putNextInt(value);
        }
        this.runAndSendAction(content);
    }

    public void executePacket(PacketReader reader) {
        int[] values = new int[arrayLength()];
        for (int i = 0; i < arrayLength(); i++) {
            values[i] = reader.getNextInt();
        }
        this.run(values);
    }

    protected abstract void run(int[] var1);

    public abstract int arrayLength();
}
