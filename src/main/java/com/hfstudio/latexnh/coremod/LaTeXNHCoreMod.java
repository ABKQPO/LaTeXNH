package com.hfstudio.latexnh.coremod;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.mixins.Mixins;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name("LaTeXNH Core")
@IFMLLoadingPlugin.SortingIndex(1001)
public class LaTeXNHCoreMod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    static {
        try {
            ModConfig.registerConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getMixinConfig() {
        return "mixins.latexnh.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return IMixins.getEarlyMixins(Mixins.class, loadedMods);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
