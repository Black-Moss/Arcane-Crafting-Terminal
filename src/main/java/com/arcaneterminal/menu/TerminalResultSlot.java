package com.arcaneterminal.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Result slot whose removal runs the craft on the server, like Thaumaturge's arcane result slot. */
public final class TerminalResultSlot extends Slot {
    private final ArcaneCraftingTerminalMenu menu;
    private int amountCrafted;

    public TerminalResultSlot(ResultContainer result, ArcaneCraftingTerminalMenu menu, int x, int y) {
        super(result, 0, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        return menu.craftsOnServer() ? menu.takeCraft() : super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        amountCrafted += numItemsCrafted;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        amountCrafted = 0;
    }
}
