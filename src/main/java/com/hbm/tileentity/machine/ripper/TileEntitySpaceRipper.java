package com.hbm.tileentity.machine.ripper;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.calc.Location;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * {@link com.hbm.blocks.machine.ripper.MachineSpaceRipper}
 * @author Jack Andersen
 */
public class TileEntitySpaceRipper extends TileEntitySRBase implements IEnergyReceiverMK2 {
	public static final long MAX_POWER = 100_000_000;
	public static final long FIRE_POWER = 50_000_000;
	public static final int DISTANCE = 14;

	public long power = 0;

	public TileEntitySpaceRipper() {
		super(2);
	}

	@Override
	public String getName() {
		return "container.spaceRipper";
	}

	@Override
	public void updateEntity() {

	}

	public void tryFire(EntityPlayer player){
		if(riftPosition != null){
			return;
		}
		if(!hasOpposingCannon()){
			// TODO inform.
			return;
		}
		TileEntitySpaceRipper opposite = getOpposingCannonPosition().getTileEntity();
		long powerRequired = FIRE_POWER/2;
		if(getPower() < powerRequired){
			// TODO inform.
			return;
		}
		if(opposite.getPower() < powerRequired){
			// TODO inform.
			return;
		}
		if(!isSpaceEmpty()){
			//TODO inform.
			return;
		}
	}

	public <T> @Nullable T forEachVoidableBlock(Function<Location,T> func){
		ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata());
		Location offset = new Location(worldObj,dir.offsetX,dir.offsetY,dir.offsetZ);
		for (int i = 0; i < 7; i++) {
			int ym = Math.min(i/2,2);
			for(int y = -ym; y <= ym; y++){
				T t = func.apply(offset.multiply(i).add(xCoord,yCoord,zCoord));
				if(t != null){
					return t;
				}
				t = func.apply(offset.multiply(DISTANCE-i).add(xCoord,yCoord,zCoord));
				if(t != null){
					return t;
				}
			}
		}
		return null;
	}

	public boolean isSpaceEmpty(){
		return forEachVoidableBlock(location-> location.isAir() ? null : location) == null;
	}

	public Location getOpposingCannonPosition(){
		ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata());
		Location offset = new Location(worldObj,dir.offsetX*DISTANCE,dir.offsetY*DISTANCE,dir.offsetZ*DISTANCE);
		return offset.add(xCoord,yCoord,zCoord);
	}

	public boolean hasOpposingCannon(){
		ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata());
		Location pos = getOpposingCannonPosition();
		ForgeDirection oppositeDir = ForgeDirection.getOrientation(pos.getBlockMetadata());
		return dir.getOpposite() == oppositeDir && pos.getTileEntity() instanceof TileEntitySpaceRipper;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power=power;
	}

	@Override
	public long getMaxPower() {
		return MAX_POWER;
	}
}
