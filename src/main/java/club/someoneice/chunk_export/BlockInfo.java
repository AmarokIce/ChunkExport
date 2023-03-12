package club.someoneice.chunk_export;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BlockInfo {
    public boolean silktouch;
    public List<ItemStack> dropsList;

    public BlockInfo(boolean silktouch, List<ItemStack> dropsList) {
        this.silktouch = silktouch;
        this.dropsList = dropsList;
    }
}
