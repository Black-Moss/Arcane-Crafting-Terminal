package com.arcaneterminal.compat.jei;

import com.arcaneterminal.ArcaneTerminal;
import com.arcaneterminal.menu.ArcaneCraftingTerminalMenu;
import com.arcaneterminal.registry.ATRegistry;
import com.leclowndu93150.thaumaturge.compat.jei.category.ArcaneWorkbenchCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;

/**
 * Shows the terminal as a station for arcane and crafting recipes and lets JEI fill its grid and
 * crystal slots from the player's inventory, using the same slot layout as the arcane workbench.
 */
@JeiPlugin
public final class ArcaneTerminalJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = ArcaneTerminal.id("jei_plugin");
    private static final int PLAYER_SLOT_COUNT = 36;
    private static final int GRID_AND_CRYSTAL_SLOTS = ArcaneCraftingTerminalMenu.WAND_SLOT - ArcaneCraftingTerminalMenu.GRID_START;
    private static final int GRID_SLOTS = ArcaneCraftingTerminalMenu.GRID_END - ArcaneCraftingTerminalMenu.GRID_START;

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ArcaneWorkbenchCategory.RECIPE_TYPE, ATRegistry.ARCANE_CRAFTING_TERMINAL_ITEM.get());
        registration.addCraftingStation(RecipeTypes.CRAFTING, ATRegistry.ARCANE_CRAFTING_TERMINAL_ITEM.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ArcaneCraftingTerminalMenu.class, ATRegistry.ARCANE_CRAFTING_TERMINAL_MENU.get(), ArcaneWorkbenchCategory.RECIPE_TYPE, ArcaneCraftingTerminalMenu.GRID_START,
                GRID_AND_CRYSTAL_SLOTS, ArcaneCraftingTerminalMenu.PLAYER_START, PLAYER_SLOT_COUNT);
        registration.addRecipeTransferHandler(ArcaneCraftingTerminalMenu.class, ATRegistry.ARCANE_CRAFTING_TERMINAL_MENU.get(), RecipeTypes.CRAFTING, ArcaneCraftingTerminalMenu.GRID_START, GRID_SLOTS,
                ArcaneCraftingTerminalMenu.PLAYER_START, PLAYER_SLOT_COUNT);
    }
}
