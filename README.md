# LaTeXNH

A Minecraft 1.7.10 mod that renders **LaTeX** formulas and **Markdown** formatting in all text-rendering locations in the game.

## Features

- **Inline LaTeX rendering** — wrap any formula with `$* formula *$` and it will be rendered as a GL-textured image inline with the surrounding text.
- **Block LaTeX rendering** — wrap multi-line formulas with `$$* formula *$$` for a larger display.
- **Markdown support** — `**bold**`, `*italic*`, `~~strikethrough~~`, `` `code` ``
- **Alt+hover tooltip** — hold Alt while hovering over a rendered formula to see its raw LaTeX source in a tooltip.
- **Parse-error tooltip** — invalid formulas are shown in red; Alt+hover reveals the exact parse-error message with a column indicator.
- **Live text-field preview** — when typing in any `GuiTextField`, a rendered preview or error description appears automatically while your cursor is inside a formula.
- **Configurable chat limit** — the chat input length cap can be raised (default 256, up to 4096) to accommodate long formulas.
- **§ colour-code compatibility** — Minecraft formatting codes in plain-text segments pass through unaffected.

## Syntax

| Input | Effect |
|-------|--------|
| `$* E=mc^2 *$` | Inline LaTeX |
| `$$* \hat{H} = -\frac{\hbar^2}{2m}\nabla^2 + V *$$` | Block LaTeX |
| `**bold**` | **Bold** |
| `*italic*` | *Italic* |
| `~~strike~~` | ~~Strikethrough~~ |
| `` `code` `` | `Monospace` |

## Configuration

Open **Mods → LaTeXNH → Config** in-game to adjust:

| Option | Default | Description |
|--------|---------|-------------|
| Enable LaTeX Rendering | `true` | Master switch |
| Enable Alt+Hover Tooltip | `true` | Raw source / error tooltip |
| Tooltip Scale | `1.0` | Preview image scale factor |
| Inline Formula Height | `16` | GUI units; ~2× font height |
| Chat Input Max Length | `256` | Maximum chat characters |

## Building

```bash
./gradlew build
```

Requires Java 8+.  The mod targets Minecraft **1.7.10** with Forge **10.13.4.1614**.

## Dependencies (runtime)

- [GTNHLib](https://github.com/GTNewHorizons/GTNHLib) — config framework
- [GTNHMixins](https://github.com/GTNewHorizons/GTNHMixins) — Mixin injection infrastructure
- [JLaTeXMath](https://github.com/opencollab/jlatexmath) `1.0.7` — LaTeX rendering library

## License

GPL v3
