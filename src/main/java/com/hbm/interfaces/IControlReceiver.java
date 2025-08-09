package com.hbm.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;

import java.util.IdentityHashMap;
import java.util.function.Consumer;

/**
 * For receiving (sort of) complex control data via NBT from clients
 * @author hbm
 */
public interface IControlReceiver {

	public boolean hasPermission(EntityPlayer player);

	public void receiveControl(NBTTagCompound data);
	/* this was the easiest way of doing this without needing to change all 7 quadrillion implementors */
	public default void receiveControl(EntityPlayer player, NBTTagCompound data) { }

	// Doing this out of laziness for now, static initializers cannot be used in interfaces so, yeah.
	class NBTHelper {
		private static final IdentityHashMap<Class<?>,Integer> classToNBTType = new IdentityHashMap<>();
		static {
			classToNBTType.put(NBTTagEnd.class, 0);
			classToNBTType.put(NBTTagByte.class, 1);
			classToNBTType.put(Byte.class, 1);
			classToNBTType.put(byte.class, 1);
			classToNBTType.put(NBTTagShort.class, 2);
			classToNBTType.put(Short.class, 2);
			classToNBTType.put(short.class, 2);
			classToNBTType.put(NBTTagInt.class, 3);
			classToNBTType.put(Integer.class, 3);
			classToNBTType.put(int.class, 3);
			classToNBTType.put(NBTTagLong.class, 4);
			classToNBTType.put(Long.class, 4);
			classToNBTType.put(long.class, 4);
			classToNBTType.put(NBTTagFloat.class, 5);
			classToNBTType.put(Float.class, 5);
			classToNBTType.put(float.class, 5);
			classToNBTType.put(NBTTagDouble.class, 6);
			classToNBTType.put(Double.class, 6);
			classToNBTType.put(double.class, 6);
			classToNBTType.put(NBTTagByteArray.class, 7);
			classToNBTType.put(byte[].class, 7);
			classToNBTType.put(NBTTagString.class, 8);
			classToNBTType.put(String.class, 8);
			classToNBTType.put(NBTTagList.class, 9);
			classToNBTType.put(NBTTagCompound.class, 10);
			classToNBTType.put(NBTTagIntArray.class, 11);
			classToNBTType.put(int[].class, 11);
		}
		public static int getTagType(Class<?> cls) {
			return classToNBTType.getOrDefault(cls,-1);
		}
	}
	static <T> void executeIfPresent(NBTTagCompound tag, String name, Class<T> cls, Consumer<T> cons){
		int k = NBTHelper.getTagType(cls);
		if(tag.hasKey(name,k)){
			cons.accept(getValue(tag,name,k));
		}
	}
	@SuppressWarnings("unchecked")
	static <T> T getValue(NBTTagCompound tag, String name, int id){
		return (T)switch (id) {
			case 1 -> tag.getByte(name);
			case 2 -> tag.getShort(name);
			case 3 -> tag.getInteger(name);
			case 4 -> tag.getLong(name);
			case 5 -> tag.getFloat(name);
			case 6 -> tag.getDouble(name);
			case 7 -> tag.getByteArray(name);
			case 8 -> tag.getString(name);
			case 9 -> tag.getTag(name);
			case 10 -> tag.getCompoundTag(name);
			case 11 -> tag.getIntArray(name);
			default -> null;
		};
	}
}
