package com.hotbarmod.mixin;

import com.hotbarmod.HotbarConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps InGameHud#renderHotbar in a scale transform so the whole hotbar
 * (background, item slots, item counts, durability bars, selected-slot
 * highlight - everything drawn in that method) shrinks together.
 *
 * NOTE ON MAPPINGS: "renderHotbar" and its (float, DrawContext) signature
 * have been stable across recent 1.21.x Yarn releases, but Yarn names do
 * occasionally change between versions. If this mixin fails to apply
 * (you'll see a mixin apply error mentioning "renderHotbar" in the log
 * when you launch), open InGameHud in your IDE (Loom's genSources task
 * makes this readable) and confirm the real method name/signature there,
 * then update the "method" and "target" strings below to match.
 */
@Mixin(InGameHud.class)
public class HotbarScaleMixin {

	@Inject(method = "renderHotbar(FLnet/minecraft/client/gui/DrawContext;)V", at = @At("HEAD"))
	private void hotbarmod$pushScale(float tickDelta, DrawContext context, CallbackInfo ci) {
		HotbarConfig cfg = HotbarConfig.INSTANCE;
		float scale = cfg.hotbarScale;
		if (scale == 1.0f && cfg.verticalOffset == 0) {
			return;
		}

		int screenWidth = context.getScaledWindowWidth();
		int screenHeight = context.getScaledWindowHeight();

		// Anchor the scale around the bottom-center of the screen (where the
		// hotbar is drawn) so it shrinks toward its own middle instead of
		// sliding toward the top-left corner.
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(screenWidth / 2f, screenHeight + cfg.verticalOffset);
		context.getMatrices().scale(scale, scale);
		context.getMatrices().translate(-screenWidth / 2f, -screenHeight);
	}

	@Inject(method = "renderHotbar(FLnet/minecraft/client/gui/DrawContext;)V", at = @At("TAIL"))
	private void hotbarmod$popScale(float tickDelta, DrawContext context, CallbackInfo ci) {
		HotbarConfig cfg = HotbarConfig.INSTANCE;
		if (cfg.hotbarScale == 1.0f && cfg.verticalOffset == 0) {
			return;
		}
		context.getMatrices().popMatrix();
	}
}
