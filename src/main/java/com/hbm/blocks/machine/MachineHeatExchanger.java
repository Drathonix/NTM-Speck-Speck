package com.hbm.blocks.machine;

import api.hbm.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDysonConverterAnatmogenesis;
import com.hbm.tileentity.machine.TileEntityHeatExchanger;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

/**
 * Block for {@link com.hbm.tileentity.machine.TileEntityHeatExchanger}
 * @author Jack Andersen
 */
public class MachineHeatExchanger extends BlockContainer implements ILookOverlay, IToolable {

	public MachineHeatExchanger(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityHeatExchanger();
	}


	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x,y,z);
		if(!(te instanceof TileEntityHeatExchanger)) return;

		TileEntityHeatExchanger hex = (TileEntityHeatExchanger) te;

		List<String> text = new ArrayList<String>();

		text.add("Cold Tank: " + I18nUtil.resolveKey(hex.tanks[1].getTankType().getConditionalName()) + ": " + hex.tanks[1].getFill() + "mB/" + hex.tanks[1].getMaxFill() + "mB");
		text.add("Hot Tank: " + I18nUtil.resolveKey(hex.tanks[0].getTankType().getConditionalName()) + ": " + hex.tanks[0].getFill() + "mB/" + hex.tanks[0].getMaxFill() + "mB");
		text.add("Exchanger Heat: " + hex.heatEnergy + "TU");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;

		if(world.isRemote) return true;

		TileEntity te = world.getTileEntity(x,y,z);
		if(!(te instanceof TileEntityHeatExchanger)) return false;

		TileEntityHeatExchanger hex = (TileEntityHeatExchanger) te;
		hex.changeStep();
		player.addChatComponentMessage(new ChatComponentText("Changed hot tank type to ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendSibling(new ChatComponentTranslation(hex.tanks[0].getTankType().getConditionalName())).appendSibling(new ChatComponentText("!")));

		return true;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
			return true;

		if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IItemFluidIdentifier) {
			TileEntity te = world.getTileEntity(x,y,z);
			if(!(te instanceof TileEntityHeatExchanger)) return true;

			TileEntityHeatExchanger hex = (TileEntityHeatExchanger) te;

			FluidType hotType = ((IItemFluidIdentifier) player.getHeldItem().getItem()).getType(world, x, y, z, player.getHeldItem());
			if(hotType.hasTrait(FT_Coolable.class)) {
				FluidType coldType = hotType.getTrait(FT_Coolable.class).coolsTo;
				if(!coldType.hasTrait(FT_Heatable.class) || coldType.getTrait(FT_Heatable.class).getFirstStep().typeProduced != hotType) {
					return false;
				}
				hex.step=0;
				hex.tanks[0].setTankType(hotType);
				hex.tanks[1].setTankType(coldType);
				hex.recalculateConsts();
				player.addChatComponentMessage(new ChatComponentText("Changed cold tank type to ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendSibling(new ChatComponentTranslation(hotType.getConditionalName())).appendSibling(new ChatComponentText("!")));
				player.addChatComponentMessage(new ChatComponentText("Changed hot tank type to ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendSibling(new ChatComponentTranslation(hotType.getConditionalName())).appendSibling(new ChatComponentText("!")));
				hex.markChanged();
			}
			return true;
		}

		return false;
	}
}

