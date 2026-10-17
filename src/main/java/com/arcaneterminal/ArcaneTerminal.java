package com.arcaneterminal;

import com.arcaneterminal.aura.TerminalAuraSource;
import com.arcaneterminal.registry.ATRegistry;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchAuraSourcesEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ArcaneTerminal.MODID)
public final class ArcaneTerminal {
    public static final String MODID = "arcane_terminal";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    private static final ResourceKey<CreativeModeTab> TOMS_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("toms_storage", "tab"));

    public ArcaneTerminal(IEventBus modBus) {
        ATRegistry.register(modBus);
        modBus.addListener(ArcaneTerminal::registerAuraSources);
        modBus.addListener(ArcaneTerminal::fillCreativeTabs);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void registerAuraSources(RegisterWorkbenchAuraSourcesEvent event) {
        event.register(new TerminalAuraSource());
    }

    private static void fillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(TOMS_TAB)) {
            event.accept(ATRegistry.ARCANE_CRAFTING_TERMINAL_ITEM.get());
        }
    }
}
