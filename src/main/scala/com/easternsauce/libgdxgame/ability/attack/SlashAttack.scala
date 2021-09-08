package com.easternsauce.libgdxgame.ability.attack

import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

class SlashAttack private (val creature: Creature) extends MeleeAttack {

  val id: String = "slash"

  protected val baseChannelTime = 0.3f
  protected val baseActiveTime = 0.3f
  val numOfChannelFrames = 6
  val numOfFrames = 6

  var attackRange: Float = 0.9375f
  protected var aimed: Boolean = false
  protected var spriteWidth: Int = 40
  protected var spriteHeight: Int = 40
  protected var knockbackVelocity: Float = 20f
  protected val cooldownTime: Float = 0.8f

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

object SlashAttack {
  def apply(abilityCreature: Creature): SlashAttack = {
    new SlashAttack(abilityCreature)
  }
}
