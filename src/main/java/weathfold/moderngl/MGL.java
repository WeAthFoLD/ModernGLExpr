package weathfold.moderngl;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.util.ResourceLocation;
import weathfold.moderngl.nanosuit.BlockNanosuit;
import weathfold.moderngl.nanosuit.RendererNanosuit;
import weathfold.moderngl.nanosuit.TileNanosuit;

import static weathfold.moderngl.Utils.*;

@Mod(modid="moderngl", name="ModernGL", version="0.1")
public class MGL {

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        log.info("ModernGL is loading.");

        // Register block, TE and renderer
        GameRegistry.registerBlock(new BlockNanosuit(), "nanosuit");
        GameRegistry.registerTileEntity(TileNanosuit.class, "nanosuit");
        ClientRegistry.bindTileEntitySpecialRenderer(TileNanosuit.class, new RendererNanosuit());
    }

}
