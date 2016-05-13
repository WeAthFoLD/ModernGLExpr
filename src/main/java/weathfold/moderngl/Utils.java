package weathfold.moderngl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

/**
 * Unordered generic utils. Suitable to be static imported.
 */
public class Utils {

    public static final Logger log = LogManager.getLogger("ModernGL");

    public static InputStream getResourceStream(ResourceLocation res) {
        try {
            String domain = res.getResourceDomain(), path = res.getResourcePath();
            return Utils.class.getResourceAsStream("/assets/" + domain + "/" + path);
        } catch(Exception e) {
            throw new RuntimeException("Invalid resource " + res, e);
        }
    }

    public static ResourceLocation getShader(String loc) {
        return getResource("shader/" + loc);
    }

    public static ResourceLocation getTexture(String loc) {
        return getResource("tex/" + loc + ".png");
    }

    public static ResourceLocation getResource(String loc) {
        return new ResourceLocation("mgl:" + loc);
    }

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static TextureManager texManager() {
        return mc().getTextureManager();
    }

}
