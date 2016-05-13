package weathfold.moderngl.nanosuit;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockNanosuit extends Block implements ITileEntityProvider {

    public BlockNanosuit() {
        super(Material.rock);
        setBlockName("Nanosuit");
        setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par) {
        return new TileNanosuit();
    }

}
