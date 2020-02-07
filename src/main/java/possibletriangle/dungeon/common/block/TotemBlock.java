package possibletriangle.dungeon.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import possibletriangle.dungeon.common.block.tile.TotemTile;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TotemBlock extends ContainerBlock {

    public static final IProperty<State> STATE = EnumProperty.create("state", State.class);

    @ObjectHolder("dungeon:totem")
    public static final Block TOTEM = null;

    public TotemBlock() {
        super(Block.Properties.create(Material.ROCK, MaterialColor.GRAY)
                .hardnessAndResistance(-1.0F, 3600000.0F)
                .noDrops()
        );
        this.setDefaultState(getDefaultState().with(STATE, State.UNCLAIMED));
    }
    

    @SubscribeEvent
    public void blockColors(ColorHandlerEvent.Block event) {

        event.getBlockColors().register((s,w,p,i) -> {
            if(i == 0) return -1;
            return getTE(w, p).map(TotemTile::getColor).orElse(State.INVALID.color);
        }, TOTEM);

    }
    
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
		builder.add(STATE);
	}

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader world) {
        return new TotemTile();
    }

    public Optional<TotemTile> getTE(IBlockReader world, BlockPos pos) {
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

    public enum State implements IStringSerializable {
        UNCLAIMED(new Color(42,42,42)),
        CLAIMED(new Color(255, 255, 255)),
        INVALID(new Color(175, 17, 17));

        public final int color;
        State(int color) {
            this.color = color;
        }

        State(Color color) {
            this(color.getRGB());
        }

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }
    }

}
