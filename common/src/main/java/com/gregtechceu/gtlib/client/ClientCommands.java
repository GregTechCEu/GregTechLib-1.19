package com.gregtechceu.gtlib.client;

import com.gregtechceu.gtlib.client.shader.Shaders;
import com.gregtechceu.gtlib.client.shader.management.ShaderManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote ClientCommands
 */
@Environment(EnvType.CLIENT)
public class ClientCommands {

    @ExpectPlatform
    public static LiteralArgumentBuilder createLiteral(String command) {
        throw new AssertionError();
    }

    @SuppressWarnings("unchecked")
    public static <S> List<LiteralArgumentBuilder<S>> createClientCommands() {
        return List.of(
                (LiteralArgumentBuilder<S>) createLiteral("gtlib_client").then(createLiteral("reload_shader")
                        .executes(context -> {
                            Shaders.reload();
                            ShaderManager.getInstance().reload();
                            return 1;
                        }))
        );
    }
}
