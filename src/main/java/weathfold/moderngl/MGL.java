package weathfold.moderngl;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.util.ResourceLocation;

import static weathfold.moderngl.Utils.*;

@Mod(modid="moderngl", name="ModernGL", version="0.1")
public class MGL {

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        log.info("ModernGL is loading.");

        ObjParser.parse(new ResourceLocation("mgl:mdl/nanosuit2.obj"));
    }

}
