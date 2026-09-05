package com.hotbarmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * The screen that pops up when a player types /hotbarmod (or /hotbarmod config).
 * All changes are applied live to HotbarConfig.INSTANCE so you can see the
 * hotbar resize itself behind the screen as you drag the sliders, and are
 * written to disk when you press Done.
 */
public class HotbarConfigScreen extends Screen {

	private final Screen parent;

	private ScaleSlider scaleSlider;
	private OffsetSlider offsetSlider;

	protected HotbarConfigScreen(Screen parent) {
		super(Text.literal("Hotbar Mod Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int y = this.height / 2 - 40;

		HotbarConfig cfg = HotbarConfig.INSTANCE;

		// --- Hotbar scale slider ---
		this.scaleSlider = new ScaleSlider(centerX - 100, y, 200, 20, cfg.hotbarScale);
		this.addDrawableChild(this.scaleSlider);

		y += 28;

		// --- Vertical offset slider (-20 to +20 px) ---
		this.offsetSlider = new OffsetSlider(centerX - 100, y, 200, 20, cfg.verticalOffset);
		this.addDrawableChild(this.offsetSlider);

		y += 28;

		// --- Scale side icons toggle ---
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.scaleSideIcons)
				.build(centerX - 100, y, 200, 20,
						Text.literal("Scale Offhand/Attack Icons"),
						(button, value) -> HotbarConfig.INSTANCE.scaleSideIcons = value));

		y += 32;

		// --- Reset to defaults ---
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset to Default"), button -> {
					HotbarConfig fresh = new HotbarConfig();
					HotbarConfig.INSTANCE.hotbarScale = fresh.hotbarScale;
					HotbarConfig.INSTANCE.verticalOffset = fresh.verticalOffset;
					HotbarConfig.INSTANCE.scaleSideIcons = fresh.scaleSideIcons;
					this.clearAndInit();
				})
				.dimensions(centerX - 100, y, 200, 20)
				.build());

		y += 28;

		// --- Done ---
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> this.close())
				.dimensions(centerX - 100, y, 200, 20)
				.build());
	}

	@Override
	public void close() {
		HotbarConfig.INSTANCE.save();
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ------------------------------------------------------------------
	// Slider implementations
	// ------------------------------------------------------------------

	private static class ScaleSlider extends SliderWidget {
		ScaleSlider(int x, int y, int width, int height, float initialScale) {
			super(x, y, width, height,
					Text.literal("Hotbar Scale: " + formatScale(initialScale)),
					toSliderValue(initialScale));
		}

		private static double toSliderValue(float scale) {
			return (scale - HotbarConfig.MIN_SCALE) / (HotbarConfig.MAX_SCALE - HotbarConfig.MIN_SCALE);
		}

		private static float toScale(double sliderValue) {
			return (float) (HotbarConfig.MIN_SCALE + sliderValue * (HotbarConfig.MAX_SCALE - HotbarConfig.MIN_SCALE));
		}

		private static String formatScale(float scale) {
			return Math.round(scale * 100) + "%";
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Text.literal("Hotbar Scale: " + formatScale(toScale(this.value))));
		}

		@Override
		protected void applyValue() {
			HotbarConfig.INSTANCE.hotbarScale = toScale(this.value);
		}
	}

	private static class OffsetSlider extends SliderWidget {
		private static final int MIN_OFFSET = -20;
		private static final int MAX_OFFSET = 20;

		OffsetSlider(int x, int y, int width, int height, int initialOffset) {
			super(x, y, width, height,
					Text.literal("Vertical Offset: " + initialOffset + "px"),
					toSliderValue(initialOffset));
		}

		private static double toSliderValue(int offset) {
			return (double) (offset - MIN_OFFSET) / (MAX_OFFSET - MIN_OFFSET);
		}

		private static int toOffset(double sliderValue) {
			return MIN_OFFSET + (int) Math.round(sliderValue * (MAX_OFFSET - MIN_OFFSET));
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Text.literal("Vertical Offset: " + toOffset(this.value) + "px"));
		}

		@Override
		protected void applyValue() {
			HotbarConfig.INSTANCE.verticalOffset = toOffset(this.value);
		}
	}
}
