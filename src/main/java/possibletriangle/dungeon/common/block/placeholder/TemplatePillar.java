package possibletriangle.dungeon.common.block.placeholder;

import net.minecraft.block.RotatedPillarBlock;
import net.minecraftforge.registries.ObjectHolder;
import possibletriangle.dungeon.DungeonMod;

@ObjectHolder(DungeonMod.ID)
public class TemplatePillar extends RotatedPillarBlock implements IPlaceholder {

    private final Type type;

    public TemplatePillar(Type type) {
        super(TemplateBlock.PROPERTIES());
        this.type = type;
        setRegistryName("placeholder_" + type.name().toLowerCase());
    }

    @Override
    public Type getType() {
        return this.type;
    }
}
