package com.nick.industrialcraft.content.block.cable;

import com.nick.industrialcraft.registry.ModBlocks;
import com.nick.industrialcraft.registry.ModTags;
import com.nick.industrialcraft.api.energy.OvervoltageHandler;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import com.nick.industrialcraft.registry.ModBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Base class for all energy cables (copper, insulated copper, gold, iron, etc).
 * - 6-way connections stored in the blockstate
 * - Dynamic voxel shapes based on connections
 * - BlockEntity for energy transfer
 */
public abstract class BaseCableBlock extends Block implements EntityBlock {

    // 6-way connection booleans
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;
    public static final BooleanProperty UP    = BlockStateProperties.UP;
    public static final BooleanProperty DOWN  = BlockStateProperties.DOWN;

    // Shapes (cable core + connection arms for each direction)
    private static final VoxelShape CORE  = Block.box(5, 5, 5, 11, 11, 11);  // 6px core
    private static final VoxelShape ARM_N = Block.box(5, 5, 0, 11, 11, 5);   // North arm
    private static final VoxelShape ARM_S = Block.box(5, 5, 11, 11, 11, 16); // South arm
    private static final VoxelShape ARM_W = Block.box(0, 5, 5, 5, 11, 11);   // West arm
    private static final VoxelShape ARM_E = Block.box(11, 5, 5, 16, 11, 11); // East arm
    private static final VoxelShape ARM_U = Block.box(5, 11, 5, 11, 16, 11); // Up arm
    private static final VoxelShape ARM_D = Block.box(5, 0, 5, 11, 5, 11);   // Down arm

    protected BaseCableBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST,  false).setValue(WEST,  false)
                .setValue(UP,    false).setValue(DOWN,  false));
    }

    /* ---------------- state + shapes ---------------- */

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_N);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_S);
        if (state.getValue(WEST))  shape = Shapes.or(shape, ARM_W);
        if (state.getValue(EAST))  shape = Shapes.or(shape, ARM_E);
        if (state.getValue(UP))    shape = Shapes.or(shape, ARM_U);
        if (state.getValue(DOWN))  shape = Shapes.or(shape, ARM_D);
        return shape.optimize();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getShape(state, level, pos, ctx);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        return this.defaultBlockState()
                .setValue(NORTH, canConnect(level, pos, Direction.NORTH))
                .setValue(SOUTH, canConnect(level, pos, Direction.SOUTH))
                .setValue(EAST,  canConnect(level, pos, Direction.EAST))
                .setValue(WEST,  canConnect(level, pos, Direction.WEST))
                .setValue(UP,    canConnect(level, pos, Direction.UP))
                .setValue(DOWN,  canConnect(level, pos, Direction.DOWN));
    }

    /** 1.21+ neighbor shape update. */
    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction dir,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        return state.setValue(prop(dir), canConnect(level, pos, dir));
    }

    private static BooleanProperty prop(Direction d) {
        return switch (d) {
            case NORTH -> NORTH; case SOUTH -> SOUTH; case EAST -> EAST;
            case WEST  -> WEST;  case UP    -> UP;    case DOWN -> DOWN;
        };
    }

    /* ---------- hide faces toward connected neighbor cables ---------- */

    @Override
    protected boolean skipRendering(BlockState self, BlockState other, Direction dir) {
        if (other.getBlock() instanceof BaseCableBlock) {
            BooleanProperty p = prop(dir);
            if (self.hasProperty(p) && self.getValue(p)) return true;
        }
        return super.skipRendering(self, other, dir);
    }

    /* ---------------- connectivity policy ---------------- */

    /**
     * Cable connection policy:
     *  - Connect to other BaseCableBlocks (all cables connect to all cables)
     *  - Connect to blocks in ENERGY_ACCEPTORS tag (machines that accept EU)
     *  - Connect to blocks in ENERGY_SOURCES tag (machines that produce EU)
     */
    protected boolean canConnect(LevelReader level, BlockPos pos, Direction dir) {
        if (level == null) return false;

        BlockPos np = pos.relative(dir);
        BlockState neighborState = level.getBlockState(np);
        if (neighborState == null) return false;

        Block neighborBlock = neighborState.getBlock();

        // Step 1: Connect to other cables
        if (neighborBlock instanceof BaseCableBlock) {
            return true;
        }

        // Step 2: Connect to energy acceptors (machines that accept EU)
        if (neighborState.is(ModTags.ENERGY_ACCEPTORS)) {
            return true;
        }

        // Step 3: Connect to energy sources (machines that produce EU)
        if (neighborState.is(ModTags.ENERGY_SOURCES)) {
            return true;
        }

        // Step 4: Direct block checks (fallback/debug)
        // These are redundant with the tag system but kept as a safety net
        if (neighborBlock == ModBlocks.GENERATOR.get()) {
            return true;
        }
        if (neighborBlock == ModBlocks.GEOTHERMAL_GENERATOR.get()) {
            return true;
        }
        if (neighborBlock == ModBlocks.ELECTRIC_FURNACE.get()) {
            return true;
        }

        return false;
    }

    /* ---------------- Overvoltage Check on Placement ---------------- */

    /**
     * When a cable is placed, check if it creates an overvoltage situation.
     * This happens INSTANTLY - if an MV generator is connected to an LV machine
     * through this cable, the machine will catch fire/explode immediately.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide && !isMoving) {
            // Invalidate network cache when cable is placed
            EnergyNetworkManager.invalidateAt(level, pos);
            // Schedule the overvoltage check for next tick to ensure block entity exists
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void destroy(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel && !realLevel.isClientSide()) {
            // Invalidate network cache when cable is removed
            EnergyNetworkManager.invalidateAt(realLevel, pos);
        }
        super.destroy(level, pos, state);
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        // Check for overvoltage when the cable connects the network
        OvervoltageHandler.checkOnPlacement(level, pos);
    }

    /* ---------------- EntityBlock implementation ---------------- */

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != ModBlockEntity.CABLE.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> CableBlockEntity.serverTick(lvl, pos, st, (CableBlockEntity) be);
    }

    /* ---------------- Capability Registration ---------------- */

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register energy capability for all cable types
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof CableBlockEntity cable) {
                    return cable.getEnergyStorage();
                }
                return null;
            },
            ModBlocks.COPPER_CABLE.get(),
            ModBlocks.INSULATED_COPPER_CABLE.get(),
            ModBlocks.GOLD_CABLE.get(),
            ModBlocks.GOLD_CABLE_INSULATED.get(),
            ModBlocks.GOLD_CABLE_DOUBLE_INSULATED.get(),
            ModBlocks.HIGH_VOLTAGE_CABLE.get(),
            ModBlocks.HIGH_VOLTAGE_CABLE_INSULATED.get(),
            ModBlocks.HIGH_VOLTAGE_CABLE_DOUBLE_INSULATED.get(),
            ModBlocks.HIGH_VOLTAGE_CABLE_QUADRUPLE_INSULATED.get(),
            ModBlocks.GLASS_FIBER_CABLE.get(),
            ModBlocks.ULTRA_LOW_CURRENT_CABLE.get()
        );
    }
}
