package possibletriangle.dungeon.palette;

import net.minecraft.block.BlockState;

import java.util.Arrays;
import java.util.Random;

public abstract class StateProvider implements IStateProvider {

    private PropertyProvider[] properties = new PropertyProvider[0];

    void setProperties(PropertyProvider[] properties) {
        this.properties = properties;
    }

    @Override
    public final BlockState apply(BlockState in, int variant, Random random) {
        return Arrays.stream(properties).reduce(applyChildren(in, variant, random), (s, p) -> p.apply(s, random), (a, b) -> a);
    }

    protected BlockState applyChildren(BlockState in, int variant, Random random) {
        return in;
    }

}
