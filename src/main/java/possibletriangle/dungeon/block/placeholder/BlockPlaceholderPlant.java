package possibletriangle.dungeon.block.placeholder;

import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import possibletriangle.dungeon.Dungeon;
import possibletriangle.dungeon.pallete.Pallete;

public class BlockPlaceholderPlant extends BlockBush implements IPlaceholder {

    private final ResourceLocation name;
    private final Pallete.Type type;

    public BlockPlaceholderPlant(Pallete.Type type) {
        super(Material.PLANTS);

        String id = type.name().toLowerCase();
        this.type = type;
        this.name = new ResourceLocation(Dungeon.MODID, "placeholder_" + id);

        setRegistryName(name);
        setUnlocalizedName(name.toString());
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return true;
    }

    @Override
    public Pallete.Type getType() {
        return type;
    }
}
