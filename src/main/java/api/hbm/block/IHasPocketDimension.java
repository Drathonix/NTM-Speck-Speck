package api.hbm.block;

import com.hbm.calc.Location;
import com.hbm.tileentity.machine.ripper.PocketDimension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO @Drathonix
 * Allows a block to be linked to a pocket dimension. Using an {@link RiftLinkerItem}
 * @author Jack Andersen
 */
public interface IHasPocketDimension {
	void linkToPocketDimension(@Nonnull PocketDimension dimension, @Nonnull Location rift);
	@Nullable PocketDimension getPocketDimension();
	@Nullable Location getRift();
}
