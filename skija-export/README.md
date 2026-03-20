# Skija 封装类使用说明

这是从 SoarClient 提取的 Skija 封装类，方便在其他项目中使用。

## 依赖配置

### Gradle

```gradle
repositories {
    mavenCentral()
}

dependencies {
    // Skija 核心库
    implementation 'io.github.humbleui:types:0.2.0'
    
    // 各平台 Skija 原生库（根据目标平台选择）
    implementation 'io.github.humbleui:skija-windows-x64:0.143.8'
    implementation 'io.github.humbleui:skija-linux-x64:0.143.8'
    implementation 'io.github.humbleui:skija-linux-arm64:0.143.8'
    implementation 'io.github.humbleui:skija-macos-x64:0.143.8'
}
```

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.github.humbleui</groupId>
        <artifactId>types</artifactId>
        <version>0.2.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.humbleui</groupId>
        <artifactId>skija-windows-x64</artifactId>
        <version>0.143.8</version>
    </dependency>
</dependencies>
```

## 文件结构

```
skija-export/
├── SkiaContext.java          # Skia 上下文管理
├── Skia.java                 # 主要绘图 API
├── font/
│   ├── FontType.java         # 字体类型枚举
│   ├── FontHelper.java       # 字体加载辅助类
│   └── Fonts.java            # 字体管理类
├── image/
│   └── ImageHelper.java      # 图片加载辅助类
└── utils/
    └── SkiaUtils.java        # 工具类
```

## 使用步骤

### 1. 修改包名

将所有文件中的 `your.package.skia` 替换为你的实际包名。

### 2. 初始化

在游戏初始化时创建 Skia Surface：

```java
// 在窗口大小改变或初始化时调用
SkiaContext.createSurface(width, height);
```

### 3. 绘制

使用 Skia API 进行绘制：

```java
Skia.draw(canvas -> {
    // 绘制圆角矩形
    Skia.drawRoundedRect(x, y, width, height, radius, color);
    
    // 绘制文字
    Skia.drawText(text, x, y, color, font);
    
    // 绘制图片
    Skia.drawImage(textureId, x, y, width, height);
});
```

## API 列表

### 基础图形

- `drawRect(x, y, width, height, color)` - 绘制矩形
- `drawRoundedRect(x, y, width, height, radius, color)` - 绘制圆角矩形
- `drawRoundedRectVarying(...)` - 绘制不同圆角的矩形
- `drawCircle(x, y, radius, color)` - 绘制圆形
- `drawCircle(x, y, radius, strokeWidth, color)` - 绘制圆形边框
- `drawLine(x, y, endX, endY, width, color)` - 绘制线条
- `drawArc(...)` - 绘制圆弧

### 图片

- `drawImage(path, x, y, width, height)` - 绘制图片（从资源路径）
- `drawImage(textureId, x, y, width, height)` - 绘制 OpenGL 纹理
- `drawImage(file, x, y, width, height)` - 绘制文件图片
- `drawRoundedImage(...)` - 绘制圆角图片
- `drawPlayerHead(identifier, x, y, width, height, radius)` - 绘制玩家头像

### 文字

- `drawText(text, x, y, color, font)` - 绘制文字
- `drawCenteredText(text, x, y, color, font)` - 绘制居中文字
- `drawHeightCenteredText(...)` - 垂直居中文字
- `drawFullCenteredText(...)` - 完全居中文字
- `getTextBounds(text, font)` - 获取文字边界
- `getLimitText(text, font, width)` - 截断文字

### 效果

- `drawShadow(x, y, width, height, radius)` - 绘制阴影
- `drawOutline(...)` - 绘制描边
- `drawGradientRoundedRect(...)` - 绘制渐变圆角矩形

### 变换

- `save()` / `restore()` - 保存/恢复画布状态
- `translate(x, y)` - 平移
- `scale(scale)` / `scale(x, y, scale)` - 缩放
- `rotate(x, y, width, height, angle)` - 旋转
- `clip(...)` - 裁剪区域

## 字体使用

```java
// 加载字体
Font font = FontHelper.load("font.ttf", 16f);

// 或使用 Fonts 类
Font font = Fonts.getRegular(16f);
Font font = Fonts.getMedium(16f);
Font font = Fonts.getIcon(16f);
```

## 注意事项

1. 所有绘制必须在 `Skia.draw()` 回调中进行
2. 窗口大小改变时需要重新调用 `SkiaContext.createSurface()`
3. 字体文件需要放在 `/assets/yourmod/fonts/` 目录下
4. 图片资源路径以 `/assets/yourmod/` 开头
