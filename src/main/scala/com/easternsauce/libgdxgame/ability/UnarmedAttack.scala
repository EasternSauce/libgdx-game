package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

class UnarmedAttack(val creature: Creature) extends MeleeAttack {

  var weaponSpeed: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.weaponSpeed.get
    else 1.0f

  private val baseChannelTime = 0.3f
  private val baseActiveTime = 0.3f
  private val numOfChannelFrames = 6
  private val numOfFrames = 6

  override protected val activeTime: Float = baseActiveTime / weaponSpeed
  override protected val channelTime: Float = baseChannelTime / weaponSpeed

  override var scale: Float = 1.3f
  override var attackRange: Float = 0.9375f
  override protected var aimed: Boolean = false
  override protected var spriteWidth: Int = 40
  override protected var spriteHeight: Int = 40
  override protected var knockbackPower: Float = 20f
  override protected val cooldownTime: Float = 0.8f

  override protected val abilitySound: Option[Sound] = Some(Assets.sound(Assets.attackSound))

  setupActiveAnimation(
    regionName = "slash",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = baseActiveTime / numOfFrames
  )

  setupWindupAnimation(
    regionName = "slash_windup",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfChannelFrames,
    frameDuration = baseChannelTime / numOfChannelFrames
  )
}
