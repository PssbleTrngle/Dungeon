package possibletriangle.dungeon.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.ObjectHolder;
import possibletriangle.dungeon.DungeonMod;

@ObjectHolder(DungeonMod.MODID)
public class TemplateBlock extends Block implements IPlaceholder {

    @ObjectHolder("placeholder_floor")
    public static final Block FLOOR = null;

    @ObjectHolder("placeholder_wall")
    public static final Block WALL = null;

    private final Type type;

    static final Properties PROPERTIES =
            Properties.create(Material.ROCK)
                .sound(SoundType.STONE)
                .hardnessAndResistance(1000.0F)
                .noDrops();

    public TemplateBlock(Type type) {
        super(PROPERTIES);
        this.type = type;
        setRegistryName("placeholder_" + type.name().toLowerCase());
    }

    @Override
    public Type getType() {
        return this.type;
    }
}
