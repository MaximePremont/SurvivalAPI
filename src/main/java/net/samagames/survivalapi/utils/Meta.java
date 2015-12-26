package net.samagames.survivalapi.utils;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class Meta
{
    private static final UUID ID = UUID.fromString("3745e6a8-821a-4c53-bd7c-3a1246a458f0");

    public static ItemStack addMeta(ItemStack stack)
    {
        stack = new ItemStack(stack.getType(), stack.getAmount(), stack.getDurability());

        AttributeStorage storage = AttributeStorage.newTarget(stack, ID);
        storage.setData("dropped");

        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        stack.setItemMeta(itemMeta);

        return storage.getTarget();
    }

    public static boolean hasMeta(ItemStack stack)
    {
        if (stack == null)
            return false;

        ItemStack itemStack = new ItemStack(stack.clone());
        AttributeStorage storage = AttributeStorage.newTarget(itemStack, ID);

        return storage.getData("").equals("dropped");
    }
}
