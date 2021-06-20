package com.easternsauce.libgdxgame.ability

import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

class SlashAttack(val creature: Creature) extends MeleeAttack {

  override val id: String = "slash"

  override protected val baseChannelTime = 0.3f
  override protected val baseActiveTime = 0.3f
  private val numOfChannelFrames = 6
  private val numOfFrames = 6

  override var attackRange: Float = 0.9375f
  override protected var aimed: Boolean = false
  override protected var spriteWidth: Int = 40
  override protected var spriteHeight: Int = 40
  override protected var knockbackPower: Float = 20f
  override protected val cooldownTime: Float = 0.8f

  activeSound = Some(Assets.sound(Assets.attackSound))
  activeSoundVolume = Some(0.1f)

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
