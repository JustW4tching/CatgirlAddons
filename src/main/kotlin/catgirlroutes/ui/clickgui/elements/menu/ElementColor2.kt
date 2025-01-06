package catgirlroutes.ui.clickgui.elements.menu

import catgirlroutes.module.settings.impl.ColorSetting2
import catgirlroutes.ui.clickgui.elements.Element
import catgirlroutes.ui.clickgui.elements.ElementType
import catgirlroutes.ui.clickgui.elements.ModuleButton
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.ColorUtil.hex
import catgirlroutes.ui.clickgui.util.ColorUtil.hsbMax
import catgirlroutes.ui.clickgui.util.ColorUtil.withAlpha
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.Utils.equalsOneOf
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import java.awt.Color

class ElementColor2(parent: ModuleButton, setting: ColorSetting2) :
    Element<ColorSetting2>(parent, setting, ElementType.COLOR2) {

    var dragging: Int? = null

    private var hexString = "#00000000"
    private var hexPrev = hexString
    private var listeningHex = false

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Int {

        val colorValue = this.setting.value

        FontUtil.drawString(displayName, 1, 2)

        /** Render the color preview */
        Gui.drawRect(width - 26, 2, width - 1, 11, colorValue.rgb)

        /** Render the tab indicating the drop-down */
//        Gui.drawRect(0,  13, width, 15, ColorUtil.tabColorBg)
//        Gui.drawRect((width * 0.4).toInt(), 12, (width * 0.6).toInt(), 15, ColorUtil.tabColor)

        if (extended) {

            /**
             * SB
             */
            HUDRenderUtils.drawSBBox(0, DEFAULT_HEIGHT, width, DEFAULT_HEIGHT * 5, colorValue.hsbMax().rgb) // todo: hue instead
            HUDRenderUtils.drawBorderedRect(
                this.setting.saturation * width.toDouble(),
                DEFAULT_HEIGHT + (1 - this.setting.brightness) * (DEFAULT_HEIGHT * 5).toDouble(),
                5.0, 5.0, 1.0,
                colorValue.withAlpha(255), colorValue.darker().withAlpha(255)
            )

            /**
             * HUE
             */
            HUDRenderUtils.drawHueBox(0, DEFAULT_HEIGHT * 6 + 3, width, DEFAULT_HEIGHT - 6)
            HUDRenderUtils.drawBorderedRect(
                this.setting.hue * width.toDouble(),
                DEFAULT_HEIGHT.toDouble() * 6 + 3,
                3.0, 9.0, 1.0,
                colorValue.hsbMax().withAlpha(255), colorValue.hsbMax().darker().withAlpha(255)
            )

            /**
             * ALPHA
             */
            if (this.setting.allowAlpha) {
                HUDRenderUtils.drawSBBox(
                    0, DEFAULT_HEIGHT * 7 + 3, width, DEFAULT_HEIGHT - 6,
                    this.setting.value.withAlpha(255).rgb, Color.black.rgb, this.setting.value.withAlpha(255).rgb, Color.black.rgb
                )

                HUDRenderUtils.drawBorderedRect(
                    this.setting.alpha * width.toDouble(),
                    DEFAULT_HEIGHT.toDouble() * 7 + 3,
                    3.0, 9.0, 1.0,
                    Color.WHITE.withAlpha(this.setting.alpha), colorValue.darker().withAlpha(255)
                )
            }

            /**
             * DRAGGING
             */
            when (dragging) {
                0 -> {
                    this.setting.saturation = MathHelper.clamp_float((mouseX - xAbsolute).toFloat() / width, 0.0f, 1.0f)
                    this.setting.brightness = MathHelper.clamp_float(-(mouseY - yAbsolute - DEFAULT_HEIGHT * 6).toFloat() / (DEFAULT_HEIGHT * 5), 0.0f, 1.0f)
                }
                1 -> this.setting.hue = MathHelper.clamp_float(((mouseX - xAbsolute - 6).toFloat() / width), 0.0f, 1.0f)
                2 -> this.setting.alpha = MathHelper.clamp_float((mouseX - xAbsolute - 6).toFloat() / width, 0.0f, 1.0f)
            }

            /**
             * HEX STRING
             */
            if (dragging != null) {
                hexString = "#${colorValue.hex}"
            }

            if (listeningHex) {
                HUDRenderUtils.renderRect(0.0, DEFAULT_HEIGHT.toDouble() * 8, width.toDouble(), DEFAULT_HEIGHT.toDouble() - 2, ColorUtil.clickGUIColor)
            }

            FontUtil.drawString("Hex", 1, DEFAULT_HEIGHT * 8 + 2)
            FontUtil.drawString(hexString, width - FontUtil.getStringWidth(hexString), DEFAULT_HEIGHT * 8 + 2)

//            debugMessage(dragging)
//            debugMessage("""
//                ${this.setting.saturation} ${this.setting.brightness}
//                ${this.setting.hue}
//                ${this.setting.alpha}
//            """.trimIndent())

        }

        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered(mouseX, mouseY)) {
                extended = !extended
                return true
            }

            if (!extended) return false

            dragging = when {
                isHovered(mouseX, mouseY, 0, DEFAULT_HEIGHT, width, DEFAULT_HEIGHT * 6) -> 0 // SB box
                isHovered(mouseX, mouseY, 0, DEFAULT_HEIGHT * 6 + 3, width, DEFAULT_HEIGHT * 7 - 3) -> 1 // hue
                isHovered(mouseX, mouseY, 0, DEFAULT_HEIGHT * 7 + 3, width, DEFAULT_HEIGHT * 8 - 3) && this.setting.allowAlpha -> 2 // alpha
                else -> null
            }

            if (isHovered(mouseX, mouseY, 0, DEFAULT_HEIGHT * 8, width, DEFAULT_HEIGHT * 9)) {
                if (listeningHex) completeHexString()
                else listeningHex = true
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listeningHex) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                completeHexString()
                listeningHex = false
            } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                hexString = ""
                hexString += GuiScreen.getClipboardString()
            } else if (keyCode == Keyboard.KEY_BACK) {
                hexString = hexString.dropLast(1)
            } else if (!ElementTextField.keyBlackList.contains(keyCode)) {
                hexString += typedChar.toString()
            }
            return true
        }
        return super.keyTyped(typedChar, keyCode)
    }

    private fun completeHexString() {
        if (hexString.isEmpty()) return
        val stringWithoutHash = hexString.removePrefix("#")
        if (stringWithoutHash.length.equalsOneOf(6, 8)) {
            try {
                val alpha = if (stringWithoutHash.length == 8) stringWithoutHash.substring(6, 8).toInt(16) / 255f else 1f
                val red = stringWithoutHash.substring(0, 2).toInt(16) / 255f
                val green = stringWithoutHash.substring(2, 4).toInt(16) / 255f
                val blue = stringWithoutHash.substring(4, 6).toInt(16) / 255f
                setting.value = Color(red, green, blue, alpha)
                hexPrev = hexString
            } catch (e: Exception) {
                debugMessage(e.toString())
                hexString = hexPrev
            }
        } else {
            hexString = hexPrev
        }
    }

    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + 15
    }

    private fun isHovered(mouseX: Int, mouseY: Int, x: Int, y: Int, width: Int, height: Int): Boolean {
        return mouseX >= xAbsolute + x && mouseX <= xAbsolute + width && mouseY >= yAbsolute + y && mouseY <= yAbsolute + height
    }

}