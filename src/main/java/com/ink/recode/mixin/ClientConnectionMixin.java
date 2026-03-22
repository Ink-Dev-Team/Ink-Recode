package com.ink.recode.mixin;

import com.ink.recode.utils.Rotation;
import com.ink.recode.utils.RotationManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    
    // 防止无限递归的标志
    private boolean isSendingModifiedPacket = false;
    
    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        // 防止无限递归
        if (isSendingModifiedPacket) {
            return;
        }
        
        try {
            // 如果是玩家移动包
            if (packet instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
                
                // 获取服务器旋转角度（使用INSTANCE访问Kotlin object）
                Rotation serverRotation = RotationManager.INSTANCE.getServerRotation();
                
                if (serverRotation != null) {
                    // 使用Kotlin data class的字段
                    float yaw = serverRotation.yaw;
                    float pitch = serverRotation.pitch;
                    
                    // 检查数据包类型并创建相应的新包
                    PlayerMoveC2SPacket newPacket;
                    if (movePacket instanceof PlayerMoveC2SPacket.Full) {
                        // 完整移动包
                        newPacket = new PlayerMoveC2SPacket.Full(
                            movePacket.getX(0),
                            movePacket.getY(0),
                            movePacket.getZ(0),
                            yaw,
                            pitch,
                            movePacket.isOnGround()
                        );
                    } else if (movePacket instanceof PlayerMoveC2SPacket.LookAndOnGround) {
                        // 仅视角包
                        newPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                            yaw,
                            pitch,
                            movePacket.isOnGround()
                        );
                    } else if (movePacket instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                        // 仅位置包（保持原角度）
                        return; // 不修改仅位置的包
                    } else {
                        // 其他类型的包
                        return;
                    }
                    
                    // 取消原包
                    ci.cancel();
                    
                    // 发送新包，设置递归标志
                    isSendingModifiedPacket = true;
                    ((ClientConnection)(Object)this).send(newPacket);
                    isSendingModifiedPacket = false;
                }
            }
        } catch (Exception e) {
            // 捕获所有异常，避免崩溃
            e.printStackTrace();
            // 确保标志被重置
            isSendingModifiedPacket = false;
        }
    }
}
