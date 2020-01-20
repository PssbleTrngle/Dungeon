package possibletriangle.dungeon.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ObjectHolder;
import possibletriangle.dungeon.DungeonMod;

@ObjectHolder(DungeonMod.MODID)
public class BreakableBlock extends Block {

    @ObjectHolder("porous_stone")
    public static final Block STONE = null;

    @ObjectHolder("gravelous_gravel")
    public static final Block GRAVEL = null;

    @ObjectHolder("morsh_wood")
    public static final Block WOOD = null;

    public final ToolType tool;

    public BreakableBlock(Material material, SoundType sound, float hardness, ToolType tool) {
        super(Properties.create(material)
                .sound(sound)
                .harvestTool(tool)
                .hardnessAndResistance(hardness).noDrops()
        );
        this.tool = tool;
    }

    public BreakableBlock(Block parent) {
        this(parent.getDefaultState());
    }

    private BreakableBlock(BlockState parent) {
        this(parent.getMaterial(), parent.getSoundType(), parent.getBlockHardness(null, null), parent.getHarvestTool());
    }

}