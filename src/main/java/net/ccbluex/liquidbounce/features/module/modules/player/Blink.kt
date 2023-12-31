/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils.handlePackets
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

@ModuleInfo(name = "Blink", description = "Makes you blink.", category = ModuleCategory.PLAYER)
object Blink : Module() {

    private val packets = mutableListOf<Packet<*>>()
    private val packetsReceived = mutableListOf<Packet<*>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private val positions = mutableListOf<Vec3>()
    val pulse by BoolValue("Pulse", false)
    private val fakePlayerMenu by BoolValue("FakePlayer", true)
    private val pulseDelay by IntegerValue("PulseDelay", 1000, 500, 1000) { pulse }
    private val mode by ListValue("Mode", arrayOf("Sent", "Received", "Both"), "Sent")
    private val trail by BoolValue("Trail", true)
    private val trailRed by IntegerValue("TrailRed", 255, 0, 255) { trail }
    private val trailGreen by IntegerValue("TrailGreen", 255, 0, 255) { trail }
    private val trailBlue by IntegerValue("TrailBlue", 255, 0, 255) { trail }
    private val trailAlpha by IntegerValue("TrailAlpha", 255, 0, 255) { trail }
    private val pulseTimer = MSTimer()

    private val trailColor
        get() = Color(trailRed, trailGreen, trailBlue, trailAlpha)

    override fun onEnable() {
        pulseTimer.reset()

        addFakePlayer()
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        blink()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return

        if (event.isCancelled)
            return

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is S02PacketChat, is S40PacketDisconnect -> {
                return
            }
        }

        when (mode.lowercase()) {
            "sent" -> {
                if (event.eventType == EventState.RECEIVE) {
                    handlePackets(*packetsReceived.toTypedArray())
                    packetsReceived.clear()
                }
                if (event.eventType == EventState.SEND) {
                    event.cancelEvent()
                    packets += packet
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        positions += packetPos
                    }
                }
            }
            "received" -> {
                if (event.eventType == EventState.RECEIVE && mc.thePlayer.ticksExisted > 10) {
                    event.cancelEvent()
                    packetsReceived += packet
                }
                if (event.eventType == EventState.SEND) {
                    sendPackets(*packets.toTypedArray(), triggerEvents = false)
                    packets.clear()
                }
            }
            "both" -> {
                if (event.eventType == EventState.RECEIVE && mc.thePlayer.ticksExisted > 10) {
                    event.cancelEvent()
                    packetsReceived += packet
                }
                if (event.eventType == EventState.SEND) {
                    event.cancelEvent()
                    packets += packet
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        positions += packetPos
                    }
                }
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // Clear packets on disconnect only
        if (event.worldClient == null) {
            packets.clear()
            packetsReceived.clear()
            positions.clear()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isDead || mc.thePlayer.ticksExisted <= 10) {
            blink()
        }

        when (mode.lowercase()) {
            "sent" -> {
                handlePackets(*packetsReceived.toTypedArray())
                packetsReceived.clear()
            }
            "received" -> {
                sendPackets(*packets.toTypedArray(), triggerEvents = false)
                packets.clear()
            }
        }

        if (pulse && pulseTimer.hasTimePassed(pulseDelay)) {
            blink()
            addFakePlayer()
            pulseTimer.reset()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!trail)
            return
        val color = trailColor

        synchronized(positions) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            glBegin(GL_LINE_STRIP)
            glColor(color)

            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            for (pos in positions)
                glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ)

            glColor4d(1.0, 1.0, 1.0, 1.0)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    override val tag
        get() = (packets.size + packetsReceived.size).toString()

    private fun blink() {
        handlePackets(*packetsReceived.toTypedArray())
        sendPackets(*packets.toTypedArray(), triggerEvents = false)

        packets.clear()
        packetsReceived.clear()
        positions.clear()

        // Remove fake player
        fakePlayer?.let {
            mc.theWorld?.removeEntityFromWorld(it.entityId)
            fakePlayer = null
        }
    }

    private fun addFakePlayer() {
        if (!fakePlayerMenu) return

        val thePlayer = mc.thePlayer ?: return

        val faker = EntityOtherPlayerMP(mc.theWorld, thePlayer.gameProfile)

        faker.rotationYawHead = thePlayer.rotationYawHead
        faker.renderYawOffset = thePlayer.renderYawOffset
        faker.copyLocationAndAnglesFrom(thePlayer)
        faker.rotationYawHead = thePlayer.rotationYawHead
        faker.inventory = thePlayer.inventory
        mc.theWorld.addEntityToWorld(-1337, faker)

        fakePlayer = faker

        // Add positions indicating a blink start
        // val pos = thePlayer.positionVector
        // positions += pos.addVector(.0, thePlayer.eyeHeight / 2.0, .0)
        // positions += pos
    }

    fun blinkingSend() = handleEvents() && (mode == "Sent" || mode == "Both")
    fun blinkingReceive() = handleEvents() && (mode == "Received" || mode == "Both")
}