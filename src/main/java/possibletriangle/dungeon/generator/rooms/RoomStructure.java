package possibletriangle.dungeon.generator.rooms;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import possibletriangle.dungeon.generator.ChunkPrimerDungeon;
import possibletriangle.dungeon.generator.DungeonOptions;
import possibletriangle.dungeon.structures.DungeonStructur;

import java.util.HashMap;
import java.util.Random;

public class RoomStructure extends Room {

    private boolean genWall = true;

    public RoomStructure noRandomWall() {
        genWall = false;
        return this;
    }

    @Override
    public boolean generateWall() {
        return genWall;
    }

    public static final HashMap<String, RoomStructure> MAP = new HashMap<>();

    private boolean noCeil;

    public final DungeonStructur structure;

    public RoomStructure noCeil() {
        noCeil = true;
        return this;
    }

    public RoomStructure(String category, String id) {
        super(id);

        String source = category + "/" + id;
        structure = new DungeonStructur(source);

        MAP.put(source, this);
    }

    @Override
    public void generateAt(DungeonOptions options, ChunkPrimerDungeon primer, int floor, Random r, Rotation rotation) {
        structure.generate(primer, options, floor, (x,y,z) -> (x < 16 && z < 16 && x >= 0 && z >= 0 && y >= 0 && (y < DungeonOptions.FLOOR_HEIGHT || floor == options.floorCount()-1)), rotation );
    }

    @Override
    public void populate(DungeonOptions options, World world, int chunkX, int chunkZ, int floor, Random r) {
        structure.populate(world, chunkX, chunkZ, options, floor, Rotation.NONE, false, new BlockPos(0,0,0), r);
    }

    @Override
    public boolean noCeiling() {
        return noCeil;
    }
}
