package com.cyannyan.cyansky.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
	protected ItemEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(at = @At("HEAD"), method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable = true)
	private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		// Disable explosive item damage (Fire damage desync with client)
		if (source.isExplosive()) {
			cir.setReturnValue(false);
		}
	}
	
	@Inject(at = @At("TAIL"), method = "tick()V", cancellable = true)
	private void onTick(CallbackInfo ci) {
	  if (getY() < -15 && isGlowing()) {
	  	requestTeleport(getX(), 255, getZ());
	  	setVelocity(getVelocity().getX(), 0, getVelocity().getZ());
		}
	}
}
