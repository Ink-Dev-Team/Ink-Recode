# Skija 渲染器使用指南

## 概述

这是一个基于 Skija (HumbleUI) 的 2D 渲染器，为 Minecraft Fabric 模块提供高性能的图形渲染功能。

## 文件结构

```
com.ink.recode.render/
├── Skia.java                 # 主要绘图 API
├── SkiaContext.java          # Skia 上下文管理
├── SkiaRenderer.java         # 渲染器初始化和管理
├── FontManager.java          # 字体管理器
├── font/
│   ├── FontType.java         # 字体类型枚举
│   ├── FontHelper.java       # 字体加载辅助类
│   └── Fonts.java            # 预设字体管理
├── image/
│   └── ImageHelper.java      # 图片加载辅助类
└── utils/
    └── SkiaUtils.java        # 工具类
```

## 初始化

渲染器在 `InkRecode.kt` 中自动初始化：

```kotlin
SkiaRenderer.init()
EventBus.register(Watermark)
```

## 基础使用

### 1. 创建渲染监听器

```kotlin
package com.ink.recode.render

import com.ink.recode.event.Listener
import com.ink.recode.event.events.RenderEvent
import io.github.humbleui.skija.Font
import java.awt.Color

object MyRenderer : Listener {
    private val font: Font by lazy { FontManager.getRegular(16f) }
    
    fun onRender(event: RenderEvent) {
        if (!SkiaRenderer.isInitialized()) return
        
        Skia.draw { canvas ->
            // 在这里绘制内容
            Skia.drawText("Hello Skija!", 10f, 20f, Color.WHITE, font)
        }
    }
}
```

### 2. 注册监听器

在 `InkRecode.kt` 中注册：

```kotlin
EventBus.register(MyRenderer)
```

## API 参考

### 图形绘制

#### 矩形
```kotlin
Skia.drawRect(x, y, width, height, color)
Skia.drawRoundedRect(x, y, width, height, radius, color)
Skia.drawRoundedRectVarying(x, y, width, height, topLeft, topRight, bottomRight, bottomLeft, color)
```

#### 圆形
```kotlin
Skia.drawCircle(x, y, radius, color)
Skia.drawCircle(x, y, radius, strokeWidth, color)
```

#### 线条
```kotlin
Skia.drawLine(x, y, endX, endY, width, color)
```

#### 圆弧
```kotlin
Skia.drawArc(x, y, radius, startAngle, endAngle, strokeWidth, color)
```

### 文字绘制

```kotlin
Skia.drawText(text, x, y, color, font)
Skia.drawCenteredText(text, x, y, color, font)
Skia.drawHeightCenteredText(text, x, y, color, font)
Skia.drawFullCenteredText(text, x, y, color, font)
```

### 图片绘制

```kotlin
// 从资源路径
Skia.drawImage("/assets/ink-recode/image.png", x, y, width, height)

// 从纹理 ID
Skia.drawImage(textureId, x, y, width, height)

// 圆角图片
Skia.drawRoundedImage(textureId, x, y, width, height, radius)

// 玩家头像
Skia.drawPlayerHead(identifier, x, y, width, height, radius)
```

### 效果

```kotlin
// 阴影
Skia.drawShadow(x, y, width, height, radius)

// 描边
Skia.drawOutline(x, y, width, height, radius, strokeWidth, color)

// 渐变圆角矩形
Skia.drawGradientRoundedRect(x, y, width, height, radius, color1, color2)
```

### 变换

```kotlin
Skia.save()      // 保存状态
Skia.restore()   // 恢复状态
Skia.translate(x, y)
Skia.scale(scale)
Skia.scale(x, y, width, height, scale)
Skia.rotate(x, y, width, height, angle)
Skia.setAlpha(alpha)
```

### 裁剪

```kotlin
Skia.clip(x, y, width, height, radius)
Skia.clip(x, y, width, height, radius, ClipMode)
```

## 字体使用

### 预设字体

```kotlin
val regularFont = FontManager.getRegular(16f)
val mediumFont = FontManager.getMedium(16f)
val boldFont = FontManager.getBold(16f)
```

### 自定义字体

将字体文件放入 `run/ink-recode/fonts/` 目录：

```kotlin
val customFont = FontManager.getCustomFont("MyFont", 16f)
```

### 获取可用字体列表

```kotlin
val fontNames = FontManager.getCustomFontNames()
```

## 完整示例

```kotlin
object ExampleRenderer : Listener {
    private val fontRegular = FontManager.getRegular(18f)
    private val fontMedium = FontManager.getMedium(24f)
    
    fun onRender(event: RenderEvent) {
        if (!SkiaRenderer.isInitialized()) return
        
        Skia.draw { canvas ->
            val x = 10f
            var y = 20f
            
            // 绘制标题
            Skia.drawText("示例渲染器", x, y, Color.WHITE, fontMedium)
            y += 40f
            
            // 绘制圆角矩形
            Skia.drawRoundedRect(x, y, 200f, 50f, 10f, Color(100, 150, 255, 200))
            y += 60f
            
            // 绘制圆形
            Skia.drawCircle(30f, y + 15f, 15f, Color(255, 100, 100, 255))
            y += 40f
            
            // 绘制线条
            Skia.drawLine(x, y, 210f, y, 2f, Color.WHITE)
        }
    }
}
```

## 注意事项

1. 所有绘制必须在 `Skia.draw { }` 块中进行
2. 窗口大小改变时会自动重新创建 Surface
3. 字体文件需要放在 `/assets/ink-recode/fonts/` 目录
4. 图片资源路径以 `/assets/ink-recode/` 开头
5. 使用 `SkiaRenderer.isInitialized()` 检查渲染器是否已初始化

## 性能优化

1. 使用 `lazy` 延迟初始化字体对象
2. 避免在渲染循环中创建新对象
3. 使用 `save()` 和 `restore()` 管理画布状态
4. 合理使用裁剪区域减少绘制范围
