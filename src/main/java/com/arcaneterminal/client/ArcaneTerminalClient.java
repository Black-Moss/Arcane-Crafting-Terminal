package com.arcaneterminal.client;

import com.arcaneterminal.ArcaneTerminal;
import com.arcaneterminal.registry.ATRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = ArcaneTerminal.MODID, dist = Dist.CLIENT)
public final class ArcaneTerminalClient {
    public ArcaneTerminalClient(IEventBus modBus) {
        modBus.addListener(ArcaneTerminalClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ATRegistry.ARCANE_CRAFTING_TERMINAL_MENU.get(), ArcaneCraftingTerminalScreen::new);
    }
}
