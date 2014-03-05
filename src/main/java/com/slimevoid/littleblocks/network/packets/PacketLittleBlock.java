package com.slimevoid.littleblocks.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

import com.slimevoid.library.nbt.NBTHelper;
import com.slimevoid.library.network.PacketIds;
import com.slimevoid.library.network.PacketUpdate;
import com.slimevoid.littleblocks.core.lib.CommandLib;
import com.slimevoid.littleblocks.core.lib.CoreLib;

public class PacketLittleBlock extends PacketUpdate {

    private ItemStack itemStack;

    @Override
    public void writeData(DataOutputStream data) throws IOException {
        super.writeData(data);
        if (this.itemStack == null) {
            data.writeBoolean(false);
        } else {
            data.writeBoolean(true);
            NBTHelper.writeItemStack(
                                  data,this.itemStack);
        }
    }

    @Override
    public void readData(DataInputStream data) throws IOException {
        super.readData(data);
        if (data.readBoolean() == true) {
            this.itemStack = NBTHelper.readItemStack(data);
        }
    }

    public PacketLittleBlock() {
        super(PacketIds.TILE);
        this.setChannel(CoreLib.MOD_CHANNEL);
    }

    /**
     * CLICK BLOCK
     */
    public PacketLittleBlock(int x, int y, int z, int face) {
        this();
        this.setPosition(x,
                         y,
                         z,
                         face);
        this.setCommand(CommandLib.BLOCK_CLICKED);
    }

    /**
     * ACTIVATE BLOCK
     */
    public PacketLittleBlock(int x, int y, int z, int direction, ItemStack itemStack, float xOff, float yOff, float zOff) {
        this();
        this.setPosition(x,
                         y,
                         z,
                         direction);
        this.setHitVectors(xOff,
                           yOff,
                           zOff);
        this.itemStack = itemStack != null ? itemStack.copy() : null;
        this.setCommand(CommandLib.BLOCK_ACTIVATED);
    }

    public int getDirection() {
        return this.side;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public boolean targetExists(World world) {
        return false;
    }
}
