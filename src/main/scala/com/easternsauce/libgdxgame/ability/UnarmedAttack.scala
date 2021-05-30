package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.easternsauce.libgdxgame.creature.traits.Creature

class UnarmedAttack(val creature: Creature) extends MeleeAttack {

  var weaponSpeed: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.weaponSpeed.get
    else 1.0f

  private val baseChannelTime = 0.3f
  private val baseActiveTime = 0.3f
  private val numOfChannelFrames = 6
  private val numOfFrames = 6

  override protected val activeTime: Float = baseActiveTime * 1f / weaponSpeed
  override protected val channelTime: Float = 0.3f
  override protected var abilityActiveAnimation: Animation[TextureRegion] = _
  override protected var abilityWindupAnimation: Animation[TextureRegion] = _

  override var scale: Float = 1.0f
  override var attackRange: Float = 0.9375f
  override protected var aimed: Boolean = false
  override protected var spriteWidth: Int = 40
  override protected var spriteHeight: Int = 40
  override protected var knockbackPower: Float = 10f
  override protected val cooldownTime: Float = 0.8f

  setupActiveAnimation(
    atlas = creature.screen.atlas,
    regionName = "slash",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfChannelFrames,
    frameDuration = baseChannelTime / numOfChannelFrames
  )

  setupWindupAnimation(
    atlas = creature.screen.atlas,
    regionName = "slash_windup",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = baseActiveTime / numOfFrames
  )
}
