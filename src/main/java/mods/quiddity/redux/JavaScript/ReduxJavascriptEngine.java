package mods.quiddity.redux.JavaScript;

import mods.quiddity.redux.json.model.Pack;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngineManager;

public class ReduxJavascriptEngine {
    public static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private JavascriptEngine engine;
    private final Pack packRefrence;

    public ReduxJavascriptEngine(Pack pack) {
        System.out.println(engineManager.getEngineFactories().toString()); // Debug Code for Ecu
        if (engineManager.getEngineByName("nashorn") != null) {
            engine = new NashornEngine();
        } else if (engineManager.getEngineByName("rhino") != null) {
            engine = new RhinoEngine();
        } else if (engineManager.getEngineByName("javascript") != null) {
            try {
                if (Class.forName("jdk.nashorn.api.scripting.NashornScriptEngine") != null) {
                    engine = new NashornEngine();
                } else if (engineManager.getEngineByName("javascript") != null) {
                    engine = new RhinoEngine();
                }
            } catch (ClassNotFoundException e) {
                throw new AssertionError("Your Java Runtime Environment does not support JSR-223", e);
            }
        }
        if (engine == null) {
            throw new AssertionError("Your Java Runtime Environment does not support JSR-223");
        }
        this.packRefrence = pack;
    }

    public JavascriptEngine getEngine() {
        return engine;
    }

    public void init() {
        engine.init();
        engine.addJavaObject("ReduxAPI", new ReduxAPI());
    }

    public class ReduxAPI implements ICommandSender {
        private Entity lastEntity = null;
        private World lastWorld = null;
        private BlockPos lastBlockPos = null;

        @SuppressWarnings("unused")
        public int runCommand(String... args) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance() != null &&
                    FMLCommonHandler.instance().getMinecraftServerInstance().isCallingFromMinecraftThread()) {
                if (engine.hasObject("entity") && engine.getObject("entity") instanceof Entity) {
                    lastEntity = (Entity) engine.getObject("entity");
                } else {
                    lastEntity = null;
                }
                if (engine.hasObject("world") && engine.getObject("world") instanceof World) {
                    lastWorld = (World) engine.getObject("world");
                } else {
                    lastWorld = null;
                }
                if (engine.hasObject("pos") && engine.getObject("pos") instanceof BlockPos) {
                    lastBlockPos = (BlockPos) engine.getObject("pos");
                } else {
                    lastBlockPos = null;
                }
                ICommandManager manager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
                manager.executeCommand(this, StringUtils.join(args));
            }
            return 0;
        }

        @Override
        public String getName() {
            return packRefrence.getName();
        }

        @Override
        public IChatComponent getDisplayName() {
            return new ChatComponentText(packRefrence.getName());
        }

        @Override
        public void addChatMessage(IChatComponent message) {
        }

        @Override
        public boolean canUseCommand(int permLevel, String commandName) {
            return permLevel <= 2;
        }

        @Override
        public BlockPos getPosition() {
            return lastBlockPos == null ? lastEntity == null ? new BlockPos(0, 0, 0) : lastEntity.getPosition() :lastBlockPos;
        }

        @Override
        public Vec3 getPositionVector() {
            return lastBlockPos == null ? lastEntity == null ? new Vec3(0, 0, 0) : lastEntity.getPositionVector() : new Vec3(lastBlockPos.getX(), lastBlockPos.getY(), lastBlockPos.getZ());
        }

        @Override
        public World getEntityWorld() {
            return lastWorld == null ? lastEntity == null ? null : lastEntity.worldObj : lastWorld;
        }

        @Override
        public Entity getCommandSenderEntity() {
            return lastEntity;
        }

        @Override
        public boolean sendCommandFeedback() {
            return false;
        }

        @Override
        public void setCommandStat(CommandResultStats.Type type, int amount) {
        }
    }
}
