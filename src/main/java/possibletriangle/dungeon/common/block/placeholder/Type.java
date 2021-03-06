package possibletriangle.dungeon.common.block.placeholder;

import net.minecraft.block.*;
import net.minecraft.client.renderer.model.BlockPartRotation;
import possibletriangle.dungeon.common.data.blockstate.Placeholders;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Type<T extends Block> {

    private static final ArrayList<Type> VALUES = new ArrayList<>();

    public static final Type<Block> FLOOR = new Type<>("FLOOR", TemplateBlock::new);
    public static final Type<Block> WALL = new Type<>("WALL", TemplateBlock::new);
    public static final Type<Block> PATH = new Type<>("PATH", TemplateBlock::new);
    public static final Type<Block> RUNE = new Type<>("RUNE", TemplateBlock::new);
    public static final Type<Block> GEM = new Type<>("GEM", TemplateBlock::new);
    public static final Type<Block> SEAL = new Type<>("SEAL", TemplateBlock::new);
    public static final Type<Block> GLASS = new Type<>("GLASS", TemplateTransparent::hide);
    public static final Type<Block> FALLING = new Type<>("FALLING", TemplateFalling::new);
    public static final Type<RotatedPillarBlock> PILLAR = new Type<>("PILLAR", TemplatePillar::new, Placeholders::pillar);
    public static final Type<Block> LAMP = new Type<>("LAMP", TemplateBlock.glowing(15));

    public static final Type<StairsBlock> STAIRS = new Type<>("STAIRS", TemplateStairs::new, Placeholders.stairs(() -> TemplateBlock.FLOOR));
    public static final Type<SlabBlock> SLAB = new Type<>("SLAB", TemplateSlab::new, Placeholders.slab(() -> TemplateBlock.FLOOR));
    public static final Type<StairsBlock> STAIRS_WALL = new Type<>("STAIRS_WALL", TemplateStairs::new, Placeholders.stairs(() -> TemplateBlock.WALL));
    public static final Type<SlabBlock> SLAB_WALL = new Type<>("SLAB_WALL", TemplateSlab::new, Placeholders.slab(() -> TemplateBlock.WALL));
    
    public static final Type<Block> PLANKS = new Type<>("PLANKS", TemplateBlock::new, Placeholders::full);
    public static final Type<StairsBlock> STAIRS_WOOD = new Type<>("STAIRS_WOOD", TemplateStairs::new, Placeholders.stairs(() -> TemplateBlock.PLANKS));
    public static final Type<SlabBlock> SLAB_WOOD = new Type<>("SLAB_WOOD", TemplateSlab::new, Placeholders.slab(() -> TemplateBlock.PLANKS));
    public static final Type<Block> LEAVES = new Type<>("LEAVES", TemplateTransparent::new);
    public static final Type<RotatedPillarBlock> STRIPPED_LOG = new Type<>("STRIPPED_LOG", TemplatePillar::new, Placeholders::pillar);
    public static final Type<RotatedPillarBlock> LOG = new Type<>("LOG", TemplatePillar::new, Placeholders::pillar);

    public static final Type<Block> DIRT = new Type<>("DIRT", TemplateGrass::new);
    public static final Type<Block> GRASS = new Type<>("GRASS", TemplateGrass::new, Placeholders.grass(() -> TemplateBlock.DIRT));
    public static final Type<Block> FARMLAND = new Type<>("FARMLAND", TemplateFarmland::new, Placeholders.farmland(() -> TemplateBlock.DIRT));
    public static final Type<Block> PLANT = new Type<>("PLANT", TemplateFlower::new, Placeholders::cross);
    public static final Type<Block> CROP = new Type<>("CROP", TemplateCrop::new, Placeholders::cross);
    public static final Type<Block> FRUIT = new Type<>("FRUIT", TemplateBlock::new, Placeholders::pillar);
    public static final Type<Block> BOOKSHELF = new Type<>("BOOKSHELF", TemplateBlock::new, Placeholders.pillar(() -> TemplateBlock.PLANKS));

    public static final Type<FenceBlock> FENCE = new Type<>("FENCE", TemplateFence::new, Placeholders.fence(() -> TemplateBlock.PLANKS));
    public static final Type<WallBlock> WALL_BLOCK = new Type<>("WALL_BLOCK", TemplateWall::new, Placeholders.wall(() -> TemplateBlock.WALL));
    public static final Type<Block> FLUID_UNSAFE = new Type<>("FLUID_UNSAFE", TemplateBlock::new, Placeholders::full);
    
    public static final Type<PressurePlateBlock> PRESSURE_PLATE = new Type<>("PRESSURE_PLATE", TemplatePlate::new, Placeholders.plate(() -> TemplateBlock.FLOOR));
    public static final Type<AbstractButtonBlock> BUTTON = new Type<>("BUTTON", TemplateButton::new, Placeholders.button(() -> TemplateBlock.WALL));
    public static final Type<LeverBlock> LEVER = new Type<>("LEVER", TemplateLever::new, Placeholders.lever(() -> TemplateBlock.WALL));

    private final Function<Type<T>, T> block;
    private final BiConsumer<T, Placeholders> model;
    private final String name;

    public void createModel(T name, Placeholders provider) {
        model.accept(name, provider);
    }

    public T block() {
        return this.block.apply(this);
    }

    Type(String name, Function<Type<T>, T> block, BiConsumer<T, Placeholders> model) {
        this.block = block;
        this.model = model;
        this.name = name;
        VALUES.add(this);
    }

    Type(String name, Function<Type<T>, T> block) {
        this(name, block, Placeholders::full);
    }

    public String name() {
        return this.name;
    }

    public static Stream<Type> values() {
        return VALUES.stream();
    }

}
