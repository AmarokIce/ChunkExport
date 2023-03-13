package club.someoneice.chunk_export;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChunkGetter {
    private final List<ChunkAccess> list;
    private final int maxChunk;
    private final @Nullable Player player;
    public ChunkGetter(ServerLevel world, int maxChunk, @Nullable Player player) {
        this.maxChunk = maxChunk;
        this.player = player;
        list = ChunkGenerator(world);
    }

    public final List<ChunkAccess> getList() {
        return list;
    }

    private List<ChunkAccess> ChunkGenerator(ServerLevel world) {
        // WorldBorder border = world.getWorldBorder();
        // int maxChunkPos = (int) Math.ceil(border.getSize() / 16);

        // float randomX = Math.abs(world.random.nextInt(maxChunkPos * 2) - maxChunkPos + (int) border.getCenterX());
        // float randomZ = Math.abs(world.random.nextInt(maxChunkPos * 2) - maxChunkPos + (int) border.getCenterZ());

        List<ChunkAccess> accessList = new ArrayList<>();
        randomChunkGenerator(world, this.maxChunk, accessList);
        if (accessList.size() < maxChunk) randomChunkGenerator(world, world.random.nextInt(this.maxChunk) + maxChunk, accessList);

        return accessList;
    }

    private void randomChunkGenerator(Level world, int chunk, List<ChunkAccess> list) {
        for (int x = 0; x < chunk; x ++) {
            for (int z = 0; z < chunk; z++) {
                if (!world.random.nextBoolean()) continue;
                if (!world.random.nextBoolean()) x = -x;
                if (!world.random.nextBoolean()) z = -z;

                list.add(world.getChunk(x + 1, z + 1));
                if (list.size() > this.maxChunk) return;
            }
            if (player != null) player.sendMessage(new TranslatableComponent("Sampling progress: " + list.size() + "/" + this.maxChunk), player.getUUID());

            if (list.size() > this.maxChunk) return;
        }
    }
}
