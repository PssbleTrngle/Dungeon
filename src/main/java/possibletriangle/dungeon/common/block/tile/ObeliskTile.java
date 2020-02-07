package possibletriangle.dungeon.common.block.tile;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ObjectHolder;
import possibletriangle.dungeon.common.DungeonCommand;
import possibletriangle.dungeon.common.block.ObeliskBlock;
import possibletriangle.dungeon.common.world.DungeonSettings;
import possibletriangle.dungeon.common.world.room.Generateable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@ObjectHolder("dungeon")
public class ObeliskTile extends TileEntity implements ITickableTileEntity {

    @ObjectHolder("obelisk")
    public static final TileEntityType<ObeliskTile> TYPE = null;

    private static final AxisAlignedBB EMPTY = new AxisAlignedBB(0,0,0,0,0,0);

    @Nullable
    private BlockPos roomSize;
    @Nullable
    private Team team;
    @Nullable
    private UUID player;
    private int floor = -1;

    /**
     * Stores the players which where in the room last tick
     */
    private List<PlayerEntity> inRoom = Lists.newArrayList();
    private UUID claiming;
    private int claimProgress = 0;

    /**
     * The duration a player has to stand next to a totem to claim it in seconds
     */
    private static final int CLAIM_DURATION = 6;

    public ObeliskTile() {
        super(TYPE);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(this.inRoom()) return;

        /* Find the room it was placed in and save the required information */
        ChunkPos chunk = new ChunkPos(getPos());
        DungeonCommand.roomAt(chunk.asBlockPos(), world).ifPresent(pair -> {

            Generateable room = pair.getValue();
            this.floor = pair.getKey();
            this.roomSize = room.getSize();
            markDirty();

        });

        if(!this.inRoom()) updateState(ObeliskBlock.State.INVALID);

    }

    public boolean inRoom() {
        return this.roomSize != null && this.floor >= 0;
    }

    private AxisAlignedBB claimRange() {
        return new AxisAlignedBB(getPos()).grow(2);
    }

    private AxisAlignedBB roomBox() {
        if(!inRoom()) return EMPTY;
        assert this.roomSize != null;

        BlockPos start = new ChunkPos(getPos()).asBlockPos()
            .add(0, floor * DungeonSettings.FLOOR_HEIGHT, 0);

        BlockPos end = start.add(
            this.roomSize.getX() * 16,
            this.roomSize.getY() * DungeonSettings.FLOOR_HEIGHT,
            this.roomSize.getZ() * 16
        );

        return new AxisAlignedBB(start, end).grow(2);
    }

    public void tick() {
        if(!inRoom()) return;

        List<PlayerEntity> claiming = this.world.getEntitiesWithinAABB(PlayerEntity.class, claimRange());

        if(claiming.size() == 1)
            this.loadClaiming(claiming.get(0));

        else if(this.claiming != null)
            this.abort();


        if(isClaimed()) {
            List<PlayerEntity> inRoom = this.world.getEntitiesWithinAABB(PlayerEntity.class, roomBox());

            inRoom.forEach(player -> {
                boolean entered = this.inRoom.contains(player);
                if(entered) this.enteredRoom(player);
                else this.inRoom(player);
            });

            /**
             * All players which were in the room last tick but are not this one
             */
            this.inRoom.removeAll(inRoom);
            this.inRoom.forEach(this::leftRoom);

            this.inRoom = inRoom;
        } 
        
    }

    public void leftRoom(PlayerEntity player) {
        if(isOwner(player)) {
            player.sendStatusMessage(new StringTextComponent("You left your base"), true);
        }
    }

    public void enteredRoom(PlayerEntity player) {
        if(isOwner(player)) {
            player.sendStatusMessage(new StringTextComponent("You entered your base"), true);
        }
    }

    public void inRoom(PlayerEntity player) {
        if(isOwner(player)) {

        } else {
            
        }
    }

    public void loadClaiming(PlayerEntity player) {
        if(this.claiming == null) this.claiming = player.getUniqueID();
        else if(this.claiming.equals(player.getUniqueID())) {

            if(this.claimProgress < CLAIM_DURATION * 20) {

                this.claimProgress++;

                if(this.world != null) {
                    double x = Math.random() * 2 - 3.5 + getPos().getX();
                    double y = Math.random() * 2 + getPos().getY();
                    double z = Math.random() * 2 - 3.5 + getPos().getZ();
                    this.world.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
                }

            } else {

                this.claim(player);

            }

        }
            
        markDirty();
    }

    public void abort() {

        if(this.world != null) IntStream.range(0, 10).forEach(i -> {
            double x = Math.random() * 2 - 3.5 + getPos().getX();
            double y = Math.random() * 2 + getPos().getY();
            double z = Math.random() * 2 - 3.5 + getPos().getZ();
            this.world.addParticle(ParticleTypes.POOF, x, y, z, 0, 0, 0);
        });

        this.claiming = null;
        markDirty();
    }

    public boolean isClaimed() {
        return this.team != null || this.player != null;
    }

    public boolean claim(PlayerEntity player) {
        if(!this.isClaimed()) {

            Team team = player.getTeam();
            if(team != null) this.team = team;
            else this.player = player.getUniqueID();
            this.markDirty();

            if(this.world != null) IntStream.range(0, 20).forEach(i -> {
                double x = Math.random() * 2 - 3.5 + getPos().getX();
                double y = Math.random() * 2 + getPos().getY();
                double z = Math.random() * 2 - 3.5 + getPos().getZ();
                this.world.addParticle(ParticleTypes.ENCHANT, x, y, z, 0, 0, 0);
            });

            this.updateState(ObeliskBlock.State.CLAIMED);

            return true;
        }

        return false;
    }

    /**
     * @return If the player is part of the totems team or it is his totem
     */
    public boolean isOwner(PlayerEntity player) {

        Team team = player.getTeam();
        if(this.team != null && this.team.equals(team)) return true;
        return player.getUniqueID().equals(this.player);

    }

    public boolean click(PlayerEntity player) {
        return false;
    }

    /**
     * Called when the blockstate is changed to re-render BlockColor for the model
     * @return The current representing color as RGB int
     */
    public int getColor() {
        if(this.team != null) return Optional.ofNullable(team.getColor().getColor()).orElse(ObeliskBlock.State.CLAIMED.color);
        else if(this.player != null) return ObeliskBlock.State.CLAIMED.color;
        else if(this.inRoom()) return ObeliskBlock.State.UNCLAIMED.color;
        return ObeliskBlock.State.INVALID.color;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        this.roomSize = getPos(compound, "roomSize");
        if(compound.contains("floor")) this.floor = compound.getInt("floor");
        if(compound.hasUniqueId("player")) this.player = compound.getUniqueId("player");
        if(compound.contains("team")) this.team = world.getScoreboard().getTeam(compound.getString("team"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT nbt = super.write(compound);

        if(this.inRoom()) {
            putPos(this.roomSize, "roomSize", compound);
            compound.putInt("floor", this.floor);
        }

        if(this.player != null) compound.putUniqueId("player", this.player);
        if(this.team != null) compound.putString("team", this.team.getName());

        return nbt;
    }

    public static void putPos(BlockPos pos, String key, CompoundNBT compound) {
        if(pos != null) {
            compound.putInt(key + "X", pos.getX());
            compound.putInt(key + "Y", pos.getY());
            compound.putInt(key + "Z", pos.getZ());
        }
    }

    public static BlockPos getPos(CompoundNBT compound, String key) {
        int x = compound.getInt(key + "X");
        int y = compound.getInt(key + "Y");
        int z = compound.getInt(key + "Z");
        return new BlockPos(x, y, z);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {

    }

    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    private void updateState(ObeliskBlock.State state) {
        if(this.world == null) return;

        BlockState block = ObeliskBlock.TOTEM.getDefaultState().with(ObeliskBlock.STATE, state);
        this.world.setBlockState(getPos(), block);
    }

}