/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.item.ItemSword

@ModuleInfo(name = "TNTBlock", spacedName = "TNT Block", description = "Automatically blocks with your sword when TNT around you explodes.", category = ModuleCategory.COMBAT)
class TNTBlock : Module() {
    private val fuseValue = IntegerValue("Fuse", 10, 0, 80)
    private val rangeValue = FloatValue("Range", 9F, 1F, 20F)
    private val autoSwordValue = BoolValue("AutoSword", true)
    private var blocked = false

    @EventTarget
    fun onMotionUpdate(event: MotionEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityTNTPrimed && mc.thePlayer.getDistanceToEntity(entity) <= rangeValue.get()) {
                if (entity.fuse <= fuseValue.get()) {
                    if (autoSwordValue.get()) {
                        var slot = -1
                        var bestDamage = 1f
                        for (i in 0..8) {
                            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)

                            if (itemStack != null && itemStack.item is ItemSword) {
                                val itemDamage = (itemStack.item as ItemSword).damageVsEntity + 4F

                                if (itemDamage > bestDamage) {
                                    bestDamage = itemDamage
                                    slot = i
                                }
                            }
                        }

                        if (slot != -1 && slot != mc.thePlayer.inventory.currentItem) {
                            mc.thePlayer.inventory.currentItem = slot
                            mc.playerController.updateController()
                        }
                    }
                    if (mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword) {
                        mc.gameSettings.keyBindUseItem.pressed = true
                        blocked = true
                    }
                    return
                }
            }
        }

        if (blocked && !GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
            mc.gameSettings.keyBindUseItem.pressed = false
            blocked = false
        }
    }
}