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
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class MachineSpaceRipper extends BlockDummyable {

	public MachineSpaceRipper() {
		super(Material.iron);
	}

	// Y1,Y2,Z1,Z2,X1,X2
	@Override
	public int[] getDimensions() {
		return new int[] { 1, 0, 4, 0, 0, 0 };
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
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, player, itemStack);

		int i = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		// The direction the player is facing, for offsetting the block away from the player
		ForgeDirection facingDir = ForgeDirection.NORTH;
		ForgeDirection placedSide = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));

		if(placedSide == ForgeDirection.UP || placedSide == ForgeDirection.DOWN) {
			if(i == 0) facingDir = ForgeDirection.getOrientation(2);
			if(i == 1) facingDir = ForgeDirection.getOrientation(5);
			if(i == 2) facingDir = ForgeDirection.getOrientation(3);
			if(i == 3) facingDir = ForgeDirection.getOrientation(4);
		} else {
			facingDir = placedSide;
		}
		System.out.println(facingDir + " : (" + facingDir.offsetX + ", " + facingDir.offsetY + ", " + facingDir.offsetZ + ")");
		System.out.println(placedSide + " : (" + placedSide.offsetX + ", " + placedSide.offsetY + ", " + placedSide.offsetZ + ")");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		}
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}
}
