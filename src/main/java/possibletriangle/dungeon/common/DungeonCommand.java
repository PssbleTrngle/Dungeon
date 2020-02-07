package possibletriangle.dungeon.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javafx.util.Pair;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import possibletriangle.dungeon.DungeonMod;
import possibletriangle.dungeon.common.world.DungeonChunkGenerator;
import possibletriangle.dungeon.common.world.DungeonSettings;
import possibletriangle.dungeon.common.world.room.Generateable;
import possibletriangle.dungeon.common.world.structure.metadata.StructureMetadata;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class DungeonCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
          Commands.literal(DungeonMod.MODID)
            .then(Commands.literal("room")
                .then(Commands.literal("get")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(DungeonCommand::getRoom)
                ).executes(DungeonCommand::getRoom))
            )
        );
    }

    public static Optional<Pair<Integer,Generateable>> roomAt(BlockPos pos, World world) {

        ChunkPos chunk = new ChunkPos(pos);
        Map<Integer, Generateable> rooms = DungeonChunkGenerator.roomsFor(world, chunk);
        int floor = pos.getY() / (DungeonSettings.FLOOR_HEIGHT + 1);

        int nextFloor = rooms.keySet().stream().filter(f -> f <= floor).max(Comparator.comparingInt(a -> a)).orElse(0);
        Generateable room = rooms.get(nextFloor);
        if(room == null) return Optional.empty();
        return Optional.of(new Pair(floor, room));

    }

    private static int getRoom(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        BlockPos pos = new BlockPos(source.getPos());
        try {
            pos = BlockPosArgument.getBlockPos(context, "pos");
        } catch (CommandSyntaxException ignored) {}

        World world = source.getWorld();
        DungeonSettings settings = new DungeonSettings();

        Optional<Pair<Integer,Generateable>> pair = roomAt(pos, world);

        if(pair.isPresent()) {
            int floor = pair.get().getKey();
            Generateable room = pair.get().getValue();
            StructureMetadata meta = room.getMeta();
            Vec3i size = room.getSize();

            source.sendFeedback(new TranslationTextComponent("command.dungeon.get.room", meta.getDisplay()), false);
            source.sendFeedback(new TranslationTextComponent("command.dungeon.get.floor", floor, settings.floors), false);
            source.sendFeedback(new TranslationTextComponent("command.dungeon.get.size", size.getX(), size.getZ(), size.getY()), false);
            source.sendFeedback(new TranslationTextComponent("command.dungeon.get.palette", "unknown"), false);
            source.sendFeedback(new TranslationTextComponent("command.dungeon.get.weight", meta.getWeight()), false);
        } else {
            source.sendFeedback(new TranslationTextComponent("command.dungeon.get.no_room"), false);
        }

        return pair.isPresent() ? 1 : 0;
    }

}