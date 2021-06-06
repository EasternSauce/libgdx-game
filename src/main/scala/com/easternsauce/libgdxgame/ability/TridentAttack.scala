package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature

class TridentAttack(val creature: Creature) extends MeleeAttack {

  var weaponSpeed: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.weaponSpeed.get
    else 1.0f

  private val baseChannelTime = 0.6f
  private val baseActiveTime = 0.275f
  private val numOfChannelFrames = 7
  private val numOfFrames = 11

  override protected val activeTime: Float = baseActiveTime / weaponSpeed
  override protected val channelTime: Float = baseChannelTime / weaponSpeed

  override var scale: Float = 2.0f
  override var attackRange: Float = 0.9375f
  override protected var aimed: Boolean = false
  override protected var spriteWidth: Int = 64
  override protected var spriteHeight: Int = 32
  override protected var knockbackPower: Float = 20f
  override protected val cooldownTime: Float = 0.7f

  override protected val abilitySound: Option[Sound] = Some(RpgGame.manager.get(AssetPaths.attackSound, classOf[Sound]))

  setupActiveAnimation(
    atlas = creature.game.atlas,
    regionName = "trident_thrust",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = baseActiveTime / numOfFrames
  )

  setupWindupAnimation(
    atlas = creature.game.atlas,
    regionName = "trident_thrust_windup",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfChannelFrames,
    frameDuration = baseChannelTime / numOfChannelFrames
  )
}
