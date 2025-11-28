package api.hbm.tile;

import api.hbm.block.IInsulator;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Iterator;

/**
 * Rework of the IHeatSource interface that implements some universal behavior internally.
 * @author Jack Andersen
 */
public interface IHeatable extends IInsulator {
	/**
	 * Heatable blocks absorb heat so they also block loss due to radiative cooling.
	 * @return 1 to block heat loss.
	 */
	@Override
	default float radiativeCoolingLossPrevention(){
		return 1;
	}

	int getHeatStored();
	void setHeat(int heat);
	default boolean canAbsorbFrom(int x, int y, int z){
		return false;
	}
	default boolean canDistributeTo(int x, int y, int z){
		return true;
	}

	/**
	 * Syntactic sugar for {@link #useUpHeat(int)}
	 * @param heat the heat to try to use up.
	 * @return amount of heat actually consumed.
	 */
	default int consumeHeat(int heat) {
		return useUpHeat(heat);
	}

	/**
	 * Uses up heat.
	 * @param heat to try to use up.
	 * @return amount of heat actually consumed.
	 */
	default int useUpHeat(int heat) {
		int heatEnergy = getHeatStored();
		int cons = Math.min(heatEnergy,heat);
		setHeat(heat-cons);
		return cons;
	}

	/**
	 * Increases the machine's heat.
	 * @param heat the heat to receive.
	 */
	default void receiveHeat(int heat) {
		setHeat(getHeatStored()+heat);
	}

	/**
	 * Absorbs heat from another block.
	 * @param world the world to target blocks in.
	 * @param conductance the percentage of heat to absorb from the target blocks.
	 * @param target the target blocks to extract heat from.
	 */
	default void absorbHeat(World world, float conductance, BlockPos target) {
		absorbHeat(world,conductance,target.getX(),target.getY(),target.getZ());
	}


	/**
	 * Absorbs heat from another block.
	 * @param world the world to target blocks in.
	 * @param conductance the percentage of heat to absorb from the target blocks.
	 * @param x the x target;
	 * @param y the y target;
	 * @param z the z target;
	 */
	default void absorbHeat(World world, float conductance, int x, int y, int z) {
		TileEntity con = world.getTileEntity(x,y,z);
		if(con instanceof IHeatable) {
			IHeatable source = (IHeatable) con;
			int cons = (int)(source.getHeatStored()*conductance);
			receiveHeat(cons);
			source.useUpHeat(cons);
		}
	}

	default void balanceHeat(World world, float conductance, BlockPos target) {
		balanceHeat(world,conductance,target.getX(),target.getY(),target.getZ());
	}

	default void balanceHeat(World world, float conductance, int x, int y, int z){
		if(!canAbsorbFrom(x,y,z)){
			return;
		}
		TileEntity con = world.getTileEntity(x,y,z);
		if(con instanceof IHeatable) {
			IHeatable source = (IHeatable) con;
			// Note: diff will always be less than source heat when positive.
			int diff = source.getHeatStored()-this.getHeatStored();
			if(diff > 0){
				// This is the amount that needs to be transferred to establish balance.
				diff = diff/2;
				// Adjust for conductance speed.
				diff = (int)(diff*conductance);
				receiveHeat(diff);
				source.useUpHeat(diff);
			}
		}
	}

	/**
	 * Loses heat due to radiative cooling for all sides.
	 * @param world the world to locate insulator in.
	 * @param lossPercentage the base percentage of total heat lost collectively.
	 * @param sides the block positions to check for insulator.
	 */
	default void radiateAllSides(World world, float lossPercentage, DirPos... sides) {
		float mod = 0;
		int count = 0;
		for (DirPos side : sides) {
			if(world.getBlock(side.getX(),side.getY(),side.getZ()) instanceof IInsulator insulator){
				mod+=insulator.radiativeCoolingLossPrevention();
				count++;
			} else if(world.getTileEntity(side.getX(),side.getY(),side.getZ()) instanceof IInsulator insulator){
				mod+=insulator.radiativeCoolingLossPrevention();
				count++;
			}
		}
		if(count > 1){
			mod/=count;
		}
		mod = 1-mod;
		float multiplier = lossPercentage*mod;
		setHeat((int)(getHeatStored()*multiplier));
	}

	/**
	 * Loses heat due to radiative cooling for one side.
	 * @param world the world to locate insulator in.
	 * @param sideLossPercentage the base percentage of total heat lost on this side only..
	 * @param x side position.
	 * @param y side position.
	 * @param z side positions.
	 */
	default void radiate(World world, float sideLossPercentage, int x, int y, int z){
		float mod = 0;
		if(world.getBlock(x,y,z) instanceof IInsulator insulator){
			mod=insulator.radiativeCoolingLossPrevention();
		} else if(world.getTileEntity(x,y,z) instanceof IInsulator insulator){
			mod=insulator.radiativeCoolingLossPrevention();
		}
		mod = 1-mod;
		float multiplier = sideLossPercentage*mod;
		setHeat((int)(getHeatStored()*multiplier));
	}
}
