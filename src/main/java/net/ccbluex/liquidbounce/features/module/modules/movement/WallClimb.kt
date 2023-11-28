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
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlockIntersects
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import java.util.*

@ModuleInfo(name = "WallClimb", spacedName = "Wall Climb", description = "Allows you to climb up walls like a spider.", category = ModuleCategory.MOVEMENT)
class WallClimb : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Simple", "CheckerClimb", "Clip", "AAC3.3.12", "AACGlide", "Verus"), "Simple")
    private val clipMode = ListValue("ClipMode", arrayOf("Jump", "Fast"), "Fast") { modeValue.get().equals("clip", ignoreCase = true) }
    private val checkerClimbMotionValue = FloatValue("CheckerClimbMotion", 0f, 0f, 1f) { modeValue.get().equals("checkerclimb", ignoreCase = true) }
    private val verusClimbSpeed = FloatValue("VerusClimbSpeed", 0f, 0f, 1f) { modeValue.get().equals("verus", ignoreCase = true) }
    private var glitch = false
    private var canClimb = false
    private var waited = 0
    override fun onEnable() {
        glitch = false
        canClimb = false
        waited = 0
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (!mc.thePlayer.isCollidedHorizontally || mc.thePlayer.isOnLadder || mc.thePlayer.isInWater || mc.thePlayer.isInLava) return
        if ("simple".equals(modeValue.get(), ignoreCase = true)) {
            event.y = 0.2
            mc.thePlayer.motionY = 0.0
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (modeValue.get().equals("verus", ignoreCase = true) && canClimb) event.cancelEvent()
    }

    @EventTarget
    fun onUpdate(event: MotionEvent) {
        if (event.eventState !== EventState.POST) return
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "clip" -> {
                if (mc.thePlayer.motionY < 0) glitch = true
                if (mc.thePlayer.isCollidedHorizontally) {
                    when (clipMode.get().lowercase(Locale.getDefault())) {
                        "jump" -> if (mc.thePlayer.onGround) mc.thePlayer.jump()
                        "fast" -> if (mc.thePlayer.onGround) mc.thePlayer.motionY = .42 else if (mc.thePlayer.motionY < 0) mc.thePlayer.motionY = -0.3
                    }
                }
            }

            "checkerclimb" -> {
                val isInsideBlock = collideBlockIntersects(mc.thePlayer.entityBoundingBox) { block: Block? -> block !is BlockAir }
                val motion = checkerClimbMotionValue.get()
                if (isInsideBlock && motion != 0f) mc.thePlayer.motionY = motion.toDouble()
            }

            "aac3.3.12" -> if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isOnLadder) {
                waited++
                if (waited == 1) mc.thePlayer.motionY = 0.43
                if (waited == 12) mc.thePlayer.motionY = 0.43
                if (waited == 23) mc.thePlayer.motionY = 0.43
                if (waited == 29) mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ)
                if (waited >= 30) waited = 0
            } else if (mc.thePlayer.onGround) waited = 0

            "aacglide" -> {
                if (!mc.thePlayer.isCollidedHorizontally || mc.thePlayer.isOnLadder) return
                mc.thePlayer.motionY = -0.189
            }

            "verus" -> if (!mc.thePlayer.isCollidedHorizontally || mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isOnLadder) {
                canClimb = false
            } else {
                canClimb = true
                mc.thePlayer.motionY = verusClimbSpeed.get().toDouble()
                mc.thePlayer.onGround = true
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            val packetPlayer = packet
            if (glitch) {
                val yaw = MovementUtils.direction.toFloat()
                packetPlayer.x = packetPlayer.x - MathHelper.sin(yaw) * 0.00000001
                packetPlayer.z = packetPlayer.z + MathHelper.cos(yaw) * 0.00000001
                glitch = false
            }
            if (canClimb) packetPlayer.onGround = true
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer == null) return
        val mode = modeValue.get()
        when (mode.lowercase(Locale.getDefault())) {
            "checkerclimb" -> if (event.y > mc.thePlayer.posY) event.boundingBox = null
            "clip" -> if (event.block != null && mc.thePlayer != null && event.block is BlockAir && event.y < mc.thePlayer.posY && mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava) event.boundingBox = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(mc.thePlayer.posX, (mc.thePlayer.posY.toInt() - 1).toDouble(), mc.thePlayer.posZ)
        }
    }
}
