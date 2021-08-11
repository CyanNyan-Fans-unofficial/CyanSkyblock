package com.cyannyan.cyansky.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
	@Shadow private int itemAge;

	protected ItemEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(at = @At("HEAD"), method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable = true)
	private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
	  
		// Disable item damage from fire and explosion
		if (source.isExplosive() || source.isFire() || source.isFallingBlock()) {
			cir.setReturnValue(false);
		}
	}
	
	@Inject(at = @At("TAIL"), method = "tick()V", cancellable = true)
	private void onTick(CallbackInfo ci) {
		// Bounce the item up when falling into void
		if (getY() < -10) {
			setVelocity(0, 4.2, 0);
			setGlowing(true);
			itemAge += 600;
		}
	}
}
