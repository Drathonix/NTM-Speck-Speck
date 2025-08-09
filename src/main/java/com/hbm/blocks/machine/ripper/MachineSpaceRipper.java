package com.hbm.blocks.machine.ripper;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.tileentity.machine.ripper.TileEntitySpaceRipper;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class MachineSpaceRipper extends BlockDummyable {

	public MachineSpaceRipper() {
		super(Material.iron);
	}

	// reminder, if the machine is a solid brick, get dimensions will already
	// handle it without the need to use fillSapce
	// the order is up, down, forward, backward, left, right
	// x is for left(-)/right(+), z is for forward(+)/backward(-), y you already
	// know
	@Override
	public int[] getDimensions() {
		return new int[] { 0, 0, 2, 0, 1, 0 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntitySpaceRipper();
		if(meta >= 6) return new TileEntityProxyCombo(false, false, false);
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		}
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}
}
