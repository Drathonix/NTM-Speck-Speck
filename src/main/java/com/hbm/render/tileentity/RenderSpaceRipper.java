package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.tileentity.machine.ripper.TileEntitySpaceRipper;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;

public class RenderSpaceRipper extends TileEntitySpecialRenderer implements IItemRendererProvider {
	private static final float ripCenterYOffset = 1.125F;

	private static DoubleBuffer buf = null;

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {
		TileEntitySpaceRipper ripper = (TileEntitySpaceRipper) te;

		if(buf == null){
			buf = GLAllocation.createDirectByteBuffer(8*4).asDoubleBuffer();
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		switch(te.getBlockMetadata() - BlockDummyable.offset) {
		case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}
		//GL11.glTranslated(  0.5, 0, 0.5);
		//GL11.glRotated(180, 0, 1, 0);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		bindTexture(ResourceManager.space_ripper_tex);
		ResourceManager.space_ripper.renderAll();

		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPopMatrix();

	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.space_ripper);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(2, 0, 2);
				GL11.glScaled( 2, 2, 2);
			}
			public void renderCommon() {
				GL11.glScaled(1, 1, 1);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.space_ripper_tex); ResourceManager.space_ripper.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}

}
