package com.cyannyan.cyansky.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
  @Shadow public abstract boolean damage(DamageSource source, float amount);
  
  private BlockPos lastSafePos = null;
  
  protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", cancellable = true)
  private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
    if (throwRandomly) {
      ItemEntity entity = cir.getReturnValue();
      Vec3d velocity = entity.getVelocity();
      
      entity.setVelocity(velocity.multiply(0.1, 1, 0.1));
      entity.setGlowing(true);
      
      // Prevent items instantly destroyed
      if (entity.getY() < -50) {
        entity.setPosition(entity.getX(), 0, entity.getZ());
      }
      
      cir.setReturnValue(entity);
    }
  }

  @Inject(at = @At("TAIL"), method = "tick()V", cancellable = true)
  private void onTick(CallbackInfo ci) {
    // Save last safe position
    if (isOnGround()) {
      BlockPos posDown = getBlockPos().down();
      BlockState state = world.getBlockState(posDown);
      if (state.getMaterial().isSolid()) {
        lastSafePos = getBlockPos();
      }
    }
  }
  
  @Inject(at = @At("TAIL"), method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable = true)
  private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValue() && !isDead() && source.isOutOfWorld() && lastSafePos != null) {
      // Teleport to last safe position
      BlockPos pos = getBlockPos();
      if (Math.abs(pos.getX() - lastSafePos.getX()) + Math.abs(pos.getZ() - lastSafePos.getZ()) < 20) {
        fallDistance = 0;
        this.requestTeleport(lastSafePos.getX(), lastSafePos.getY() + 2, lastSafePos.getZ());
        this.setVelocity(getVelocity().getX(), 0, getVelocity().getZ());
      }
    }
  }
}
