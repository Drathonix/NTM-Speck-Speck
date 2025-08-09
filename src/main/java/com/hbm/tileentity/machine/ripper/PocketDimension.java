package com.hbm.tileentity.machine.ripper;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.calc.Location;
import com.hbm.util.Tuple;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * A Pocket Dimension capable of storing "infinite" amounts of energy given that it can be stored successfully.
 * @author Jack Andersen
 */
public class PocketDimension {
	// The tier 1 storage stores 100MHE
	public static final long POWER_BASE = 100_000_000;
	//Each tier above 1 increases power storage by 1000x.
	public static final long POWER_MULT_PER_TIER_ABOVE_ONE = 1_000;
	//Each stabilizer tier multiplies power storage by 1.05^X
	public static final float STABILIZER_POWER_MULT_BASE = 1.05F;

	public static final int ITEMS_BASE = 4;
	public static final int ITEM_STACKS_BASE = 4;
	public static final int ITEM_MULTIPLIER = 4;
	public static final int ITEM_STACKS_MULTIPLIER = 4;

	public static final int FLUID_MB_BASE = 16000;
	public static final int FLUID_STACKS_BASE = 1;
	public static final int FLUID_MULTIPLIER = 4;
	public static final int FLUID_STACKS_MULTIPLIER = 4;

	public static final Map<String,PocketDimension> dimensions = new HashMap<>();


	// The pocket dimension will violently destabilize if the reactor power exceeds 10 times the power limit.
	public static final float INSTABILITY_DETONATION_POINT = 10;

	public static long calculateStablePowerLimit(int tier, int stabilizerTiers) {
		return (long) (POWER_BASE * ((tier - 1) * POWER_MULT_PER_TIER_ABOVE_ONE) * Math.pow(STABILIZER_POWER_MULT_BASE, stabilizerTiers));
	}

	public static int calculateItemStacksLimit(int tier) {
		return ITEM_STACKS_BASE * ((tier - 1) * ITEM_STACKS_MULTIPLIER);
	}

	public static int calculateItemsCount(int tier) {
		return ITEMS_BASE * ((tier - 1) * ITEM_MULTIPLIER);
	}

	public static int calculateFluidStacksLimit(int tier) {
		return FLUID_STACKS_BASE * ((tier - 1) * FLUID_STACKS_MULTIPLIER);
	}

	public static int calculateFluidCount(int tier) {
		return FLUID_MB_BASE * ((tier - 1) * FLUID_MULTIPLIER);
	}

	public static double calculateInstability(long power, long stablePowerLimit) {
		return power / (double) stablePowerLimit;
	}

	public static boolean shouldExplode(double instability) {
		return instability >= INSTABILITY_DETONATION_POINT;
	}

	public BigInteger powerStored = BigInteger.ZERO;
	// Each pocket dimension has a name and password associated with it.
	public String name;
	// Passwords are not stored in raw text, yes this is a minecraft mod, but like I'm still not storing passes in plain text.
	public String passwordHash;
	public String passwordSalt;

	public List<TileEntitySREnergyTransceiver>[] transceivers = new ArrayList[IEnergyReceiverMK2.ConnectionPriority.values().length];

	public HashSet<Location> rifts = new HashSet<>();

	public PocketDimension(@Nonnull String name) {
		for (int i = 0; i < transceivers.length; i++) {
			transceivers[i] = new ArrayList<>();
		}
		dimensions.put(name, this);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String rawTextPassword) {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		passwordSalt = new String(salt);
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			digest.update(salt);
			passwordHash = new String(digest.digest(rawTextPassword.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean passwordMatches(String rawTextPassword) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			digest.update(passwordSalt.getBytes(StandardCharsets.UTF_8));
			String hash = new String(digest.digest(rawTextPassword.getBytes(StandardCharsets.UTF_8)));
			return hash.equals(passwordHash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the amount of power in the pocket dimension, clamped to long size.
	 */
	public long getPowerLong() {
		return powerStored.longValue();
	}

	/**
	 * Inserts energy into the
	 *
	 * @param power
	 */
	public void insertEnergy(long power) {
		powerStored = powerStored.add(BigInteger.valueOf(power));
	}


	public void removeEnergy(long power) {
		powerStored = powerStored.subtract(BigInteger.valueOf(power)).max(BigInteger.ZERO);
	}

	public void addTransceiver(TileEntitySREnergyTransceiver transceiver, IEnergyReceiverMK2.ConnectionPriority priority) {
		this.transceivers[priority.ordinal()].add(transceiver);
	}

	public void removeTransceiver(TileEntitySREnergyTransceiver transceiver, IEnergyReceiverMK2.ConnectionPriority priority) {
		this.transceivers[priority.ordinal()].remove(transceiver);
	}

	public void onTransceiverSwapPriority(TileEntitySREnergyTransceiver transceiver, IEnergyReceiverMK2.ConnectionPriority priorityBefore, IEnergyReceiverMK2.ConnectionPriority priorityAfter) {
		removeTransceiver(transceiver, priorityBefore);
		addTransceiver(transceiver, priorityAfter);
	}

	public void addRift(Location position){
		rifts.add(position);
	}

	public void removeRift(Location position){
		rifts.remove(position);
	}

	public static @Nullable Tuple.Pair<PocketDimension,Location> getNearestPocketDimensionFromAllNets(World world, double x, double y, double z, double rangeSq){
		Tuple.Pair<PocketDimension,Location> found = null;
		Location l = new Location(world, x, y, z);
		for (PocketDimension value : new ArrayList<>(dimensions.values())) {
			Location rift = value.getNearestRift(world,x,y,z,rangeSq);
			if(rift != null){
				double distSq = rift.distSq(l);
				if(found == null || found.value.distSq(l) > distSq){
					found = new Tuple.Pair<>(value,rift);
				}
			}
		}
		return found;
	}

	public @Nullable Location getNearestRift(World world, double x, double y, double z, double rangeSq){
		Location l = new Location(world, x, y, z);
		Location found = null;
		for (Location rift : rifts) {
			if(rift.world == world){
				double distSq = rift.distSq(l);
				if(found == null || found.distSq(l) > distSq){
					found = rift;
				}
			}
		}
		return found;
	}

	public void tick() {
		for (int i = transceivers.length - 1; i >= 0; i--) {
			List<TileEntitySREnergyTransceiver> receivers = this.transceivers[i];
			for (TileEntitySREnergyTransceiver receiver : receivers) {
				receiver.distributePower();
			}
		}
	}
}

