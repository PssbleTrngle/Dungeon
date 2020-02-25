package possibletriangle.dungeon.common.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.inventory.IClearable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import possibletriangle.dungeon.common.block.placeholder.IPlaceholder;
import possibletriangle.dungeon.helper.Pair;
import possibletriangle.dungeon.palette.Palette;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class PaletteCommand {

    private static final Random RANDOM = new Random();

    public static final SuggestionProvider<CommandSource> paletteSuggestions = (ctx, builder) -> ISuggestionProvider.suggestIterable(Palette.keys(), builder);

    private static final int MAX_SIZE = 32768;
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((max, size) -> new TranslationTextComponent("commands.fill.toobig", max, size));
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.fill.failed"));

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("palette")
            .then(Commands.literal("apply")
                    .then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos())
                        .then(Commands.argument("palette", ResourceLocationArgument.resourceLocation()).suggests(paletteSuggestions)
                                .executes(PaletteCommand::apply)
                                .then(Commands.argument("variant", IntegerArgumentType.integer(0, Palette.MAX_VARIANT))
                                    .executes(PaletteCommand::apply)
                                    .then(Commands.argument("seed", LongArgumentType.longArg())
                                        .executes(PaletteCommand::apply)
                                    )
                                )
            ))));
    }

    private static long getSeed(CommandContext<CommandSource> ctx) {
        try {
            return LongArgumentType.getLong(ctx, "seed");
        } catch(Exception ex) {
            return RANDOM.nextLong();
        }
    }

    private static int getVariant(CommandContext<CommandSource> ctx, long seed) {
        try {
            return IntegerArgumentType.getInteger(ctx, "variant");
        } catch(Exception ex) {
            return new Random(seed).nextInt();
        }
    }

    private static Palette getPalette(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ResourceLocation name = ResourceLocationArgument.getResourceLocation(ctx, "palette");
        return Palette.find(name).orElseThrow(() -> ResourceLocationArgument.UNKNOWN_ID.create(name));
    }

    private static int apply(CommandContext<CommandSource> ctx) throws CommandSyntaxException {

        BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");
        MutableBoundingBox area = new MutableBoundingBox(from, to);
        Palette palette = getPalette(ctx);
        long seed = getSeed(ctx);
        int variant = getVariant(ctx, seed);

        int replaced = replace(ctx.getSource().func_197023_e(), area, palette, seed, variant);
        if (replaced == 0) throw FAILED_EXCEPTION.create();

        ctx.getSource().sendFeedback(new TranslationTextComponent("commands.fill.success", replaced), true);
        return replaced;
    }

    private static int replace(ServerWorld world, MutableBoundingBox area, Palette palette, long seed, int variant) throws CommandSyntaxException {
        int size = area.getXSize() * area.getYSize() * area.getZSize();
        if (size > MAX_SIZE)
            throw TOO_BIG_EXCEPTION.create(MAX_SIZE, size);

        Random random = new Random(seed);

        Collection<Pair<BlockPos,BlockState>> map = BlockPos.getAllInBox(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ).map(pos -> {
            TileEntity tileentity = world.getTileEntity(pos);
            IClearable.clearObj(tileentity);

            BlockState block = new CachedBlockInfo(world, pos, true).getBlockState();
            Block current = block.getBlock();
            if(current instanceof IPlaceholder) {
                BlockState replace = palette.blockFor(((IPlaceholder) current).getType(), random, variant, block);
                return Optional.of(new Pair<>(new BlockPos(pos), replace));
            }

            return Optional.<Pair<BlockPos,BlockState>>empty();
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        return (int) map.stream()
                .filter(pair -> world.setBlockState(pair.getFirst(), pair.getSecond(), 2))
                .map(Pair::getFirst)
                .peek(pos -> world.notifyNeighbors(pos, world.getBlockState(pos).getBlock()))
                .count();
    }

}
