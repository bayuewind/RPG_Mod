package rpgclasses.registry;

import necesse.engine.registries.ContainerRegistry;
import necesse.gfx.forms.presets.containerComponent.item.ItemInventoryContainerForm;
import necesse.inventory.container.item.ItemInventoryContainer;
import rpgclasses.containers.customiteminventory.CustomItemInventoryForm;
import rpgclasses.containers.rpgmenu.MenuContainer;
import rpgclasses.containers.rpgmenu.MenuContainerForm;

public class RPGContainers {
    public static int MENU_CONTAINER;
    public static int CUSTOM_ITEM_INVENTORY_CONTAINER;

    public static void registerCore() {
        MENU_CONTAINER = ContainerRegistry.registerContainer(
                (client, uniqueSeed, content) -> new MenuContainerForm(client, new MenuContainer(client.getClient(), uniqueSeed)),
                (client, uniqueSeed, content, serverObject) -> new MenuContainer(client, uniqueSeed)
        );

        CUSTOM_ITEM_INVENTORY_CONTAINER = ContainerRegistry.registerContainer(
                (client, uniqueSeed, packet) -> new CustomItemInventoryForm<>(client, new ItemInventoryContainer(client.getClient(), uniqueSeed, packet)),
                (client, uniqueSeed, packet, serverObject) -> new ItemInventoryContainer(client, uniqueSeed, packet)
        );

    }

}
