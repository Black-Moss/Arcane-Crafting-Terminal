package com.arcaneterminal;

import com.tom.storagemod.util.KeyUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Tooltips in Tom's Simple Storage style: a "hold shift" hint, and the description lines (split on
 * {@code \}) while shift is held. Client only.
 */
public final class TomsTooltip {
    private static final String LINE_SEPARATOR = "\\\\";
    private static final String SHIFT_HINT = "tooltip.toms_storage.hold_shift_for_info";

    private TomsTooltip() {}

    public static void add(String key, boolean addShiftHint, Consumer<Component> tooltip, Object... args) {
        if (KeyUtil.hasShiftDown()) {
            for (String line : Component.translatable(key, args).getString().split(LINE_SEPARATOR)) {
                tooltip.accept(Component.literal(line));
            }
        } else if (addShiftHint) {
            tooltip.accept(Component.translatable(SHIFT_HINT).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
    }
}
