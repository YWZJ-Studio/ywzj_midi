package org.ywzj.midi.storage;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.ywzj.midi.YwzjMidi;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class FakePlayerSkin {

    public final static ConcurrentHashMap<String, ResourceLocation> SKINS = new ConcurrentHashMap<>();

    static {
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert");
        File f = path.toFile();
        if (!f.exists()) {
            f.mkdir();
        }
        Path cachePath = path.resolve("skin_cache");
        File fSkin = cachePath.toFile();
        if (!fSkin.exists()) {
            fSkin.mkdir();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(String name, String skinUrl) {
        try {
            if (!skinUrl.startsWith("http")) {
                return;
            }
            String fileName = skinUrl.hashCode() + ".png";
            Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert").resolve("skin_cache").resolve(fileName);
            File cacheSkin = path.toFile();
            if (cacheSkin.exists()) {
                SKINS.put(name, toLocation(name, new FileInputStream(cacheSkin)));
                return;
            }
            java.net.URL url = new URL(skinUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            connection.setConnectTimeout(0);
            InputStream inputStream = connection.getInputStream();
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                return;
            }
            ImageIO.write(image, "png", path.toFile());
            SKINS.put(name, toLocation(name, new FileInputStream(path.toFile())));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static ResourceLocation toLocation(String name, InputStream inputStream) {
        NativeImage image = null;
        try {
            image = processLegacySkin(NativeImage.read(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image == null) {
            return new ResourceLocation(YwzjMidi.MOD_ID,"err");
        }
        return registerImage(image, name);
    }

    public static ResourceLocation registerImage(NativeImage image, String name) {
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        return manager.register(name.toLowerCase(Locale.ROOT).replace(" ", ""), new DynamicTexture(image));
    }

    @Nullable
    private static NativeImage processLegacySkin(NativeImage image) {
        int i = image.getHeight();
        int j = image.getWidth();
        if (j == 64 && (i == 32 || i == 64)) {
            boolean flag = i == 32;
            if (flag) {
                NativeImage nativeimage = new NativeImage(64, 64, true);
                nativeimage.copyFrom(image);
                image.close();
                image = nativeimage;
                nativeimage.fillRect(0, 32, 64, 32, 0);
                nativeimage.copyRect(4, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(8, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(0, 20, 24, 32, 4, 12, true, false);
                nativeimage.copyRect(4, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(8, 20, 8, 32, 4, 12, true, false);
                nativeimage.copyRect(12, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(48, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(40, 20, 0, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 20, -8, 32, 4, 12, true, false);
                nativeimage.copyRect(48, 20, -16, 32, 4, 12, true, false);
                nativeimage.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }

            setNoAlpha(image, 0, 0, 32, 16);
            if (flag) {
                doNotchTransparencyHack(image, 32, 0, 64, 32);
            }

            setNoAlpha(image, 0, 16, 64, 32);
            setNoAlpha(image, 16, 48, 48, 64);
            return image;
        } else {
            image.close();
            return null;
        }
    }

    private static void doNotchTransparencyHack(NativeImage p_118013_, int p_118014_, int p_118015_, int p_118016_, int p_118017_) {
        for(int i = p_118014_; i < p_118016_; ++i) {
            for(int j = p_118015_; j < p_118017_; ++j) {
                int k = p_118013_.getPixelRGBA(i, j);
                if ((k >> 24 & 255) < 128) {
                    return;
                }
            }
        }

        for(int l = p_118014_; l < p_118016_; ++l) {
            for(int i1 = p_118015_; i1 < p_118017_; ++i1) {
                p_118013_.setPixelRGBA(l, i1, p_118013_.getPixelRGBA(l, i1) & 16777215);
            }
        }

    }

    private static void setNoAlpha(NativeImage p_118023_, int p_118024_, int p_118025_, int p_118026_, int p_118027_) {
        for (int i = p_118024_; i < p_118026_; ++i) {
            for (int j = p_118025_; j < p_118027_; ++j) {
                p_118023_.setPixelRGBA(i, j, p_118023_.getPixelRGBA(i, j) | -16777216);
            }
        }
    }

}
