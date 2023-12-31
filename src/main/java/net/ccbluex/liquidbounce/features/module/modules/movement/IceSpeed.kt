/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getMaterial
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

@ModuleInfo(name = "IceSpeed", spacedName = "Ice Speed", description = "Allows you to walk faster on ice.", category = ModuleCategory.MOVEMENT)
class IceSpeed : Module() {
    private val modeValue = ListValue("Mode", arrayOf("NCP", "AAC", "Spartan"), "NCP")
    override fun onEnable() {
        if (modeValue.get().equals("NCP", ignoreCase = true)) {
            Blocks.ice.slipperiness = 0.39f
            Blocks.packed_ice.slipperiness = 0.39f
        }
        super.onEnable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val mode = modeValue.get()
        if (mode.equals("NCP", ignoreCase = true)) {
            Blocks.ice.slipperiness = 0.39f
            Blocks.packed_ice.slipperiness = 0.39f
        } else {
            Blocks.ice.slipperiness = 0.98f
            Blocks.packed_ice.slipperiness = 0.98f
        }
        if (mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isSneaking && mc.thePlayer.isSprinting && mc.thePlayer.movementInput.moveForward > 0.0) {
            if (mode.equals("AAC", ignoreCase = true)) {
                val material = getMaterial(mc.thePlayer.position.down())
                if (material === Material.ice || material === Material.packedIce) {
                    mc.thePlayer.motionX *= 1.342
                    mc.thePlayer.motionZ *= 1.342
                    Blocks.ice.slipperiness = 0.6f
                    Blocks.packed_ice.slipperiness = 0.6f
                }
            }
            if (mode.equals("Spartan", ignoreCase = true)) {
                val material = getMaterial(mc.thePlayer.position.down())
                if (material === Material.ice || material === Material.packedIce) {
                    val upBlock = getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ))
                    if (upBlock !is BlockAir) {
                        mc.thePlayer.motionX *= 1.342
                        mc.thePlayer.motionZ *= 1.342
                    } else {
                        mc.thePlayer.motionX *= 1.18
                        mc.thePlayer.motionZ *= 1.18
                    }
                    Blocks.ice.slipperiness = 0.6f
                    Blocks.packed_ice.slipperiness = 0.6f
                }
            }
        }
    }

    override fun onDisable() {
        Blocks.ice.slipperiness = 0.98f
        Blocks.packed_ice.slipperiness = 0.98f
        super.onDisable()
    }
}
