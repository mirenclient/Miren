/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.StrafeEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.HitBox;
import net.ccbluex.liquidbounce.features.module.modules.misc.ViaVersionFix;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public float rotationPitch;

    @Shadow
    public float rotationYaw;

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public abstract float getDistanceToEntity(Entity entityIn);

    @Shadow
    public Entity ridingEntity;

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public boolean onGround;

    @Shadow
    public boolean isAirBorne;

    @Shadow
    public boolean noClip;

    @Shadow
    public World worldObj;

    @Shadow
    public void moveEntity(double x, double y, double z) {
    }

    @Shadow
    public boolean isInWeb;

    @Shadow
    public float stepHeight;

    @Shadow
    public boolean isCollidedHorizontally;

    @Shadow
    public boolean isCollidedVertically;

    @Shadow
    public boolean isCollided;

    @Shadow
    public float distanceWalkedModified;

    @Shadow
    public float distanceWalkedOnStepModified;

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    protected Random rand;

    @Shadow
    public int fireResistance;

    @Shadow
    protected boolean inPortal;

    @Shadow
    public int timeUntilPortal;

    @Shadow
    public float width;

    @Shadow
    public abstract boolean isRiding();

    @Shadow
    public abstract void setFire(int seconds);

    @Shadow
    protected abstract void dealFireDamage(int amount);

    @Shadow
    public abstract boolean isWet();

    @Shadow
    public abstract void addEntityCrashInfo(CrashReportCategory category);

    @Shadow
    protected abstract void doBlockCollisions();

    @Shadow
    protected abstract void playStepSound(BlockPos pos, Block blockIn);

    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB bb);

    @Shadow
    private int nextStepDistance;

    @Shadow
    private int fire;

    @Shadow
    public float prevRotationPitch;

    @Shadow
    public float prevRotationYaw;

    @Shadow
    protected abstract Vec3 getVectorForRotation(float pitch, float yaw);

    @Shadow
    public abstract UUID getUniqueID();

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract boolean isInsideOfMaterial(Material materialIn);

    @Shadow(remap = false) private CapabilityDispatcher capabilities;

    public int getNextStepDistance() {
        return nextStepDistance;
    }

    public void setNextStepDistance(int nextStepDistance) {
        this.nextStepDistance = nextStepDistance;
    }

    public int getFire() {
        return fire;
    }

    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    private void getCollisionBorderSize(final CallbackInfoReturnable<Float> callbackInfoReturnable) {
        final HitBox hitBox = LiquidBounce.moduleManager.getModule(HitBox.class);
        final ViaVersionFix viaVersionFix = LiquidBounce.moduleManager.getModule(ViaVersionFix.class);

        if (hitBox.getState() && EntityUtils.isSelected(((Entity)((Object)this)),true)) {
            if (viaVersionFix.getState()) {
                callbackInfoReturnable.setReturnValue(hitBox.getSizeValue().get());
            } else {
                callbackInfoReturnable.setReturnValue(0.1F + hitBox.getSizeValue().get());
            }
        } else if (viaVersionFix.getState()) {
            callbackInfoReturnable.setReturnValue(0.0F);
        }
    }

    @Inject(method = "moveFlying", at = @At("HEAD"), cancellable = true)
    private void handleRotations(float strafe, float forward, float friction, final CallbackInfo callbackInfo) {
        if ((Object) this != Minecraft.getMinecraft().thePlayer)
            return;

        final StrafeEvent strafeEvent = new StrafeEvent(strafe, forward, friction, rotationYaw);
        LiquidBounce.eventManager.callEvent(strafeEvent);

        if (strafeEvent.isCancelled())
            callbackInfo.cancel();
    }

    @Redirect(method = "getBrightnessForRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean alwaysReturnTrue(World world, BlockPos pos) {
        return true;
    }

    @Inject(method = "spawnRunningParticles", at = @At("HEAD"), cancellable = true)
    private void checkGroundState(CallbackInfo ci) {
        if (!this.onGround) ci.cancel();
    }

    /**
     * @author asbyth
     * @reason Faster capability check
     */
    @Overwrite(remap = false)
    public boolean hasCapability(Capability<?> capability, EnumFacing direction) {
        return this.capabilities != null && this.capabilities.hasCapability(capability, direction);
    }
}
