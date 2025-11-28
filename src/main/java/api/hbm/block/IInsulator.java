package api.hbm.block;

/**
 * Represents a block that has an insulating effect that reduces the heat passivley lost by {@link api.hbm.tile.IHeatable} blocks
 * @author Jack Andersen
 */
public interface IInsulator {
	/**
	 * The efficiency of the insulator against radiative cooling.
	 * @return a float between 0 and 1, with 0 meaning no heat loss reduction and 1 meaning a complete prevention of heat loss.
	 */
	float radiativeCoolingLossPrevention();
}
