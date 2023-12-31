package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.utils.MouseButtons
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.value.BoolValue
import java.awt.Color

class BoolValueButton(x: Float, y: Float, width: Float, height: Float, var setting: BoolValue, var color: Color) : BaseValueButton(x, y, width, height, setting) {
    override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
        val background = Rectangle(x, y, width, height)
        val foreground = Rectangle(x + 3, y, width - 3 * 2, height)
        drawRect(background, BACKGROUND_VALUE)
        if (setting.get()) drawRect(foreground, color.rgb)
        FONT.drawHeightCenteredString(setting.name, x + hOffset, y + height / 2, -0x1)
        return background
    }

    override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int): Boolean {
        if (!show) return false
        if (isHovered(mouseX, mouseY) && click && button == MouseButtons.LEFT.ordinal) {
            setting.set(!setting.get())
            return true
        }
        return false
    }
}
