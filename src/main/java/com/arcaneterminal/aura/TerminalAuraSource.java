package com.arcaneterminal.aura;

import com.arcaneterminal.block.ArcaneCraftingTerminalBlockEntity;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchAuraSource;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Pays the aura part of crafts run at an Arcane Crafting Terminal from the chunk aura around it,
 * honouring an Arcane Workbench Charger placed on top, exactly as a placed arcane workbench does.
 */
public final class TerminalAuraSource implements IWorkbenchAuraSource {
    @Override
    public int supply(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneWorkbench workbench, int need, TransactionContext transaction) {
        if (need <= 0 || context.position() == null) {
            return 0;
        }
        if (!(context.level().getBlockEntity(context.position()) instanceof ArcaneCraftingTerminalBlockEntity terminal)) {
            return 0;
        }
        return terminal.spendAura(need, transaction) ? need : 0;
    }
}
