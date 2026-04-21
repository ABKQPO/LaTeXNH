package com.hfstudio.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    EARLY(new MixinBuilder()
        .addSidedMixins(
            Side.CLIENT,
            "Minecraft.MixinEntityRenderer",
            "Minecraft.MixinFontRenderer",
            "Minecraft.MixinMouse",
            "Minecraft.MixinGuiScreen",
            "Minecraft.MixinGuiTextField",
            "Minecraft.MixinMinecraft")
        .setPhase(Phase.EARLY));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
