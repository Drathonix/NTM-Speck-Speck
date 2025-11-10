package com.hbm.items.special;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

import java.math.BigInteger;

public class ItemSRCrystal extends Item {
	public final long powerPerTick;
	public final BigInteger stabilityLimit;
	public final int fluidStacks;
	public final int fluidPerStack;
	public final int itemTypes;
	public final int itemStacks;
	public ItemSRCrystal(long powerPerTick, BigInteger stabilityLimit, int fluidStacks, int fluidPerStack, int itemTypes, int itemStacks) {
		this.powerPerTick = powerPerTick;
		this.stabilityLimit = stabilityLimit;
		this.fluidStacks = fluidStacks;
		this.fluidPerStack = fluidPerStack;
		this.itemTypes = itemTypes;
		this.itemStacks = itemStacks;
	}
	public ItemSRCrystal(long powerPerTick, long stabilityLimit, int fluidStacks, int fluidPerStack, int itemTypes, int itemStacks) {
		this(powerPerTick, BigInteger.valueOf(stabilityLimit),fluidStacks,fluidPerStack,itemTypes,itemStacks);
	}

	public static Item desh_sr_crystal;
	public static Item bismith_sr_crystal;
	public static Item schrabidium_sr_crystal;
	public static Item chlorophyte_sr_crystal;
	public static Item spark_sr_crystal;
	public static Item digamma_sr_crystal;
	public static Item hyper6d_sr_crystal;

	public static void init(){
		desh_sr_crystal = new ItemSRCrystal(1000,100_000_000L,0,0,0,0).setUnlocalizedName("desh_sr_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":desh_sr_crystal");
		bismith_sr_crystal = new ItemSRCrystal(1000*4,100_000_000L*1000L,4,16000,4,4).setUnlocalizedName("bismith_sr_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bismith_sr_crystal");
		schrabidium_sr_crystal = new ItemSRCrystal(1000*4*5,100_000_000L*1000L*1000L,8,32000,8,8).setUnlocalizedName("schrabidium_sr_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":schrabidium_sr_crystal");
		chlorophyte_sr_crystal = new ItemSRCrystal(1000*4*5*6,100_000_000L*1000L*1000L*1000L,8,64000,8,32).setUnlocalizedName("chlorophyte_sr_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chlorophyte_sr_crystal");
		spark_sr_crystal = new ItemSRCrystal(1000*4*5*6*7,BigInteger.valueOf(100_000_000L*1000L*1000L*1000L).multiply(BigInteger.valueOf(10L)),16,128000,8,512).setUnlocalizedName("spark_sr_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":spark_sr_crystal");
		digamma_sr_crystal = new ItemSRCrystal(1000*4*5*6*7*8,BigInteger.valueOf(100_000_000L*1000L*1000L*1000L).multiply(BigInteger.valueOf(1000L)),32,512000,16,512*512).setUnlocalizedName("digamma_sr_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":digamma_sr_crystal");
		hyper6d_sr_crystal = new ItemSRCrystal(1000*4*5*6*7*8,BigInteger.valueOf(100_000_000L*1000L*1000L*1000L).multiply(BigInteger.valueOf(100000L)),32,512000*4,16,512*512*512).setUnlocalizedName("hyper6d_sr_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":hyper6d_sr_crystal");
	}

	public static void register() {
		GameRegistry.registerItem(desh_sr_crystal,desh_sr_crystal.getUnlocalizedName());
		GameRegistry.registerItem(bismith_sr_crystal,bismith_sr_crystal.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_sr_crystal,schrabidium_sr_crystal.getUnlocalizedName());
		GameRegistry.registerItem(chlorophyte_sr_crystal,chlorophyte_sr_crystal.getUnlocalizedName());
		GameRegistry.registerItem(spark_sr_crystal,spark_sr_crystal.getUnlocalizedName());
		GameRegistry.registerItem(digamma_sr_crystal,digamma_sr_crystal.getUnlocalizedName());
		GameRegistry.registerItem(hyper6d_sr_crystal,hyper6d_sr_crystal.getUnlocalizedName());
	}
}
