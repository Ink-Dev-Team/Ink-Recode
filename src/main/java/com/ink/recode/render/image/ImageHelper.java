package com.ink.recode.render.image;

import com.ink.recode.render.SkiaContext;
import com.ink.recode.render.utils.SkiaUtils;
import io.github.humbleui.skija.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ImageHelper {

    private final Map<String, Image> images = new HashMap<>();
    private final Map<Integer, Image> textures = new HashMap<>();

    public boolean load(int texture, float width, float height, SurfaceOrigin origin) {

        if (!textures.containsKey(texture)) {
            Image image = Image.adoptGLTextureFrom(SkiaContext.getContext(), texture, GL11.GL_TEXTURE_2D, (int) width,
                (int) height, GL11.GL_RGBA8, origin, ColorType.RGBA_8888);
            textures.put(texture, image);
        }

        return true;
    }

    public boolean load(Identifier identifier) {
        if (images.containsKey(identifier.getPath())) return true;

        var mc = MinecraftClient.getInstance();
        var texture = mc.getTextureManager().getTexture(identifier);

        if (texture instanceof NativeImageBackedTexture nbt) {
            var ni = nbt.getImage();
            if (ni != null) {
                images.put(identifier.getPath(), nativeImageToSkijaImage(ni));
                return true;
            }
        } else {
            try {
                var resource = mc.getResourceManager().getResource(identifier);
                if (resource.isPresent()) {
                    try (InputStream is = resource.get().getInputStream()) {
                        images.put(identifier.getPath(), Image.makeDeferredFromEncodedBytes(is.readAllBytes()));
                        return true;
                    }
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    public boolean load(String filePath) {
        if (!images.containsKey(filePath)) {
            Optional<byte[]> encodedBytes = SkiaUtils.convertToBytes(filePath);
            if (encodedBytes.isPresent()) {
                Image image = Image.makeDeferredFromEncodedBytes(encodedBytes.get());
                images.put(filePath, image);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean load(File file) {

        if (!images.containsKey(file.getName())) {

            try {
                byte[] encoded = org.apache.commons.io.IOUtils.toByteArray(new FileInputStream(file));
                Image image = Image.makeDeferredFromEncodedBytes(encoded);
                images.put(file.getName(), image);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public Image get(String path) {

        if (images.containsKey(path)) {
            return images.get(path);
        }

        return null;
    }

    public Image get(int texture) {

        if (textures.containsKey(texture)) {
            return textures.get(texture);
        }

        return null;
    }

    public static Image nativeImageToSkijaImage(NativeImage nativeImage) {
        try {
            int width = nativeImage.getWidth();
            int height = nativeImage.getHeight();
            byte[] bytes = new byte[width * height * 4];
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = nativeImage.getColor(x, y);
                    int index = (y * width + x) * 4;
                    bytes[index] = (byte) ((color >> 16) & 0xFF);
                    bytes[index + 1] = (byte) ((color >> 8) & 0xFF);
                    bytes[index + 2] = (byte) (color & 0xFF);
                    bytes[index + 3] = (byte) ((color >> 24) & 0xFF);
                }
            }
            
            ImageInfo info = new ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL);
            return Image.makeRasterFromBytes(info, bytes, width * 4L);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
