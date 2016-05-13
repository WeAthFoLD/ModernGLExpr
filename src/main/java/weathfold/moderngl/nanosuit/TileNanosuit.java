package weathfold.moderngl.nanosuit;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

/**
 * An empty placeholder for the renderer to attach in.
 */
public class TileNanosuit extends TileEntity {

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
