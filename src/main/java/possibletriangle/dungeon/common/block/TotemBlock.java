package possibletriangle.dungeon.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import possibletriangle.dungeon.common.block.tile.TotemTile;

import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder("dungeon")
public class TotemBlock extends ContainerBlock {

    public static final IProperty<State> STATE = PropertyEnum.create(State.class);

    public static final Color INVALID = new Color(175, 17, 17);

    @ObjectHolder("totem")
    public static final Block TOTEM = null;

    public TotemBlock() {
        super(Block.Properties.create(Material.ROCK, MaterialColor.GRAY)
                .hardnessAndResistance(-1.0F, 3600000.0F)
                .noDrops()
        );
        this.setDefaultState(this.stateContainer.getBaseState().with(STATE, State.UNCLAIMED));
    }
    

    @SubscribeEvent
    public void blockColors(ColorHandlerEvent.Block event) {

        event.getBlockColors().register((s,w,p,i) -> {
            if(!(s.getBlock() instanceof TotemBlock) || i == 0) return -1;
            State state = s.get(STATE);

            switch(state) {
                case UNCLAIMED: return new Color(42,42,42);
                case INVALID: return INVALID;
                case CLAIMED: return getTE(w, p).map(TotemTile::getColor).orElse(INVALID);
            }

        }, TOTEM);

    }
    
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(STATE);
	}

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader world) {
        return new TotemTile();
    }

    public Optional<TotemTile> getTE(IWorldReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TotemTile) return Optional.of((TotemTile) te);
        return Optional.empty();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        return getTE(world, pos).map(te -> te.click(player)).orElse(false);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public enum State {
        UNCLAIMED, CLAIMED, INVALID;
    }

}
