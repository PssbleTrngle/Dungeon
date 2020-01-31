package possibletriangle.dungeon.common.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import net.minecraft.world.storage.loot.functions.SetContents;
import net.minecraft.world.storage.loot.functions.SetDamage;
import possibletriangle.dungeon.DungeonMod;
import possibletriangle.dungeon.common.CommonProxy;
import possibletriangle.dungeon.common.block.BreakableBlock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class DungeonLoot extends LootTableProvider {

    public enum Rarity {
        COMMON, RARE, EPIC;

        public ResourceLocation path() {
            return new ResourceLocation(DungeonMod.MODID, name().toLowerCase());
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    protected final Map<Rarity, LootTable.Builder> lootTables = Maps.newHashMap();
    private final DataGenerator generator;

    public DungeonLoot(DataGenerator generator) {
        super(generator);
        this.generator = generator;
    }

    public void addTables() {

        LootPool.Builder pool = LootPool.builder()
                .name(Rarity.COMMON.path().getPath())
                .rolls(ConstantRange.of(4));

        HashMap<Item, Block> breakers = new HashMap<>();
        breakers.put(Items.GOLDEN_PICKAXE, BreakableBlock.STONE);
        breakers.put(Items.GOLDEN_AXE, BreakableBlock.WOOD);
        breakers.put(Items.GOLDEN_SHOVEL, BreakableBlock.GRAVEL);
        breakers.forEach((item, block) -> pool.addEntry(
                ItemLootEntry.builder(item)
                        .acceptFunction(CanBreak.builder(block))
                        .acceptFunction(SetDamage.func_215931_a(new RandomValueRange(10, 20)))
        ));

        Item[] swords = new Item[]{ Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD };
        IntStream.range(0, swords.length).forEach(i -> pool.addEntry(
               ItemLootEntry.builder(swords[i])
                       .weight(swords.length - i)
                       .acceptFunction(EnchantRandomly.func_215900_c())
        ));

        Item[] axes = new Item[]{ Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE };
        IntStream.range(0, axes.length).forEach(i -> pool.addEntry(
                ItemLootEntry.builder(axes[i])
                        .weight(axes.length - i)
                        .acceptFunction(EnchantRandomly.func_215900_c())
        ));

        lootTables.put(Rarity.COMMON, LootTable.builder().addLootPool(pool));

    }

    @Override
    public void act(DirectoryCache cache) {
        addTables();

        Map<ResourceLocation, LootTable> tables = Maps.newHashMap();
        lootTables.forEach((rarity, builder) ->
                tables.put(rarity.path(), builder.setParameterSet(LootParameterSets.CHEST).build()));

        writeTables(cache, tables);
    }

    private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                IDataProvider.save(GSON, cache, LootTableManager.toJson(lootTable), path);
            } catch (IOException e) {
                DungeonMod.LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }

    @Override
    public String getName() {
        return "Dungeon Loot";
    }

}
