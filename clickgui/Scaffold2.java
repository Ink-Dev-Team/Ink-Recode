package com.heypixel.heypixelmod.obsoverlay.modules.impl.move;

import com.heypixel.heypixelmod.obsoverlay.events.api.EventTarget;
import com.heypixel.heypixelmod.obsoverlay.events.api.types.EventType;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventPacket;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventRunTicks;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventRender;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventMotion;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventMoveInput;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventUpdateHeldItem;
import com.heypixel.heypixelmod.obsoverlay.modules.Category;
import com.heypixel.heypixelmod.obsoverlay.modules.Module;
import com.heypixel.heypixelmod.obsoverlay.modules.ModuleInfo;
import com.heypixel.heypixelmod.obsoverlay.utils.InventoryUtils;
import com.heypixel.heypixelmod.obsoverlay.utils.MathUtils;
import com.heypixel.heypixelmod.obsoverlay.utils.MoveUtils;
import com.heypixel.heypixelmod.obsoverlay.utils.rotation.RayCastUtil;
import com.heypixel.heypixelmod.obsoverlay.utils.rotation.Rotation;
import com.heypixel.heypixelmod.obsoverlay.utils.rotation.RotationManager;
import com.heypixel.heypixelmod.obsoverlay.utils.rotation.RotationUtils;
import com.heypixel.heypixelmod.obsoverlay.utils.PacketUtils;
import com.heypixel.heypixelmod.obsoverlay.values.ValueBuilder;
import com.heypixel.heypixelmod.obsoverlay.values.impl.BooleanValue;
import com.heypixel.heypixelmod.obsoverlay.values.impl.FloatValue;
import com.heypixel.heypixelmod.obsoverlay.values.impl.ModeValue;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@ModuleInfo(
    name = "Scaffold2",
    cnName = "自动搭路2",
    description = "Automatically places blocks under you",
    category = Category.MOVEMENT
)
public final class Scaffold2 extends Module {
    
    private final Minecraft mc = Minecraft.getInstance();
    
    // 黑名单方块
    public static final List<Block> blacklistedBlocks = Arrays.asList(
            Blocks.AIR,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.ENCHANTING_TABLE,
            Blocks.GLASS_PANE,
            Blocks.IRON_BARS,
            Blocks.SNOW,
            Blocks.COAL_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.TORCH,
            Blocks.ANVIL,
            Blocks.NOTE_BLOCK,
            Blocks.JUKEBOX,
            Blocks.TNT,
            Blocks.GOLD_ORE,
            Blocks.IRON_ORE,
            Blocks.LAPIS_ORE,
            Blocks.STONE_PRESSURE_PLATE,
            Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Blocks.STONE_BUTTON,
            Blocks.LEVER,
            Blocks.TALL_GRASS,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.RAIL,
            Blocks.CORNFLOWER,
            Blocks.RED_MUSHROOM,
            Blocks.BROWN_MUSHROOM,
            Blocks.VINE,
            Blocks.SUNFLOWER,
            Blocks.LADDER,
            Blocks.FURNACE,
            Blocks.SAND,
            Blocks.CACTUS,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.CRAFTING_TABLE,
            Blocks.COBWEB,
            Blocks.PUMPKIN,
            Blocks.COBBLESTONE_WALL,
            Blocks.OAK_FENCE,
            Blocks.REDSTONE_TORCH,
            Blocks.FLOWER_POT
    );
    
    // 模式设置
    public final ModeValue mode;
    private final ModeValue rotationMode;
    public final ModeValue rayCast;
    public final ModeValue sprint;
    public final ModeValue tower;
    public final ModeValue sameY;
    public final ModeValue downwards;
    
    // 数值设置
    private final FloatValue rotationSpeed;
    public final FloatValue placeDelay;
    private final FloatValue timer;
    private final FloatValue expand;
    private final FloatValue rotateBackSpeed;
    private final FloatValue tellyTick;
    
    // 布尔设置
    public final BooleanValue movementCorrection;
    public final BooleanValue safeWalk;
    private final BooleanValue newRots;
    private final BooleanValue keepY;
    private final BooleanValue watchdogTelly;
    private final BooleanValue watchdogTelly2;
    private final BooleanValue diagonal;
    private final BooleanValue heypixel;
    private final BooleanValue heypixel2;
    public final FloatValue tellyAirRotSpeed;
    private final BooleanValue telly;
    private final BooleanValue snap;
    
    // 潜行设置
    private final BooleanValue sneak;
    public final FloatValue startSneaking;
    public final FloatValue stopSneaking;
    public final FloatValue sneakEvery;
    public final FloatValue sneakingSpeed;
    
    // 渲染设置
    private final BooleanValue render;
    private final BooleanValue advanced;
    
    // 其他设置
    public final ModeValue yawOffset;
    public final BooleanValue ignoreSpeed;
    public final BooleanValue upSideDown;
    
    // 状态变量
    private Vec3 targetBlock;
    private Direction enumFacing;
    private boolean blocks4;
    public Vec3 offset;
    private BlockPos blockFace;
    private float targetYaw;
    private float targetPitch;
    private float forward;
    private float strafe;
    private float yawDrift;
    private float pitchDrift;
    private int ticksOnAir;
    private int sneakingTicks;
    private int placements;
    private int slow;
    private int pause;
    public int recursions;
    public int recursion;
    public double startY;
    private boolean canPlace;
    private int directionalChange;
    private int test;
    private int tickSinceEnable;
    private int airTick;
    private int yLevel;
    private BlockPos blockPos;
    private int oldSlot = -1;
    private int bestSlot = -1;
    private int originSlot = -1;
    private ItemStack originalItem = null;
    
    public Scaffold2() {
        // 核心设置初始化
        this.telly = ValueBuilder.create(this, "Telly")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
        
        this.snap = ValueBuilder.create(this, "Snap")
            .setDefaultBooleanValue(false)
            .setVisibility(() -> !telly.getCurrentValue())
            .build()
            .getBooleanValue();
        
        this.rotationSpeed = ValueBuilder.create(this, "Rotation Speed")
            .setDefaultFloatValue(180.0f)
            .setFloatStep(1.0f)
            .setMinFloatValue(1.0f)
            .setMaxFloatValue(720.0f)
            .build()
            .getFloatValue();
        
        this.rotateBackSpeed = ValueBuilder.create(this, "Rotation Back Speed")
            .setDefaultFloatValue(180.0f)
            .setFloatStep(1.0f)
            .setMinFloatValue(1.0f)
            .setMaxFloatValue(720.0f)
            .setVisibility(telly::getCurrentValue)
            .build()
            .getFloatValue();
        
        this.tellyTick = ValueBuilder.create(this, "Telly Ticks")
            .setDefaultFloatValue(1.0f)
            .setFloatStep(1.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(6.0f)
            .setVisibility(telly::getCurrentValue)
            .build()
            .getFloatValue();
        
        this.safeWalk = ValueBuilder.create(this, "Safe Walk")
            .setDefaultBooleanValue(true)
            .setVisibility(() -> !telly.getCurrentValue())
            .build()
            .getBooleanValue();
        
        // 模式设置初始化
        this.mode = ValueBuilder.create(this, "Mode")
            .setModes("Normal", "Godbridge", "Breesily", "Snap", "Telly", "Eagle")
            .setDefaultModeIndex(0)
            .build()
            .getModeValue();
        
        this.rotationMode = ValueBuilder.create(this, "Rotation Mode")
            .setModes("Normal")
            .setDefaultModeIndex(0)
            .build()
            .getModeValue();
        
        this.rayCast = ValueBuilder.create(this, "Ray Cast")
            .setModes("Off", "Normal", "Strict")
            .setDefaultModeIndex(2)
            .build()
            .getModeValue();
        
        this.sprint = ValueBuilder.create(this, "Sprint")
            .setModes("Normal", "Disabled", "Legit", "Bypass", "Vulcan", "Verus", "Matrix", "Watchdog Prediction", "Watchdog Jump", "Watchdog")
            .setDefaultModeIndex(0)
            .build()
            .getModeValue();
        
        this.tower = ValueBuilder.create(this, "Tower")
            .setModes("Disabled", "Vulcan", "Vanilla", "Normal", "Air Jump", "Watchdog", "MMC", "NCP", "Matrix", "Legit", "Verus", "Watchdog Prediction 1.8")
            .setDefaultModeIndex(0)
            .build()
            .getModeValue();
        
        this.sameY = ValueBuilder.create(this, "Same Y")
            .setModes("Off", "On", "Auto Jump")
            .setDefaultModeIndex(0)
            .build()
            .getModeValue();
        
        this.downwards = ValueBuilder.create(this, "Downwards (Press Sneak)")
            .setModes("Off", "Normal", "Watchdog", "Verus")
            .setDefaultModeIndex(0)
            .build()
            .getModeValue();
        
        // 数值设置初始化
        this.placeDelay = ValueBuilder.create(this, "Place Delay")
            .setDefaultFloatValue(0.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(5.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();
        
        this.timer = ValueBuilder.create(this, "Timer")
            .setDefaultFloatValue(1.0f)
            .setMinFloatValue(0.1f)
            .setMaxFloatValue(10.0f)
            .setFloatStep(0.1f)
            .build()
            .getFloatValue();
        
        this.expand = ValueBuilder.create(this, "Expand")
            .setDefaultFloatValue(0.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(4.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();
        
        // 布尔设置初始化
        this.movementCorrection = ValueBuilder.create(this, "Movement Correction")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        this.newRots = ValueBuilder.create(this, "New Watchdog Rots")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        this.keepY = ValueBuilder.create(this, "Keep-Y bypass")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
        
        this.watchdogTelly = ValueBuilder.create(this, "Watchdog Prediction")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        this.watchdogTelly2 = ValueBuilder.create(this, "Watchdog Telly")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        this.diagonal = ValueBuilder.create(this, "Block Diagonal Ascend")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
        
        this.heypixel = ValueBuilder.create(this, "Don't force raycast on Watchdog Telly")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        this.heypixel2 = ValueBuilder.create(this, "Extend Block Reach on Watchdog Telly")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
        
        this.tellyAirRotSpeed = ValueBuilder.create(this, "Watchdog Telly Rotation Speed")
            .setDefaultFloatValue(35.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(180.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();
        
        // 潜行设置初始化
        this.sneak = ValueBuilder.create(this, "Sneak")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        this.startSneaking = ValueBuilder.create(this, "Start Sneaking")
            .setDefaultFloatValue(0.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(5.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();
        
        this.stopSneaking = ValueBuilder.create(this, "Stop Sneaking")
            .setDefaultFloatValue(0.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(5.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();
        
        this.sneakEvery = ValueBuilder.create(this, "Sneak every x blocks")
            .setDefaultFloatValue(1.0f)
            .setMinFloatValue(1.0f)
            .setMaxFloatValue(10.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();
        
        this.sneakingSpeed = ValueBuilder.create(this, "Sneaking Speed")
            .setDefaultFloatValue(0.2f)
            .setMinFloatValue(0.05f)
            .setMaxFloatValue(1.0f)
            .setFloatStep(0.05f)
            .build()
            .getFloatValue();
        
        // 渲染设置初始化
        this.render = ValueBuilder.create(this, "Render")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
        
        this.advanced = ValueBuilder.create(this, "Advanced")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        // 其他设置初始化
        this.yawOffset = ValueBuilder.create(this, "Yaw Offset")
            .setModes("0", "45", "-45")
            .setDefaultModeIndex(0)
            .build()
            .getModeValue();
        
        this.ignoreSpeed = ValueBuilder.create(this, "Ignore Speed Effect")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        this.upSideDown = ValueBuilder.create(this, "Up Side Down")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
        
        // 初始化状态变量
        this.offset = new Vec3(0.0, 0.0, 0.0);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            oldSlot = mc.player.getInventory().selected;
            originSlot = mc.player.getInventory().selected;
            // 保存原始物品
            originalItem = mc.player.getMainHandItem().copy();
        }
        airTick = 0;
        blockPos = null;
        enumFacing = null;
        // 初始化旋转值
        this.targetYaw = mc.player.getYRot() - 180.0F + 
            Float.parseFloat(String.valueOf(this.yawOffset.getCurrentValue()));
        this.targetPitch = 90.0F;
        this.pitchDrift = (float)((Math.random() - 0.5) * (Math.random() - 0.5) * 10.0);
        this.yawDrift = (float)((Math.random() - 0.5) * (Math.random() - 0.5) * 10.0);
        this.tickSinceEnable = 0;
        this.startY = Math.floor(mc.player.getY());
        this.targetBlock = null;
        this.sneakingTicks = -1;
        this.recursions = 0;
        this.placements = 0;
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        boolean isHoldingShift = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyShift.getKey().getValue());
        mc.options.keyShift.setDown(isHoldingShift);
        if (mc.player != null && oldSlot != -1) {
            mc.player.getInventory().selected = oldSlot;
        }
        if (mc.player.tickCount % 2 == 0) {
            this.blocks4 = false;
        }
    }
    
    @EventTarget
    public void onRunTicks(EventRunTicks event) {
        if (mc.player == null || mc.level == null || event.type() != EventType.PRE) return;
        
        // 物品栏选择
        int slotID = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (isValidStack(stack)) {
                slotID = i;
                break;
            }
        }
        if (slotID != -1) {
            bestSlot = slotID;
            // 保存原始槽位
            if (originSlot == -1) {
                originSlot = mc.player.getInventory().selected;
            }
            // 切换到最佳工具槽位
            if (mc.player.getInventory().selected != slotID) {
                mc.player.getInventory().selected = slotID;
            }
        }
        
        // 更新Y轴水平
        if (mc.player.onGround()) yLevel = (int) Math.floor(mc.player.getY()) - 1;
        
        // 获取方块信息
        getBlockInfo();
        
        // 处理不同模式
        if (telly.getCurrentValue()) {
            if (mc.player.onGround()) {
                airTick = 0;
                blockPos = null;
                enumFacing = null;
                Rotation rotation = new Rotation(mc.player.getYRot(), mc.player.getXRot());
                RotationManager.setRotations(rotation, rotateBackSpeed.getCurrentValue());
            } else {
                if (airTick >= tellyTick.getCurrentValue()) {
                    Rotation rotation = getRotation(blockPos, enumFacing);
                    RotationManager.setRotations(rotation, rotationSpeed.getCurrentValue());
                    place();
                }
                airTick++;
            }
            this.setSuffix("Telly");
        } else {
            if (blockPos == null) {
                RotationManager.setRotations(new Rotation(Mth.wrapDegrees(mc.player.getYRot() - 180), 89.64F), rotationSpeed.getCurrentValue());
            }
            if (onAir() || !snap.getCurrentValue()) {
                Rotation rotation = getRotation(blockPos, enumFacing);
                RotationManager.setRotations(rotation, rotationSpeed.getCurrentValue());
            }
            place();

            this.setSuffix(snap.getCurrentValue() ? "Snap" : "Normal");
        }
    }
    
    public void place() {
        if (!onAir()) return;
        boolean hasRotated = RayCastUtil.overBlock(RotationManager.getRotation(), blockPos);
        if (hasRotated) {
            InteractionResult interactionResult = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(getVec3(blockPos, enumFacing), enumFacing, blockPos, false));
            if (interactionResult == InteractionResult.SUCCESS) mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
    
    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (mc.player.onGround() && !mc.options.keyJump.isDown() && MoveUtils.isMoving() && telly.getCurrentValue())
            event.setJump(true);
    }
    
    public int getYLevel() {
        if (!mc.options.keyJump.isDown() && MoveUtils.isMoving() && mc.player.fallDistance <= 0.25 && telly.getCurrentValue()) {
            return yLevel;
        } else {
            return (int) Math.floor(mc.player.getY()) - 1;
        }
    }
    
    public void getBlockInfo() {
        Vec3 baseVec = mc.player.getEyePosition();
        BlockPos base = BlockPos.containing(baseVec.x, getYLevel(), baseVec.z);
        int baseX = base.getX();
        int baseZ = base.getZ();
        if (isSolidAndNonInteractive(mc.level.getBlockState(base), mc.level, base)) return;
        if (checkBlock(baseVec, base)) {
            return;
        }
        for (int d = 1; d <= 6; d++) {
            if (checkBlock(baseVec, new BlockPos(
                    baseX,
                    getYLevel() - d,
                    baseZ
            ))) {
                return;
            }
            for (int x = 0; x <= d; x++) {
                for (int z = 0; z <= d - x; z++) {
                    int y = d - x - z;
                    for (int rev1 = 0; rev1 <= 1; rev1++) {
                        for (int rev2 = 0; rev2 <= 1; rev2++) {
                            if (checkBlock(baseVec, new BlockPos(baseX + (rev1 == 0 ? x : -x), getYLevel() - y, baseZ + (rev2 == 0 ? z : -z))))
                                return;
                        }
                    }
                }
            }
        }
    }
    
    public boolean isSolidAndNonInteractive(BlockState state, Level level, BlockPos pos) {
        boolean hasCollision = !state.getCollisionShape(level, pos).isEmpty();
        boolean hasNoMenu = state.getMenuProvider(level, pos) == null;
        return hasCollision && hasNoMenu;
    }
    
    private boolean checkBlock(Vec3 baseVec, BlockPos pos) {
        if (!(mc.level.getBlockState(pos).getBlock() instanceof AirBlock) && !(mc.level.getBlockState(pos).getBlock() instanceof WaterlilyBlock)) {
            return false;
        }

        if (pos.getY() > getYLevel()) return false;

        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        for (Direction dir : Direction.values()) {
            Vec3 hit = center.add(new Vec3(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()).scale(0.5));
            Vec3i baseBlock = pos.offset(dir.getNormal());
            BlockPos baseBlockPos = new BlockPos(baseBlock.getX(), baseBlock.getY(), baseBlock.getZ());

            if (!isSolidAndNonInteractive(mc.level.getBlockState(baseBlockPos), mc.level, baseBlockPos)) continue;

            Vec3 relevant = hit.subtract(baseVec);
            if (relevant.lengthSqr() <= 4.5 * 4.5 && relevant.dot(new Vec3(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ())) >= 0) {
                if (dir.getOpposite() == Direction.UP && MoveUtils.isMoving() && !mc.options.keyJump.isDown())
                    continue;
                blockPos = new BlockPos(baseBlock);
                enumFacing = dir.getOpposite();
                return true;
            }
        }
        return false;
    }
    
    @EventTarget
    public void onRender(EventRender e) {
        if (blockPos != null) {
            // 渲染逻辑，这里可以根据需要实现
        }
    }
    
    @EventTarget
    public void onMotion(com.heypixel.heypixelmod.obsoverlay.events.impl.EventMotion e) {
        if (e.getType() == EventType.PRE && safeWalk.getCurrentValue() && !telly.getCurrentValue()) {
            mc.options.keyShift.setDown(mc.player.onGround() && SafeWalk.isOnBlockEdge(0.3F));
        }
    }
    
    @EventTarget
    public void onUpdateHeldItem(EventUpdateHeldItem e) {
        if (e.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND && originalItem != null) {
            e.setItem(originalItem);
        }
    }
    
    public Rotation getRotation(BlockPos pos, Direction direction) {
        Rotation rotations = onAir() ? RotationUtils.calculate(pos, direction) : RotationUtils.calculate(pos.getCenter());
        Rotation reverseYaw = new Rotation(Mth.wrapDegrees(mc.player.getYRot() - 180), rotations.getPitch());
        boolean hasRotated = RayCastUtil.overBlock(reverseYaw, pos);
        if (hasRotated) return reverseYaw;
        else return rotations;
    }
    
    private boolean onAir() {
        Vec3 baseVec = mc.player.getEyePosition();
        BlockPos base = BlockPos.containing(baseVec.x, getYLevel(), baseVec.z);
        return mc.level.getBlockState(base).getBlock() instanceof AirBlock || mc.level.getBlockState(base).getBlock() instanceof WaterlilyBlock;
    }
    
    public static Vec3 getVec3(BlockPos pos, Direction face) {
        double x = (double) pos.getX() + 0.5;
        double y = (double) pos.getY() + 0.5;
        double z = (double) pos.getZ() + 0.5;
        if (face != Direction.UP && face != Direction.DOWN) {
            y += 0.08;
        } else {
            x += MathUtils.getRandomDoubleInRange(0.3, -0.3);
            z += MathUtils.getRandomDoubleInRange(0.3, -0.3);
        }

        if (face == Direction.WEST || face == Direction.EAST) {
            z += MathUtils.getRandomDoubleInRange(0.3, -0.3);
        }

        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += MathUtils.getRandomDoubleInRange(0.3, -0.3);
        }

        return new Vec3(x, y, z);
    }
    
    public static boolean isValidStack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof BlockItem) || stack.getCount() <= 1) {
            return false;
        } else if (!InventoryUtils.isItemValid(stack)) {
            return false;
        } else {
            String string = stack.getDisplayName().getString();
            if (string.contains("Click") || string.contains("点击")) {
                return false;
            } else if (stack.getItem() instanceof ItemNameBlockItem) {
                return false;
            } else {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (block instanceof FlowerBlock) {
                    return false;
                } else if (block instanceof BushBlock) {
                    return false;
                } else if (block instanceof FungusBlock) {
                    return false;
                } else if (block instanceof CropBlock) {
                    return false;
                } else {
                    return !(block instanceof SlabBlock) && !blacklistedBlocks.contains(block);
                }
            }
        }
    }
    
    // 安全行走辅助类
    private static class SafeWalk {
        public static boolean isOnBlockEdge(float offset) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return false;
            
            double x = mc.player.getX();
            double z = mc.player.getZ();
            
            return (Math.abs(x - Math.floor(x)) < offset || Math.abs(x - Math.ceil(x)) < offset) &&
                   (Math.abs(z - Math.floor(z)) < offset || Math.abs(z - Math.ceil(z)) < offset);
        }
    }
    
    // 辅助方法：检查下方是否有方块
    private boolean doesNotContainBlock2(int offset) {
        return BlockHelper.blockRelativeToPlayer(
            this.offset.x, 
            -offset + this.offset.y, 
            this.offset.z
        ) == Blocks.AIR;
    }
    
    // 方块辅助类
    private static class BlockHelper {
        public static net.minecraft.world.level.block.Block blockAheadOfPlayer(double distance, double yOffset) {
            Minecraft mc = Minecraft.getInstance();
            Vec3 playerPos = mc.player.position().add(0, mc.player.getEyeHeight(), 0);
            Vec3 lookDir = mc.player.getLookAngle().normalize();
            Vec3 targetPos = playerPos.add(lookDir.x * distance, yOffset, lookDir.z * distance);
            
            BlockPos blockPos = BlockPos.containing(targetPos);
            return mc.level.getBlockState(blockPos).getBlock();
        }
        
        public static net.minecraft.world.level.block.Block blockRelativeToPlayer(double x, double y, double z) {
            Minecraft mc = Minecraft.getInstance();
            BlockPos blockPos = BlockPos.containing(
                mc.player.position().x + x,
                mc.player.position().y + y,
                mc.player.position().z + z
            );
            return mc.level.getBlockState(blockPos).getBlock();
        }
    }
}
