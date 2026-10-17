package com.arcaneterminal.conversion;

import com.arcaneterminal.ArcaneTerminal;
import com.arcaneterminal.registry.ATRegistry;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerFx;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.tom.storagemod.block.CraftingTerminalBlock;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * Turns a Tom's Crafting Terminal into an Arcane Crafting Terminal when Salis Mundus is used on it.
 *
 * <p>The conversion is defined by the data-driven {@code thaumaturge:dust_trigger_simple} recipe
 * {@code arcane_terminal:dust_trigger/arcane_crafting_terminal}, so it shows in JEI and keeps its
 * research gate. Thaumaturge's own handling would place the new block in its default orientation;
 * this handler runs first and keeps the terminal's facing, so it stays attached to its inventory.
 */
@EventBusSubscriber(modid = ArcaneTerminal.MODID)
public final class SalisMundusConversion {
    private static final int SWAP_DELAY_TICKS = 50;
    private static final float SOUND_VOLUME = 0.33F;
    private static final float SOUND_PITCH_BASE = 1.0F;
    private static final float SOUND_PITCH_DEVIATION = 0.05F;
    private static final int CONSUMED_COUNT = 1;
    private static final String NO_RESEARCH_KEY = "message.thaumaturge.salis_mundus.no_research";

    private SalisMundusConversion() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) {
            return;
        }
        ItemStack held = event.getItemStack();
        Player player = event.getPlayer();
        if (player == null || !held.is(TTItems.SALIS_MUNDUS.get())) {
            return;
        }
        UseOnContext context = event.getUseOnContext();
        BlockPos pos = event.getPos();
        BlockState clicked = event.getLevel().getBlockState(pos);
        if (!(clicked.getBlock() instanceof CraftingTerminalBlock)) {
            return;
        }
        if (!player.mayUseItemAt(pos, context.getClickedFace(), held)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)) {
            player.swing(context.getHand());
            event.cancelWithResult(InteractionResult.SUCCESS);
            return;
        }
        DustTriggerInput input = new DustTriggerInput(held, level, pos, clicked);
        Optional<RecipeHolder<DustTrigger>> match = level.recipeAccess().getRecipeFor(TTRecipeTypes.DUST_TRIGGER.get(), input, level);
        if (match.isEmpty()) {
            return;
        }
        RecipeHolder<DustTrigger> holder = match.get();
        DustTrigger trigger = holder.value();
        ItemStack result = trigger.assemble(input);
        if (!result.is(ATRegistry.ARCANE_CRAFTING_TERMINAL_ITEM.get())) {
            return;
        }
        event.cancelWithResult(InteractionResult.SUCCESS);
        if (!trigger.doesPassGate(serverPlayer)) {
            serverPlayer.sendOverlayMessage(Component.translatable(NO_RESEARCH_KEY).withStyle(ChatFormatting.DARK_PURPLE));
            return;
        }
        ItemStack consumed = held.copyWithCount(CONSUMED_COUNT);
        if (!serverPlayer.hasInfiniteMaterials()) {
            held.shrink(CONSUMED_COUNT);
        }
        BlockState converted = copyProperties(clicked, ATRegistry.ARCANE_CRAFTING_TERMINAL.get());
        DustTriggerSwapQueue.enqueuePlace(level, pos, clicked, converted, SWAP_DELAY_TICKS);
        emitEffects(level, serverPlayer, context.getHand(), pos, context, trigger);
        serverPlayer.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), result.getCount());
        ResearchProgressionEvents.recordCrafted(serverPlayer, result);
        CriteriaTriggers.RECIPE_CRAFTED.trigger(serverPlayer, holder.id(), List.of(consumed));
    }

    private static void emitEffects(ServerLevel level, ServerPlayer player, InteractionHand hand, BlockPos pos, UseOnContext context, DustTrigger trigger) {
        player.swing(hand, true);
        DustTriggerFx.emitUseBurst(level, player, hand, pos);
        DustTriggerFx.emitTriggerSparkles(level, player, pos, trigger, context.getClickLocation(), null);
        float pitch = SOUND_PITCH_BASE + (float) level.getRandom().nextGaussian() * SOUND_PITCH_DEVIATION;
        level.playSound(null, pos, TTSounds.DUST.get(), SoundSource.PLAYERS, SOUND_VOLUME, pitch);
    }

    private static BlockState copyProperties(BlockState from, Block target) {
        BlockState state = target.defaultBlockState();
        for (Property<?> property : from.getProperties()) {
            state = copy(from, state, property);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to, Property<T> property) {
        return to.hasProperty(property) ? to.setValue(property, from.getValue(property)) : to;
    }
}
