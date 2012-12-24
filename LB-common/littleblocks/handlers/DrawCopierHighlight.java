package littleblocks.handlers;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import littleblocks.core.LBCore;
import littleblocks.items.ItemLittleBlocksCopier;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;

public class DrawCopierHighlight {

    private static int pulse = 0;
    private static boolean doInc = true;
    
	@ForgeSubscribe
	public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event) {
		if (event.currentItem != null) {
			if (event.currentItem.getItem() instanceof ItemLittleBlocksCopier) {
                drawInWorldCopierOverlay(event);
			}
		}
	}

    public void drawInWorldCopierOverlay(DrawBlockHighlightEvent event) {

        double x = event.target.blockX + 0.5F;
        double y = event.target.blockY + 0.5F;
        double z = event.target.blockZ + 0.5F;
        double iPX = event.player.prevPosX + (event.player.posX - event.player.prevPosX) * event.partialTicks;
        double iPY = event.player.prevPosY + (event.player.posY - event.player.prevPosY) * event.partialTicks;
        double iPZ = event.player.prevPosZ + (event.player.posZ - event.player.prevPosZ) * event.partialTicks;
        int texture = event.context.renderEngine.getTexture("/terrain.png");

        float xScale = 1;
        float yScale = 1;
        float zScale = 1;
        float xShift = 1F;
        float yShift = 1F;
        float zShift = 1F;

        World world = ModLoader.getMinecraftInstance().theWorld;
        if (world.getBlockId(event.target.blockX, event.target.blockY, event.target.blockZ) == LBCore.littleBlocksID) {
        	xShift = 0;
        	yShift = 0;
        	zShift = 0;
        }
        
        ForgeDirection sideHit = ForgeDirection.getOrientation(event.target.sideHit);

        switch (sideHit) {
            case UP: {
                xScale = 1 + 0.1F;
                zScale = 1 + 0.1F;
                xShift = 0;
                zShift = 0;
                break;
            }
            case DOWN: {
                xScale = 1 + 0.1F;
                zScale = 1 + 0.1F;
                xShift = 0;
                yShift = -yShift;
                zShift = 0;
                break;
            }
            case NORTH: {
                xScale = 1 + 0.1F;
                yScale = 1 + 0.1F;
                xShift = 0;
                yShift = 0;
                zShift = -zShift;
                break;
            }
            case SOUTH: {
                xScale = 1 + 0.1F;
                yScale = 1 + 0.1F;
                xShift = 0;
                yShift = 0;
                break;
            }
            case EAST: {
                yScale = 1 + 0.1F;
                zScale = 1 + 0.1F;
                yShift = 0;
                zShift = 0;
                break;
            }
            case WEST: {
                yScale = 1 + 0.1F;
                zScale = 1 + 0.1F;
                xShift = -xShift;
                yShift = 0;
                zShift = 0;
                break;
            }
            default:
                break;
        }

        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);

        for (int i = 0; i < 6; i++) {
            ForgeDirection forgeDir = ForgeDirection.getOrientation(i);
            int zCorrection = (i == 2) ? -1 : 1;
            GL11.glPushMatrix();
            GL11.glTranslated(-iPX + x + xShift, -iPY + y + yShift, -iPZ + z + zShift);
            GL11.glScalef(1F * xScale, 1F * yScale, 1F * zScale);
            GL11.glRotatef(90, forgeDir.offsetX, forgeDir.offsetY, forgeDir.offsetZ);
            GL11.glTranslated(0, 0, 0.5f * zCorrection);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            renderPulsingQuad(texture, 0.75F);
            GL11.glPopMatrix();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
    }

    public static void renderPulsingQuad(int texture, float maxTransparency) {

        float pulseTransparency = (getPulseValue() * maxTransparency) / 3000f;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        Tessellator tessellator = Tessellator.instance;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, pulseTransparency);

        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(1, 1, 1, pulseTransparency);

        tessellator.addVertexWithUV(-0.5D, 0.5D, 0F, 0, 0.5);
        tessellator.addVertexWithUV(0.5D, 0.5D, 0F, 0.5, 0.5);
        tessellator.addVertexWithUV(0.5D, -0.5D, 0F, 0.5, 0);
        tessellator.addVertexWithUV(-0.5D, -0.5D, 0F, 0, 0);

        tessellator.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    private static int getPulseValue() {

        if (doInc) {
            pulse += 8;
        }
        else {
            pulse -= 8;
        }

        if (pulse == 3000) {
            doInc = false;
        }

        if (pulse == 0) {
            doInc = true;
        }

        return pulse;
    }
}