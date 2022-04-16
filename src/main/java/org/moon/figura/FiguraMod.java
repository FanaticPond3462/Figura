package org.moon.figura;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.providers.AvatarLoader;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.testing.LuaTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class FiguraMod implements ClientModInitializer {

    public static final String MOD_ID = "figura";
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();
    public static final boolean CHEESE_DAY = LocalDate.now().getDayOfMonth() == 1 && LocalDate.now().getMonthValue() == 4;
    public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
    public static final Logger LOGGER = LogManager.getLogger();

    public static int ticks = 0;

    @Override
    public void onInitializeClient() {
        //register fabric events
        ClientTickEvents.END_CLIENT_TICK.register(FiguraMod::tick);

        //TODO - test
        LuaTest.test();
    }


    private static Avatar a;
    public static void tick(MinecraftClient client) {
        ticks++;

        //TODO - test
        try {
            LocalAvatarFetcher.load();
            if (a == null && !LocalAvatarFetcher.ALL_AVATARS.isEmpty()) {
                a = AvatarLoader.loadAvatar(LocalAvatarFetcher.ALL_AVATARS.get(0).getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -- Helper Functions -- //

    //mod root directory
    public static Path getFiguraDirectory() {
        Path p = GAME_DIR.normalize().resolve(MOD_ID);
        try {
            Files.createDirectories(p);
        } catch (Exception e) {
            LOGGER.error("Failed to create the main Figura directory");
            LOGGER.error(e);
        }

        return p;
    }
}
