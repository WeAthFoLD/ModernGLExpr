package weathfold.moderngl;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

import static weathfold.moderngl.Utils.*;

@Mod(modid="moderngl", name="ModernGL", version="0.1")
public class MGL {

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        log.info("ModernGL is loading.");
    }

}
