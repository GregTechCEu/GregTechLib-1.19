package com.gregtechceu.gtlib.gui.widget;

import com.gregtechceu.gtlib.client.shader.Shaders;
import com.gregtechceu.gtlib.gui.editor.ColorPattern;
import com.gregtechceu.gtlib.gui.util.DrawerHelper;
import com.gregtechceu.gtlib.utils.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@Accessors(chain = true)
public class HsbColorWidget extends Widget {

	/**
	 * all supported pick mode
	 */
	private enum HSB_MODE {
		H("hue"), S("saturation"), B("brightness");
		private final String name;

		HSB_MODE(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * the length between the main and slide
	 */
	@Setter
	private int gap;
	/**
	 * the slide width
	 */
	@Setter
	private int barWidth;
	/**
	 * hue component, must range from 0f to 360f
	 */
	private float h = 204;
	/**
	 * saturation component, must range from 0f to 1f
	 */
	private float s = 0.72f;
	/**
	 * the brightness component, must range from 0f to 1f
	 */
	private float b = 0.94f;
	/**
	 * thr alpha used for draw main and slide
	 */
	private float alpha = 1;
	/**
	 * the rgb transformed from hsb color space
	 * [0x00rrggbb]
	 */
	private int argb;
	private boolean isDraggingMain, isDraggingColorSlider, isDraggingAlphaSlider;

	private HSB_MODE mode = HSB_MODE.H;
	@Setter
	private IntSupplier colorSupplier;
	@Setter
	private IntConsumer onChanged;

	public HsbColorWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.gap = 10;
		this.barWidth = 10;
		refreshRGB();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void updateScreen() {
		super.updateScreen();
		if (isClientSideWidget && colorSupplier != null) {
			setColor(colorSupplier.getAsInt());
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		if (colorSupplier != null && !isClientSideWidget) {
			int lastColor = argb;
			setColor(colorSupplier.getAsInt());
			if (lastColor != argb) {
				writeUpdateInfo(-1, buffer -> buffer.writeVarInt(argb));
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawInBackground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		super.drawInBackground(poseStack, mouseX, mouseY, partialTicks);
		Matrix4f pose = poseStack.last().pose();
		int x = getPosition().x;
		int y = getPosition().y;
		int width = getSize().width;
		int height = getSize().height;

		BufferBuilder builder = Tesselator.getInstance().getBuilder();
		drawHsbContext(pose, builder, x, y, width - barWidth - gap, height - barWidth - gap);

		// alpha bar
		DrawerHelper.drawGradientRect(poseStack, x, y + height - barWidth, width - barWidth - gap, barWidth, argb & 0x00ffffff, argb | 0xff000000, true);
		// preview
		DrawerHelper.drawSolidRect(poseStack, x + width - barWidth, y + height - barWidth, barWidth, barWidth, argb);

		float color = 0;
		float mainX = 0, mainY = 0;
		switch (mode) {
			case H -> {
				mainX = s;
				mainY = 1 - b;
				color = (1 - h / 360f);
			}
			case S -> {
				mainX = h / 360f;
				mainY = 1 - b;
				color = (1 - s);
			}
			case B -> {
				mainX = h / 360f;
				mainY = 1- s;
				color = (1 - b);
			}
		}

		// main indicator
		DrawerHelper.drawSolidRect(poseStack, (int) (x + mainX * (width - barWidth - gap)) - 1, (int) (y + mainY * (height - barWidth - gap)) - 1, 3, 3, 0xffff0000);
		// color indicator
		DrawerHelper.drawSolidRect(poseStack, x + width - barWidth - 2, (int) (y + color * (height - barWidth - gap)), barWidth + 4, 1, 0xffff0000);
		// alpha indicator
		DrawerHelper.drawSolidRect(poseStack, (int) (x + alpha * (width - barWidth - gap)), y + height - barWidth - 2, 1, barWidth + 4, 0xffff0000);

		renderInfo(poseStack, x, y, width - barWidth - gap, height - barWidth - gap);

		// border
		DrawerHelper.drawBorder(poseStack, x, y, width - barWidth - gap, height - barWidth - gap, ColorPattern.WHITE.color, 2);
	}

	/**
	 * have context for render hsb content
	 */
	@Environment(EnvType.CLIENT)
	private void drawHsbContext(Matrix4f pose, BufferBuilder builder, int x, int y, int width, int height) {
		RenderSystem.setShader(Shaders::getHsbShader);
		builder.begin(VertexFormat.Mode.QUADS, Shaders.HSB_VERTEX_FORMAT);

		renderMain(pose, builder, x, y, width, height);
		renderColorSlide(pose, builder, x, y, width, height);

		BufferUploader.drawWithShader(builder.end());
	}

	@Environment(EnvType.CLIENT)
	private void renderMain(Matrix4f pose, BufferBuilder builder, int x, int y, int width, int height) {
		float _h = 0, _s = 0, _b = 0f;

		{
			//left-up corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 0f;
					_b = 1f;
				}
				case S -> {
					_h = 0f;
					_s = s;
					_b = 1f;
				}
				case B -> {
					_h = 0f;
					_s = 1f;
					_b = b;
				}
			}
			builder.vertex(pose, x, y, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();
		}

		{
			//left-down corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 0f;
					_b = 0f;
				}
				case S -> {
					_h = 0f;
					_s = s;
					_b = 0f;
				}
				case B -> {
					_h = 0f;
					_s = 0;
					_b = b;
				}
			}
			builder.vertex(pose, x, y + height, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();
		}

		{
			//right-down corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 1f;
					_b = 0f;
				}
				case S -> {
					_h = 360f;
					_s = s;
					_b = 0f;
				}
				case B -> {
					_h = 360f;
					_s = 0f;
					_b = b;
				}
			}
			builder.vertex(pose, x + width, y + height, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();
		}

		{
			//right-up corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 1f;
					_b = 1f;
				}
				case S -> {
					_h = 360f;
					_s = s;
					_b = 1f;
				}
				case B -> {
					_h = 360f;
					_s = 1f;
					_b = b;
				}
			}

			builder.vertex(pose, x + width, y, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();
		}
	}

	@Environment(EnvType.CLIENT)
	private void renderColorSlide(Matrix4f pose, BufferBuilder builder, int x, int y, int width, int height) {

		float _h = 0f, _s = 0f, _b = 0f;
		var barX = x + width + gap;

		{
			//down two corners
			switch (mode) {
				case H -> {
					_h = 0f;
					_s = 1f;
					_b = 1f;
				}
				case S -> {
					_h = h;
					_s = 0f;
					_b = b;
				}
				case B -> {
					_h = h;
					_s = s;
					_b = 0f;
				}
			}
			builder.vertex(pose, barX, y + height, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();

			builder.vertex(pose, barX + barWidth, y + height, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();
		}

		{
			//up two corners
			switch (mode) {
				case H -> {
					_h = 360f;
					_s = 1f;
					_b = 1f;
				}
				case S -> {
					_h = h;
					_s = 1f;
					_b = b;
				}
				case B -> {
					_h = h;
					_s = s;
					_b = 1f;
				}
			}
			builder.vertex(pose, barX + barWidth, y, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();

			builder.vertex(pose, barX, y, 0.0f);
			putColor(builder, _h, _s, _b).nextElement();
			builder.endVertex();
		}


	}

	/**
	 * render hsb/rgb/mode info
	 */
	@Environment(EnvType.CLIENT)
	private void renderInfo(PoseStack poseStack, int x, int y, int width, int height) {
		Font font = Minecraft.getInstance().font;
		y += 2;
		var strX = x + 10;
		var strGapY = (int) Math.max(0, (height - 7f * font.lineHeight) / 6f) + font.lineHeight;
		DrawerHelper.drawText(poseStack,"h:" + (int) h + "°", strX, y, 1f, -1, true);
		DrawerHelper.drawText(poseStack, "s:" + (int) (s * 100) + "%", strX, y + strGapY, 1f, -1, true);
		DrawerHelper.drawText(poseStack, "b:" + (int) (b * 100) + "%", strX, y + strGapY * 2, 1f, -1, true);
		DrawerHelper.drawText(poseStack, "r:" + ((argb >> 16) & 0xff), strX, y + strGapY * 3, 1f, -1, true);
		DrawerHelper.drawText(poseStack, "g:" + ((argb >> 8) & 0xff), strX, y + strGapY * 4, 1f, -1, true);
		DrawerHelper.drawText(poseStack, "b:" + (argb & 0xff), strX, y + strGapY * 5, 1f, -1, true);
		DrawerHelper.drawText(poseStack, "a:" + ((argb >> 24) & 0xff), strX, y + strGapY * 6, 1f, -1, true);
//		DrawerHelper.drawText(poseStack, "mode:" + mode, strX, y + strGapY * 6, 1f, 0xffffffff);
	}

	/**
	 * put hsb color into BufferBuilder
	 */
	@Environment(EnvType.CLIENT)
	private BufferBuilder putColor(BufferBuilder builder, float h, float s, float b) {
		return putColor(builder, h, s, b, 1);
	}

	@Environment(EnvType.CLIENT)
	private BufferBuilder putColor(BufferBuilder builder, float h, float s, float b, float a) {
		builder.putFloat(0, h);
		builder.putFloat(4, s);
		builder.putFloat(8, b);
		builder.putFloat(12, a);
		return builder;
	}

	public boolean isMouseOverMain(double mouseX, double mouseY) {
		int x = getPosition().x;
		int y = getPosition().y;
		int width = getSize().width - gap - barWidth;
		int height = getSize().height - gap - barWidth;
		return isMouseOver(x, y, width, height, mouseX, mouseY);
	}

	public boolean isMouseOverColorSlider(double mouseX, double mouseY) {
		int x = getPosition().x + getSize().width - barWidth;
		int y = getPosition().y;
		int height = getSize().height - gap - barWidth;
		return isMouseOver(x, y, barWidth, height, mouseX, mouseY);
	}

	public boolean isMouseOverAlphaSlider(double mouseX, double mouseY) {
		int x = getPosition().x;
		int y = getPosition().y + getSize().width - barWidth;
		int width = getSize().width - gap - barWidth;
		return isMouseOver(x, y, width, barWidth, mouseX, mouseY);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		isDraggingMain = false;
		isDraggingColorSlider = false;
		isDraggingAlphaSlider = false;
		if (isMouseOverMain(mouseX, mouseY)) {
			if (button == 0) {
				isDraggingMain = true;
			} else if (button == 1) {
				mode = switch (mode) {
					case H -> HSB_MODE.S;
					case S -> HSB_MODE.B;
					case B -> HSB_MODE.H;
				};
				return true;
			}
		} else if (isMouseOverColorSlider(mouseX, mouseY)) {
			if (button == 0) {
				isDraggingColorSlider = true;
			}
		} else if (isMouseOverAlphaSlider(mouseX, mouseY)) {
			if (button == 0) {
				isDraggingAlphaSlider = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void refreshRGB() {
		argb = ColorUtils.HSBtoRGB(h / 360f, s, b);
		argb = ColorUtils.color(alpha, ColorUtils.red(argb), ColorUtils.green(argb), ColorUtils.blue(argb));
		if (onChanged != null) {
			onChanged.accept(argb);
		}
		if (isRemote() && !isClientSideWidget) {
			writeClientAction(-1, buffer -> buffer.writeVarInt(argb));
		}
	}

	@Override
	public void handleClientAction(int id, FriendlyByteBuf buffer) {
		if (id == -1) {
			setColor(buffer.readVarInt());
		} else {
			super.handleClientAction(id, buffer);
		}
	}

	@Override
	public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
		if (id == -1) {
			setColor(buffer.readVarInt());
		} else {
			super.readUpdateInfo(id, buffer);
		}
	}

	private static float normalizeMouse(double mouse, int pos, int size) {
		if (mouse >= pos + size) return 1;
		if (mouse <= pos) return 0;
		double x = mouse - pos;
		double y = x % size / size;
		if (y < 0) {
			x = -x;
			y = -y;
		}
		x /= size;
		return (float) (x % 2 > 1 ? 1 - y : y);
	}


	@Override
	@Environment(EnvType.CLIENT)
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		int x = getPosition().x;
		int y = getPosition().y;
		int width = getSize().width;
		int height = getSize().height;
		if (isDraggingMain) {
			float normalizedX = normalizeMouse(mouseX, x, width - gap - barWidth);
			float normalizedY = normalizeMouse(mouseY, y, height - gap - barWidth);
			switch (mode) {
				case H -> {
					s = normalizedX;
					b = 1.0f - normalizedY;
				}
				case S -> {
					h = normalizedX * 360f;
					b = 1.0f - normalizedY;
				}
				case B -> {
					h = normalizedX * 360f;
					s = 1.0f - normalizedY;
				}
			}
			refreshRGB();
			return true;
		} else if (isDraggingColorSlider) {
			float normalizedY = normalizeMouse(mouseY, y, height - gap - barWidth);
			switch (mode) {
				case H -> h = (1.0f - normalizedY) * 360f;
				case S -> s = 1.0f - normalizedY;
				case B -> b = 1.0f - normalizedY;
			}
			refreshRGB();
			return true;
		} else if (isDraggingAlphaSlider) {
			this.alpha = normalizeMouse(mouseX, x, width - gap - barWidth);

			refreshRGB();
		}
		return false;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		isDraggingMain = false;
		isDraggingColorSlider = false;
		isDraggingAlphaSlider = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	public HsbColorWidget setColor(int argb) {
		if (this.argb == argb) return this;
		this.alpha = ColorUtils.alpha(argb);
		var hsb = ColorUtils.RGBtoHSB(argb);
		hsb[0] *= 360f;
		this.h = hsb[0];
		this.s = hsb[1];
		this.b = hsb[2];
		refreshRGB();
		return this;
	}

}
