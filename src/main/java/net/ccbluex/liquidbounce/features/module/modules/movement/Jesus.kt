/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.lwjgl.input.Keyboard
import java.util.*

@ModuleInfo(name = "Jesus", description = "Allows you to walk on water.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_J)
class Jesus : Module() {
    val modeValue = ListValue("Mode", arrayOf("Vanilla", "NCP", "AAC", "AAC3.3.11", "AACFly", "AAC4.2.1", "Horizon1.4.6", "Twillight", "Matrix", "Dolphin", "Swim"), "NCP")
    private val noJumpValue = BoolValue("NoJump", false)
    private val aacFlyValue = FloatValue("AACFlyMotion", 0.5f, 0.1f, 1f)
    private val matrixSpeedValue = FloatValue("MatrixSpeed", 1.15f, 0.1f, 1.15f) { modeValue.get().equals("matrix", ignoreCase = true) }
    private var nextTick = false

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer == null || mc.thePlayer.isSneaking) return
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "ncp", "vanilla" -> if (collideBlock(mc.thePlayer.entityBoundingBox) { block: Block? -> block is BlockLiquid } && mc.thePlayer.isInsideOfMaterial(Material.air) && !mc.thePlayer.isSneaking) mc.thePlayer.motionY = 0.08
            "aac" -> {
                val blockPos = mc.thePlayer.position.down()
                if (!mc.thePlayer.onGround && getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
                    if (!mc.thePlayer.isSprinting) {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                    } else {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                    }
                    if (mc.thePlayer.fallDistance >= 4) mc.thePlayer.motionY = -0.004 else if (mc.thePlayer.isInWater) mc.thePlayer.motionY = 0.09
                }
                if (mc.thePlayer.hurtTime != 0) mc.thePlayer.onGround = false
            }

            "matrix" -> if (mc.thePlayer.isInWater) {
                mc.gameSettings.keyBindJump.pressed = false
                if (mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.motionY = 0.09
                    return
                }
                val block = getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ))
                val blockUp = getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.1, mc.thePlayer.posZ))
                if (blockUp is BlockLiquid) {
                    mc.thePlayer.motionY = 0.1
                } else if (block is BlockLiquid) {
                    mc.thePlayer.motionY = 0.0
                }
                mc.thePlayer.motionX *= matrixSpeedValue.get().toDouble()
                mc.thePlayer.motionZ *= matrixSpeedValue.get().toDouble()
            }

            "aac3.3.11" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionX *= 1.17
                mc.thePlayer.motionZ *= 1.17
                if (mc.thePlayer.isCollidedHorizontally) mc.thePlayer.motionY = 0.24 else if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ)).block !== Blocks.air) mc.thePlayer.motionY += 0.04
            }

            "dolphin" -> if (mc.thePlayer.isInWater) mc.thePlayer.motionY += 0.03999999910593033
            "aac4.2.1" -> {
                //i didn't fix if player collided wall(trigger aac flag)
                val blockPos = mc.thePlayer.position.down()
                if (!mc.thePlayer.onGround && getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
                    mc.thePlayer.motionY *= 0.0
                    //decrease value due to compatible 4 direction. forward is 0.089F
                    mc.thePlayer.jumpMovementFactor = 0.08f
                    if (mc.thePlayer.fallDistance > 0) return else if (mc.thePlayer.isInWater) mc.gameSettings.keyBindJump.pressed = true
                }
            }

            "horizon1.4.6" -> if (mc.thePlayer.isInWater) {
                MovementUtils.strafe()
                mc.gameSettings.keyBindJump.pressed = true
                if (MovementUtils.isMoving) if (!mc.thePlayer.onGround) {
                    mc.thePlayer.motionY += 0.13
                }
            }

            "twillight" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionX *= 1.04
                mc.thePlayer.motionZ *= 1.04
                MovementUtils.strafe()
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if ("aacfly".equals(modeValue.get(), ignoreCase = true) && mc.thePlayer.isInWater) {
            event.y = aacFlyValue.get().toDouble()
            mc.thePlayer.motionY = aacFlyValue.get().toDouble()
        }
        if ("twillight".equals(modeValue.get(), ignoreCase = true) && mc.thePlayer.isInWater) {
            event.y = 0.01
            mc.thePlayer.motionY = 0.01
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer == null || mc.thePlayer.entityBoundingBox == null) return
        if (event.block is BlockLiquid && !collideBlock(mc.thePlayer.entityBoundingBox) { block: Block? -> block is BlockLiquid } && !mc.thePlayer.isSneaking) {
            when (modeValue.get().lowercase(Locale.getDefault())) {
                "ncp", "vanilla" -> event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), (event.y + 1).toDouble(), (event.z + 1).toDouble())
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || !modeValue.get().equals("NCP", ignoreCase = true)) return
        if (event.packet is C03PacketPlayer) {
            if (collideBlock(AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY, mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01, mc.thePlayer.entityBoundingBox.minZ)) { block: Block? -> block is BlockLiquid }) {
                nextTick = !nextTick
                if (nextTick) event.packet.y -= 0.001
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer == null) return
        val block = getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.01, mc.thePlayer.posZ))
        if (noJumpValue.get() && block is BlockLiquid) event.cancelEvent()
    }

    override val tag: String
        get() = modeValue.get()
}
