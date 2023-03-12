package club.someoneice.chunk_export;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static club.someoneice.chunk_export.JsonHandler.infoList;

public class ChunkProcessor extends Thread {
    public static Map<Block, Map<Integer, Integer>> blockOnLevelMap = new HashMap<>();
    public static Map<Block, BlockInfo> blockInfoMap = new HashMap<>();
    public static Map<Block, String> blockNameMap = new HashMap<>();

    List<ChunkAccess> list;
    ServerLevel level;
    int maxChunk;
    Player player;

    public ChunkProcessor(Player player, @Nonnull ServerLevel level, int maxChunk) {
        this.level = level;
        this.maxChunk = maxChunk;
        this.player = player;
    }

    @Override
    public void run() {
        ChunkGetter chunkManager = new ChunkGetter(level, maxChunk, player);
        if (chunkManager.getList().size() != maxChunk && chunkManager.getList().size() > maxChunk) {
            List<ChunkAccess> list = new ArrayList<>();
            for (int i = 0; i < maxChunk; i ++) list.add(chunkManager.getList().get(i));

            this.list = list;
        } else this.list = chunkManager.getList();

        ChunkExport.LOGGER.debug("Size: " + this.list.size());

        int chunkCount = this.list.size();

        for (ChunkAccess chunk : this.list) {
            player.sendSystemMessage(Component.literal("Remaining chunk : " + --chunkCount));
            for (int y = 1; y < 256; y++)
                ChunkScanner(chunk, y);
        }

        JsonHelper();

        player.sendSystemMessage(Component.translatable("chunk_export.success.message"));
    }

    private void ChunkScanner(ChunkAccess chunk, int posY) {
        for (int x = 0; x <  16; x ++) {
            for (int z = 0; z <  16; z++) {
                BlockPos pos = new BlockPos(x + 1, posY, z + 1);
                BlockState block = level.getBlockState(pos);
                if (!blockInfoMap.containsKey(block.getBlock()))
                    BlockInfoHandler(block, pos);

                if (!blockOnLevelMap.containsKey(block.getBlock())) {
                    HashMap<Integer, Integer> map = new HashMap<>();
                    map.put(posY, 1);
                    blockOnLevelMap.put(block.getBlock(), map);
                } else if (!blockOnLevelMap.get(block.getBlock()).containsKey(posY))
                    blockOnLevelMap.get(block.getBlock()).put(posY, 1);
                else blockOnLevelMap.get(block.getBlock()).put(posY, blockOnLevelMap.get(block.getBlock()).get(posY) + 1);
            }
        }
    }

    private void BlockInfoHandler(BlockState state, BlockPos pos) {
        List<ItemStack> dropList = Block.getDrops(state, level, pos, null);
        boolean canSilkTouch = state.getBlock().canHarvestBlock(state, level, pos, player);
        blockInfoMap.put(state.getBlock(), new BlockInfo(canSilkTouch, dropList));
    }

    private void JsonHelper() {
        player.sendSystemMessage(Component.translatable("chunk_export.operation.message"));
        Map<Block, Map<Integer, Integer>> dataMap = blockOnLevelMap;
        double buffer = ((maxChunk * 16.0D) * (maxChunk * 16.0D));
        for (Block block : dataMap.keySet()) {
            String name = blockNameMap.containsKey(block) ? blockNameMap.get(block) : putNameInDataMap(block);
            BlockInfo info = blockInfoMap.get(block);
            Map<Integer, Double> levelDataMap = new HashMap<>();

            for (int posY : dataMap.get(block).keySet())
                levelDataMap.put(posY, dataMap.get(block).get(posY) / buffer);

            Map<String, Integer> itemDrop = new HashMap<>();
            for (ItemStack item : info.dropsList)
                itemDrop.put(Registry.ITEM.getKey(item.getItem()).toString(), item.getCount());

            infoList.add(new JsonHandler.JsonBlockOutputInfo(name, levelDataMap, info.silktouch, itemDrop));
        }

        player.sendSystemMessage(Component.translatable("chunk_export.output.message"));
        new JsonHandler();
    }

    private String putNameInDataMap(Block block) {
        String name = Registry.BLOCK.getKey(block).toString();
        blockNameMap.put(block, name);
        return name;
    }
}
