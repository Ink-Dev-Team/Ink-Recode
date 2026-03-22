package com.ink.recode.mixin;

import com.ink.recode.modules.impl.movement.Scaffold2;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerMixin {
    
    @Inject(at = @At("HEAD"), method = "tickMovement")
    private void onTickMovement(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        if (Scaffold2.INSTANCE != null && Scaffold2.INSTANCE.enabled) {
            Scaffold2.INSTANCE.handleAutoJump(player);
        }
    }
}
