/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.movement.InvMove;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput {

    public MixinMovementInputFromOptions(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    @Shadow
    private final GameSettings gameSettings;
    
    private final InvMove invMove = LiquidBounce.moduleManager.getModule(InvMove.class);

    private boolean shouldMove() {
        assert invMove != null;
        return invMove.getState() && (Objects.equals(invMove.getWhenMove().get(), "Inventory") && mc.currentScreen instanceof GuiInventory || Objects.equals(invMove.getWhenMove().get(), "Chest") && mc.currentScreen instanceof GuiChest || Objects.equals(invMove.getWhenMove().get(), "All") && (mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiInventory));
    }
    /**
     * @author Randomguy
     * @reason InvMove
     */
    @Overwrite
    public void updatePlayerMoveState() {

        if(this.shouldMove()) {

            moveStrafe = 0.0F;
            moveForward = 0.0F;

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
                ++moveForward;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
                --moveForward;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
                ++moveStrafe;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
                --moveStrafe;
            }

            jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        } else {
            moveStrafe = 0.0F;
            moveForward = 0.0F;
            if (gameSettings.keyBindForward.isKeyDown()) ++moveForward;
            if (gameSettings.keyBindBack.isKeyDown()) --moveForward;
            if (gameSettings.keyBindLeft.isKeyDown()) ++moveStrafe;
            if (gameSettings.keyBindRight.isKeyDown()) --moveStrafe;
            jump = gameSettings.keyBindJump.isKeyDown();
            sneak = gameSettings.keyBindSneak.isKeyDown();
            if (sneak) {
                moveStrafe = (float) ((double) moveStrafe * (Objects.requireNonNull(LiquidBounce.moduleManager.getModule(NoSlow.class)).getState() ? Objects.requireNonNull(LiquidBounce.moduleManager.getModule(NoSlow.class)).getSneakStrafeMultiplier().get() : 0.3D));
                moveForward = (float) ((double) moveForward * (Objects.requireNonNull(LiquidBounce.moduleManager.getModule(NoSlow.class)).getState() ? Objects.requireNonNull(LiquidBounce.moduleManager.getModule(NoSlow.class)).getSneakForwardMultiplier().get() : 0.3D));
            }
        }
        super.updatePlayerMoveState();
    }
}
