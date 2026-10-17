package com.arcaneterminal.menu;

import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;

/**
 * Screen geometry shared by the menu and the screen.
 *
 * <p>Arcane mode: the top of the GUI is Tom's storage view (search bar, item rows, scrollbar);
 * below it sits the arcane workbench GUI, unchanged, shifted by {@link #ARCANE_X}/{@link #ARCANE_Y}.
 *
 * <p>Crafting mode: the exact layout of Tom's Crafting Terminal; the crystal and wand slots are
 * hidden off screen. In tall mode Tom's code moves every slot down together.
 */
public final class TerminalLayout {
    public static final int WIDTH = 194;
    public static final int STORAGE_SLOT_X = 7;
    public static final int STORAGE_SLOT_Y = 17;
    public static final int ROW_HEIGHT = 18;
    public static final int SLOT_SPACING = 18;
    /** Position used for slots that the current mode does not show. */
    public static final int HIDDEN = -10000;

    // Arcane mode
    public static final int STORAGE_ROWS = 3;
    /** Height of the storage panel with {@link #STORAGE_ROWS} rows, frame included. */
    public static final int STORAGE_PANEL_HEIGHT = STORAGE_SLOT_Y + STORAGE_ROWS * ROW_HEIGHT + 7;
    /** Offset of the arcane workbench GUI texture inside this screen. */
    public static final int ARCANE_X = 2;
    public static final int ARCANE_Y = STORAGE_PANEL_HEIGHT - 2;
    public static final int ARCANE_GUI_WIDTH = 190;
    public static final int ARCANE_GUI_HEIGHT = 234;
    public static final int HEIGHT = ARCANE_Y + ARCANE_GUI_HEIGHT;

    public static final int GRID_SIZE = 3;
    public static final int GRID_X = ARCANE_X + 41;
    public static final int GRID_Y = ARCANE_Y + 41;
    public static final int GRID_SPACING = 23;
    public static final int RESULT_X = ARCANE_X + 160;
    public static final int RESULT_Y = ARCANE_Y + 64;
    public static final int WAND_X = ARCANE_X + MenuArcaneWorkbench.WAND_X;
    public static final int WAND_Y = ARCANE_Y + MenuArcaneWorkbench.WAND_Y;
    public static final int INVENTORY_X = ARCANE_X + 16;
    public static final int INVENTORY_Y = ARCANE_Y + 151;
    /** The clear button sits on the wooden top-right corner of the workbench panel. */
    public static final int CLEAR_BUTTON_X = ARCANE_X + 127;
    public static final int CLEAR_BUTTON_Y = ARCANE_Y + 6;

    // Crafting mode (Tom's Crafting Terminal)
    public static final int CRAFTING_STORAGE_ROWS = 5;
    public static final int CRAFTING_HEIGHT = 256;
    public static final int CRAFTING_GRID_X = 26;
    public static final int CRAFTING_GRID_Y = 111;
    public static final int CRAFTING_RESULT_X = 120;
    public static final int CRAFTING_RESULT_Y = 129;
    public static final int CRAFTING_INVENTORY_X = 8;
    public static final int CRAFTING_INVENTORY_Y = 174;
    public static final int CRAFTING_CLEAR_BUTTON_X = 80;

    private TerminalLayout() {}

    public static int crystalX(int index) {
        return ARCANE_X + MenuArcaneWorkbench.CRYSTAL_X[index];
    }

    public static int crystalY(int index) {
        return ARCANE_Y + MenuArcaneWorkbench.CRYSTAL_Y[index];
    }
}
