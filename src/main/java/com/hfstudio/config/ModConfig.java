package com.hfstudio.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat;
import com.gtnewhorizon.gtnhlib.config.Config.RequiresMcRestart;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.hfstudio.LaTeXNH;

@Config(modid = LaTeXNH.MODID, filename = "latexnh", configSubDirectory = "latexnh")
@Config.LangKeyPattern(pattern = "latexnh.gui.config.%cat.%field", fullyQualified = true)
@Comment("LaTeXNH configuration")
public class ModConfig {

    public static void registerConfig() throws ConfigException {
        ConfigurationManager.registerConfig(ModConfig.class);
    }

    public static final Debug debug = new Debug();
    public static final Render render = new Render();

    @Comment("Debug settings")
    public static class Debug {

        @Comment("Enable verbose debug logging")
        @DefaultBoolean(false)
        @RequiresMcRestart
        public boolean enableDebugMode = false;
    }

    @Comment("LaTeX / Markdown rendering settings")
    public static class Render {

        @Comment("Enable inline LaTeX and Markdown rendering (disable to show raw text)")
        @DefaultBoolean(true)
        public boolean enableLatexRendering = true;

        @Comment("Enable Alt+hover tooltip that shows the raw LaTeX source or parse errors")
        @DefaultBoolean(true)
        public boolean enableHoverTooltip = true;

        @Comment({ "Tooltip image scale factor (1.0 = default size, increase for sharper preview)",
            "Valid range: 0.5 to 3.0" })
        @DefaultFloat(1.0f)
        @Config.RangeFloat(min = 0.5f, max = 3.0f)
        public float tooltipScale = 1.0f;

        @Comment({ "Target display height for inline formula rendering in GUI units",
            "(default 16 ≈ 2× font height).  Valid range: 8 to 32" })
        @DefaultFloat(14.0f)
        @Config.RangeFloat(min = 8.0f, max = 32.0f)
        public float inlineHeight = 14.0f;
    }
}
