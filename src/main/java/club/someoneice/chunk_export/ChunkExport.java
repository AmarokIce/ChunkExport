package club.someoneice.chunk_export;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChunkExport.MODID)
public class ChunkExport {
    public static final String MODID = "chunk_export";
    public static final Logger LOGGER = LogManager.getLogger();

    public ChunkExport() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    boolean init = false;
    private void register(CommandDispatcher<CommandSourceStack> event) {
        CommandChunkExport(event);
    }

    @SubscribeEvent
    public void registerCommand(ServerStartedEvent event) {
        if (!init) {
            register(event.getServer().getCommands().getDispatcher());
            init = true;
        }
    }

    @SubscribeEvent
    public void register(RegisterCommandsEvent event) {
        if (init) register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        init = false;
    }

    public static void CommandChunkExport(CommandDispatcher<CommandSourceStack> event) {
        event.register(Commands.literal("chunk_export").then(Commands.argument("chunk", IntegerArgumentType.integer()).executes(export -> {
            ServerPlayer player = export.getSource().getPlayerOrException();

            try {
                if (player == null) return 0;
                ChunkProcessor runner = new ChunkProcessor(player, (ServerLevel) player.level, IntegerArgumentType.getInteger(export, "chunk"));
                runner.start();
                export.getSource().sendSuccess(new TranslatableComponent("Now start get chunk : " + IntegerArgumentType.getInteger(export, "chunk")), false);
            } catch (NumberFormatException e) {
                export.getSource().sendFailure(new TranslatableComponent("chunk_export.failed.message"));
            }

            return 0;
        })));
    }
}
