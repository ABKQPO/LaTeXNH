package com.hfstudio.mixins.early.Minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.render.latex.LatexRenderTracker;
import com.hfstudio.render.latex.LatexTextureCache;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void latexnh$beginRenderFrame(CallbackInfo ci) {
        LatexRenderTracker.INSTANCE.beginFrame();
    }

    @Inject(
        method = "launchIntegratedServer(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;)V",
        at = @At("HEAD"))
    private void latexnh$onLoadWorld(String folderName, String worldName, WorldSettings settings, CallbackInfo ci) {
        // settings == null means world unload; clear all cached GL textures
        if (settings == null) {
            LatexTextureCache.INSTANCE.clearAll();
        }
    }
}
