package com.cyannyan.cyansky.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

  @Shadow public abstract void sendMessage(Text message, boolean actionBar);

  private BlockPos lastSafePos = null;
  
  protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", cancellable = true)
  private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
    ItemEntity entity = cir.getReturnValue();

    if (throwRandomly) {
      entity.setGlowing(true);
      Vec3d velocity = entity.getVelocity();
      if (getY() < 0) {
        entity.setVelocity(velocity.multiply(0.1, 1, 0.1));
        entity.setPos(entity.getX(), 0, entity.getZ());
      } else {
        entity.setVelocity(velocity.multiply(0.1, 1, 0.1));
      }
      
      cir.setReturnValue(entity);
    }
  }

  @Inject(at = @At("TAIL"), method = "tick()V")
  private void onTick(CallbackInfo ci) {
    // Save last safe position
    if (isOnGround() && checkSafe(getBlockPos())) {
      lastSafePos = getBlockPos();
    }
  }
  
  private boolean checkSafe(BlockPos pos) {
    BlockPos posDown = pos.down();
    BlockState state = world.getBlockState(pos);
    BlockState stateDown = world.getBlockState(posDown);
    
    return state.getMaterial().isSolid() || stateDown.getMaterial().isSolid();
  }

  @Inject(at = @At("TAIL"), method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable = true)
  private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValue() && !isDead() && source.isOutOfWorld() && getHealth() < 5 && lastSafePos != null) {
      // Teleport to last safe position
      BlockPos pos = getBlockPos();
      if (Math.abs(pos.getX() - lastSafePos.getX()) + Math.abs(pos.getZ() - lastSafePos.getZ()) < 50) {
        fallDistance = 0;
        this.requestTeleport(lastSafePos.getX(), lastSafePos.getY() + 2, lastSafePos.getZ());
        this.setVelocity(getVelocity().getX(), 0, getVelocity().getZ());
        Text teleportText = new LiteralText("Be careful! You're teleported back.").formatted(Formatting.YELLOW);
        this.sendMessage(teleportText, false);
      }
    }
  }
  
}
