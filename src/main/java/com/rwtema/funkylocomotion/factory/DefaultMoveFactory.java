package com.rwtema.funkylocomotion.factory;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import framesapi.IMoveFactory;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DefaultMoveFactory implements IMoveFactory {
    public static NBTTagCompound getBBTag(AxisAlignedBB bb, BlockPos pos) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("x1", bb.minX - pos.x);
        tag.setDouble("y1", bb.minY - pos.y);
        tag.setDouble("z1", bb.minZ - pos.z);
        tag.setDouble("x2", bb.maxX - pos.x);
        tag.setDouble("y2", bb.maxY - pos.y);
        tag.setDouble("z2", bb.maxZ - pos.z);
        return tag;
    }

    @Override
    public NBTTagCompound destroyBlock(World world, BlockPos pos) {
        Chunk chunk = world.getChunkFromBlockCoords(pos.x, pos.z);
        Block b = chunk.getBlock(pos.x & 15, pos.y, pos.z & 15);
        int meta = chunk.getBlockMetadata(pos.x & 15, pos.y, pos.z & 15);

        NBTTagCompound tag = new NBTTagCompound();

        if (b == Blocks.air) return tag;

        String name = Block.blockRegistry.getNameForObject(b);

        tag.setString("Block", name);
        if (meta != 0)
            tag.setByte("Meta", (byte) meta);

        saveTile(pos, chunk, tag);

        BlockHelper.silentClear(chunk, pos);

        return tag;
    }

    protected NBTTagCompound saveTile(BlockPos pos, Chunk chunk, NBTTagCompound tag) {
        TileEntity tile = chunk.getTileEntityUnsafe(pos.x & 15, pos.y, pos.z & 15);
        if (tile != null) {
            NBTTagCompound tileTag = new NBTTagCompound();
            tile.writeToNBT(tileTag);
            tag.setTag("Tile", tileTag);
            chunk.removeTileEntity(pos.x & 15, pos.y, pos.z & 15);
			return tileTag;
        }
		return null;
    }

    @Override
    public boolean recreateBlock(World world, BlockPos pos, NBTTagCompound tag) {
        Block block = Block.getBlockFromName(tag.getString("Block"));

        if (block == null)
            block = Blocks.air;

        int meta = tag.getByte("Meta");

        Chunk chunk = world.getChunkFromBlockCoords(pos.x, pos.z);

        BlockHelper.silentSetBlock(chunk, pos, block, meta);

        loadTile(pos, tag, chunk);

        return true;
    }

    protected TileEntity loadTile(BlockPos pos, NBTTagCompound tag, Chunk chunk) {
        if (tag.hasKey("Tile", 10)) {
            NBTTagCompound tileTag = tag.getCompoundTag("Tile");
            tileTag.setInteger("x", pos.x);
            tileTag.setInteger("y", pos.y);
            tileTag.setInteger("z", pos.z);

            TileEntity tile = TileEntity.createAndLoadEntity(tileTag);
            if (tile != null) {
                chunk.addTileEntity(tile);
				return tile;
            }
        }

		return null;
    }


}
