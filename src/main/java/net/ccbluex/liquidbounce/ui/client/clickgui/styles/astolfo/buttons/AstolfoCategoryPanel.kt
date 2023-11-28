package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_CATEGORY
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_MODULE
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.PANEL_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.PANEL_WIDTH
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import java.awt.Color

class AstolfoCategoryPanel(x: Float, y: Float, var category: ModuleCategory, var color: Color) : AstolfoButton(x, y, PANEL_WIDTH, PANEL_HEIGHT) {
    var open = false
    var moduleButtons = ArrayList<AstolfoModuleButton>()
    val name = category.displayName
    private var dragged = false
    private var mouseX2 = 0
    private var mouseY2 = 0

    init {
        val startY = y + height
        for ((count, mod) in LiquidBounce.moduleManager.modules.filter { it.category.displayName.equals(this.category.displayName, true) }.withIndex()) {
            moduleButtons.add(AstolfoModuleButton(x, startY + height * count, width, height, mod, color))
        }
    }

    override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
        if (dragged) {
            x = (mouseX2 + mouseX).toFloat()
            y = (mouseY2 + mouseY).toFloat()
        }
        drawRect(x, y, x + width, y + height, BACKGROUND_CATEGORY)
        FONT.drawHeightCenteredString(category.displayName.lowercase(), x + 8, y + height / 2, -0x1)

        var used = 0f
        if (open) {
            val startY = y + height
            for (moduleButton in moduleButtons) {
                moduleButton.x = x
                moduleButton.y = startY + used
                val box = moduleButton.drawPanel(mouseX, mouseY)
                used += box.height
            }
        }

        if (moduleButtons.isNotEmpty() && open) {
            drawRect(x, y + height + used, x + width, y + height + used + 2f, BACKGROUND_MODULE)
        }
        drawBorderedRect(x, y, x + width, y + height + used + if (moduleButtons.isNotEmpty() && open) 2f else 0f, 2f, color.rgb, Color(0, 0, 0, 0).rgb)

        return Rectangle() // unused since panel is the biggest unit there is
    }

    override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int) {
        if (isHovered(mouseX, mouseY)) {
            if (click) {
                if (button == 0) {
                    dragged = true
                    mouseX2 = (x - mouseX).toInt()
                    mouseY2 = (y - mouseY).toInt()
                } else {
                    open = !open
                }
            }
        }
        if (!click) dragged = false
    }

    override fun onClosed() {
        dragged = false
    }
}
