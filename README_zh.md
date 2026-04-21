# LaTeXNH

Minecraft 1.7.10 模组，在游戏中所有文字渲染位置支持渲染 **LaTeX** 公式和 **Markdown** 格式化。

## 功能

- **行内 LaTeX 渲染** — 用 `$* 公式 *$` 包裹任意公式，将在文字中渲染为 GL 纹理图像。
- **块级 LaTeX 渲染** — 用 `$$* 公式 *$$` 包裹多行公式，以更大尺寸显示。
- **Markdown 支持** — `**粗体**`、`*斜体*`、`~~删除线~~`、`` `代码` ``
- **Alt+悬停 Tooltip** — 将鼠标悬停于已渲染公式上并按住 Alt，可查看原始 LaTeX 源文本。
- **语法错误 Tooltip** — 无效公式以红色显示；Alt+悬停会展示含列号指示的具体错误信息。
- **文本框实时预览** — 在任意 `GuiTextField` 中输入时，光标位于公式内时自动显示渲染预览或错误描述。
- **聊天字数上限可配置** — 聊天输入框长度上限可从默认 256 提升至最多 4096，方便输入较长公式。
- **§ 颜色代码兼容** — 纯文字段中的 Minecraft 格式代码正常透传，不受干扰。

## 语法

| 输入 | 效果 |
|------|------|
| `$* E=mc^2 *$` | 行内 LaTeX |
| `$$* \hat{H} = -\frac{\hbar^2}{2m}\nabla^2 + V *$$` | 块级 LaTeX |
| `**粗体**` | **粗体** |
| `*斜体*` | *斜体* |
| `~~删除线~~` | ~~删除线~~ |
| `` `代码` `` | `等宽` |

## 配置

游戏内打开 **Mods → LaTeXNH → Config** 进行配置：

| 选项 | 默认值 | 说明 |
|------|--------|------|
| 启用 LaTeX 渲染 | `true` | 总开关 |
| 启用 Alt+悬停 Tooltip | `true` | 原文 / 错误 tooltip |
| Tooltip 缩放 | `1.0` | 预览图像缩放系数 |
| 行内公式高度 | `16` | GUI 单位；约 2 倍字体高度 |
| 聊天输入上限 | `256` | 聊天框最大字符数 |

## 构建

```bash
./gradlew build
```

需要 Java 8+。目标版本：Minecraft **1.7.10**，Forge **10.13.4.1614**。

## 运行时依赖

- [GTNHLib](https://github.com/GTNewHorizons/GTNHLib) — 配置框架
- [GTNHMixins](https://github.com/GTNewHorizons/GTNHMixins) — Mixin 注入框架
- [JLaTeXMath](https://github.com/opencollab/jlatexmath) `1.0.7` — LaTeX 渲染库

## 许可证

GPL v3