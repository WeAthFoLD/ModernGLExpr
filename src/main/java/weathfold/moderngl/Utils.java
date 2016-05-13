package weathfold.moderngl;

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

}
