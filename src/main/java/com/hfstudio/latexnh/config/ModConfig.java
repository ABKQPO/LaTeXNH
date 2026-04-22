package com.hfstudio.latexnh.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultEnum;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultInt;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultString;
import com.gtnewhorizon.gtnhlib.config.Config.Pattern;
import com.gtnewhorizon.gtnhlib.config.Config.RangeInt;
import com.gtnewhorizon.gtnhlib.config.Config.RequiresMcRestart;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.hfstudio.latexnh.LaTeXNH;
import com.hfstudio.latexnh.render.latex.LatexFontFamily;
import com.hfstudio.latexnh.render.latex.LatexHintMode;
import com.hfstudio.latexnh.render.latex.LatexRenderQuality;
import com.hfstudio.latexnh.render.latex.LatexRenderStyle;
import com.hfstudio.latexnh.render.latex.LatexTextureFiltering;

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
            "(default 16 ~= 2x font height). Valid range: 8 to 32" })
        @DefaultFloat(14.0f)
        @Config.RangeFloat(min = 8.0f, max = 32.0f)
        public float inlineHeight = 14.0f;

        @Comment({ "Source render scale passed to JLaTeXMath before the texture is uploaded",
            "Higher values usually look sharper but increase texture size and CPU cost. Valid range: 16 to 256" })
        @DefaultFloat(60.0f)
        @Config.RangeFloat(min = 16.0f, max = 256.0f)
        public float sourceRenderScale = 100.0f;

        @Comment({ "Base font family for rendered LaTeX formulas",
            "DEFAULT uses JLaTeXMath built-in fonts, UNIFONT uses the bundled font, SYSTEM uses the system font name below" })
        @DefaultEnum("DEFAULT")
        public LatexFontFamily fontFamily = LatexFontFamily.DEFAULT;

        @Comment({ "System font family name used when font family is SYSTEM",
            "If the requested font is missing, LaTeXNH falls back to JLaTeXMath defaults" })
        @DefaultString("")
        public String customSystemFontName = "";

        @Comment({ "Default fill color used when a LaTeX formula does not override it with Minecraft color codes",
            "Format: #RRGGBB or #AARRGGBB" })
        @DefaultString(LatexRenderStyle.DEFAULT_FILL_COLOR_HEX)
        @Pattern("^#?(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
        public String formulaColor = LatexRenderStyle.DEFAULT_FILL_COLOR_HEX;

        @Comment({ "Allow Minecraft and Angelica color codes to tint rendered LaTeX formulas",
            "Disable to always render formulas with the configured formula color above" })
        @DefaultBoolean(true)
        public boolean allowFormattingColor = true;

        @Comment({ "Texture sampling mode used when the uploaded LaTeX texture is scaled by OpenGL",
            "NEAREST keeps magnified formulas crisp, LINEAR is smoother but blurrier" })
        @DefaultEnum("LINEAR")
        public LatexTextureFiltering textureFiltering = LatexTextureFiltering.LINEAR;

        @Comment({ "General AWT vector antialiasing used when painting the LaTeX image",
            "DEFAULT lets Java decide, OFF is harsher, ON is smoother" })
        @DefaultEnum("ON")
        public LatexHintMode shapeAntialiasing = LatexHintMode.ON;

        @Comment({ "AWT text antialiasing used when JLaTeXMath paints glyphs into the texture",
            "DEFAULT lets Java decide, OFF is harsher, ON is smoother" })
        @DefaultEnum("ON")
        public LatexHintMode textAntialiasing = LatexHintMode.ON;

        @Comment({ "Overall AWT rendering quality hint for the intermediate LaTeX image",
            "QUALITY looks best, SPEED is faster, BALANCED is the middle ground" })
        @DefaultEnum("QUALITY")
        public LatexRenderQuality renderQuality = LatexRenderQuality.QUALITY;

        @Comment("Enable fractional font metrics when rasterizing LaTeX glyphs")
        @DefaultBoolean(true)
        public boolean enableFractionalMetrics = true;

        @Comment({ "Free-form scratch text for manual font and rendering tests",
            "LaTeXNH does not use this value anywhere in code; it is only stored in config for testing" })
        @DefaultString("LaTeXNH 字体测试 AaBb123 +-*/")
        public String testInputText = "LaTeXNH 字体测试 AaBb123 +-*/";

        @Comment({ "Outline color around rendered LaTeX formulas", "Format: #RRGGBB or #AARRGGBB" })
        @DefaultString(LatexRenderStyle.DEFAULT_OUTLINE_COLOR_HEX)
        @Pattern("^#?(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
        public String outlineColor = LatexRenderStyle.DEFAULT_OUTLINE_COLOR_HEX;

        @Comment({ "Outline thickness around rendered LaTeX formulas in source texture pixels",
            "Valid range: 0 to 12" })
        @DefaultInt(LatexRenderStyle.DEFAULT_OUTLINE_THICKNESS)
        @RangeInt(min = 0, max = 12)
        public int outlineThickness = LatexRenderStyle.DEFAULT_OUTLINE_THICKNESS;
    }
}
