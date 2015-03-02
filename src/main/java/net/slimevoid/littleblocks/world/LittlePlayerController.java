package net.slimevoid.littleblocks.world;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.slimevoid.littleblocks.core.LittleBlocks;
import net.slimevoid.littleblocks.core.lib.BlockUtil;
import net.slimevoid.littleblocks.core.lib.ConfigurationLib;
import net.slimevoid.littleblocks.core.lib.PacketLib;
import net.slimevoid.littleblocks.items.ItemLittleBlocksWand;
import net.slimevoid.littleblocks.items.wand.EnumWandAction;

public class LittlePlayerController extends PlayerControllerMP {
	
    private Minecraft              mc;
    private WorldSettings.GameType currentGameType;

    public LittlePlayerController(Minecraft client, NetHandlerPlayClient clientHandler) {
        super(client, clientHandler);
        this.currentGameType = WorldSettings.GameType.SURVIVAL;
        this.mc = client;
    }

    @Override
    public void setGameType(WorldSettings.GameType gameType) {
        this.currentGameType = gameType;
        this.currentGameType.configurePlayerCapabilities(this.mc.thePlayer.capabilities);
    }

    public static void clickBlockCreative(Minecraft client, LittlePlayerController controller, BlockPos pos, EnumFacing side) {
        if (!((World) LittleBlocks.proxy.getLittleWorld(client.theWorld,
                                                        false)).extinguishFire(client.thePlayer,
                                                                               pos,
                                                                               side)) {
            controller.onPlayerDestroyBlock(pos,
                                            side);
        }
    }

    //@Override
    public void onPlayerRightClickFirst(EntityPlayerSP entityplayer, WorldClient world, ItemStack itemstack, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        boolean flag = true;
        int stackSize = itemstack != null ? itemstack.stackSize : 0;
        if (this.onPlayerRightClick(
                entityplayer,
                world,
                itemstack,
                pos,
                side,
                new Vec3(
                        hitX,
                        hitY,
                        hitZ))) {
            flag = false;
            entityplayer.swingItem();
        }
        if (itemstack == null) {
            return;
        }

        if (itemstack.stackSize == 0) {
            entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = null;
        } else if (itemstack.stackSize != stackSize || this.isInCreativeMode()) {
            FMLClientHandler.instance().getClient().entityRenderer.itemRenderer.resetEquippedProgress();
        }

        if (flag) {
            ItemStack itemstack1 = entityplayer.inventory.getCurrentItem();
            if (itemstack1 != null && this.sendUseItem(entityplayer,
                                                       world,
                                                       itemstack1)) {
                FMLClientHandler.instance().getClient().entityRenderer.itemRenderer.resetEquippedProgress2();
            }
        }
    }

    @Override
    public boolean sendUseItem(EntityPlayer entityplayer, World world, ItemStack itemstack) {
        PacketLib.sendItemUse(world,
                              entityplayer,
                              new BlockPos(
                                      -1,
                                      -1,
                                      -1),
                              255,
                              itemstack);
        int i = itemstack.stackSize;
        ItemStack itemstack1 = itemstack.useItemRightClick(world,
                                                           entityplayer);

        if (itemstack1 == itemstack
            && (itemstack1 == null || itemstack1.stackSize == i)) {
            return false;
        } else {
            entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = itemstack1;

            if (itemstack1.stackSize <= 0) {
                entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = null;
                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(entityplayer, itemstack1));
            }

            return true;
        }
    }

    @Override
    public boolean onPlayerRightClick(EntityPlayerSP entityplayer, WorldClient world, ItemStack itemstack, BlockPos pos, EnumFacing side, Vec3 hitAt) {
        float xOffset = (float) hitAt.xCoord;
        float yOffset = (float) hitAt.yCoord;
        float zOffset = (float) hitAt.zCoord;
        boolean flag = false;
        if (itemstack != null && itemstack.getItem() != null
            && itemstack.getItem().onItemUseFirst(itemstack,
                                                  entityplayer,
                                                  world,
                                                  pos,
                                                  side,
                                                  xOffset,
                                                  yOffset,
                                                  zOffset)) {
            return true;
        }

        if (!entityplayer.isSneaking()
            || (entityplayer.getHeldItem() == null || entityplayer.getHeldItem().getItem().doesSneakBypassUse/* shouldPassSneakingClickToBlock */(world,
                                                                                                                                                  pos,
                                                                                                                                                  entityplayer))) {
            IBlockState state = world.getBlockState(pos);
            flag = state.getBlock().onBlockActivated(
                    world,
                    pos,
                    state,
                    entityplayer,
                    side,
                    xOffset,
                    yOffset,
                    zOffset);
        }

        if (!flag && itemstack != null
            && itemstack.getItem() instanceof ItemBlock) {
            ItemBlock itemblock = (ItemBlock) itemstack.getItem();

            if (!itemblock.canPlaceBlockOnSide(
                    world,
                    pos,
                    side,
                    entityplayer,
                    itemstack)) {
                return false;
            }
        }

        //System.out.println("Controller: " + world);
        PacketLib.sendBlockPlace(world,
                                 entityplayer,
                                 pos,
                                 side.getIndex(),
                                 xOffset,
                                 yOffset,
                                 zOffset);

        if (flag) {
            return true;
        } else if (itemstack == null) {
            return false;
        } else if (this.currentGameType.isCreative()) {
            int damage = itemstack.getItemDamage();
            int stackSize = itemstack.stackSize;
            boolean placedOrUsed = itemstack.onItemUse(
                    entityplayer,
                    world,
                    pos,
                    side,
                    xOffset,
                    yOffset,
                    zOffset);
            itemstack.setItemDamage(damage);
            itemstack.stackSize = stackSize;
            return placedOrUsed;
        } else {
            if (!itemstack.onItemUse(
                    entityplayer,
                    world,
                    pos,
                    side,
                    xOffset,
                    yOffset,
                    zOffset)) {
                return false;
            }
            if (itemstack.stackSize <= 0) {
                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(entityplayer, itemstack));
            }
            return true;
        }
    }

    @Override
    public boolean clickBlock(BlockPos pos, EnumFacing side) {
        World littleWorld = (World) LittleBlocks.proxy.getLittleWorld(mc.theWorld,
                                                                      false);
        if (!BlockUtil.isLittleChunk(littleWorld,
                                     pos)) {
            return false;
        }
        if (this.currentGameType.isCreative()) {
            PacketLib.sendBlockClick(pos,
                                     side.getIndex());
            clickBlockCreative(this.mc,
                               this,
                               pos,
                               side);
        } else {
            PacketLib.sendBlockClick(pos,
                                     side.getIndex());
            Block block = littleWorld.getBlockState(pos).getBlock();
            boolean flag = block.getMaterial() != Material.air;
            if (flag) {
                block.onBlockClicked(littleWorld,
                                     pos,
                                     this.mc.thePlayer);
                this.onPlayerDestroyBlock(pos,
                                          side);
            }
        }
        return true;
    }

    @Override
    public boolean onPlayerDestroyBlock(BlockPos pos, EnumFacing side) {
        ItemStack stack = this.mc.thePlayer.getHeldItem();
        if (stack != null && stack.getItem() != null
            && stack.getItem().onBlockStartBreak(stack,
                                                 pos,
                                                 this.mc.thePlayer)) {
            return false;
        }
        if (this.currentGameType.isCreative()
            && this.mc.thePlayer.getHeldItem() != null
            && this.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            return false;
        } else {
            World littleWorld = (World) LittleBlocks.proxy.getLittleWorld(this.mc.theWorld,
                                                                          false);
            IBlockState littleState = littleWorld.getBlockState(pos);
            Block littleBlock = littleState.getBlock();

            if (littleBlock.getMaterial() == Material.air) {
                return false;
            } else {
                littleWorld.playAuxSFX(2001,
                                       pos,
                                       Block.getIdFromBlock(ConfigurationLib.littleChunk/*littleBlock)
                                               + (littleWorld.getBlockMetadata(x,
                                                                               y,
                                                                               z) << 12*/));

                boolean blockIsRemoved = littleBlock.removedByPlayer(littleWorld,
                        pos,
                                                                     mc.thePlayer,
                        true);

                if (this.mc.thePlayer.getHeldItem() != null
                    && this.mc.thePlayer.getHeldItem().getItem() instanceof ItemLittleBlocksWand) {
                    if (EnumWandAction.getWandAction().equals(EnumWandAction.DESTROY_LB)) {
                        littleWorld.setBlockToAir(pos);
                        blockIsRemoved = true;
                    }
                }
                if (blockIsRemoved) {
                    littleBlock.onBlockDestroyedByPlayer(littleWorld,
                                                         pos,
                                                         littleState);
                }

                if (!this.currentGameType.isCreative()) {
                    ItemStack itemstack = this.mc.thePlayer.getCurrentEquippedItem();

                    if (itemstack != null) {
                        itemstack.onBlockDestroyed(
                                littleWorld,
                                littleBlock,
                                pos,
                                this.mc.thePlayer);

                        if (itemstack.stackSize == 0) {
                            this.mc.thePlayer.destroyCurrentEquippedItem();
                        }
                    }
                }

                return blockIsRemoved;
            }
        }
    }
}