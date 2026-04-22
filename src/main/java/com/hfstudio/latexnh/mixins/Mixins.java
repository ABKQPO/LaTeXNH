package com.hfstudio.latexnh.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    EARLY(new MixinBuilder()
        .addSidedMixins(
            Side.CLIENT,
            "minecraft.MixinEntityRenderer",
            "compat.MixinAngelicaBatchingFontRenderer",
            "minecraft.MixinFontRenderer",
            "minecraft.MixinMouse",
            "minecraft.MixinGuiScreen",
            "minecraft.MixinGuiTextField",
            "compat.MixinNEIFormattedTextField",
            "minecraft.MixinMinecraft",
            "compat.MixinSmoothFontFontRendererHook")
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
