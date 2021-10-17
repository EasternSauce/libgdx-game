package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

trait Life {
  this: Creature =>

  val maxLife = 100f
  var life: Float = _

  var lifeRegenerationTimer: EsTimer = EsTimer(true)
  var healingTimer: EsTimer = EsTimer()
  var healingTickTimer: EsTimer = EsTimer()

  protected val lifeRegeneration = 0.3f

  var healing = false
  protected val healingTickTime = 0.005f
  protected val healingItemHealTime = 3f
  var healingPower = 0f

  def atFullLife: Boolean = life >= maxLife

  def isAlive: Boolean = life > 0f

  def heal(healValue: Float): Unit = {
    if (life < maxLife) {
      val afterHeal = life + healValue
      life = Math.min(afterHeal, maxLife)

    }
  }

  // TODO move?
  def takeLifeDamage(
    damage: Float,
    immunityFrames: Boolean,
    dealtBy: Option[Creature] = None,
    attackKnockbackVelocity: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ): Unit = {
    if (isAlive) {
      val beforeHP = life

      val actualDamage = damage * 100f / (100f + totalArmor)

      if (life - actualDamage > 0) life -= actualDamage
      else life = 0f

      if (beforeHP != life && life == 0f) onDeath()

      if (immunityFrames) { // immunity frames on hit
        activateEffect("immune", 0.75f)
        // stagger on hit
        activateEffect("immobilized", 0.35f)
      }

      if (isKnockbackable) {
        knockbackVector = new Vector2(pos.x - sourceX, pos.y - sourceY).nor()
        if (attackKnockbackVelocity > 0f) {
          knockbackVelocity = attackKnockbackVelocity
          activateEffect("knockedBack", 0.15f)
        }
      }

      if (onGettingHitSound.nonEmpty && GameSystem.randomGenerator.nextFloat() < 0.3f) onGettingHitSound.get.play(0.1f)
    }
  }

  def renderLifeBar(batch: EsBatch): Unit = {
    val lifeBarHeight = 0.16f
    val lifeBarWidth = 2.0f
    val currentLifeBarWidth = lifeBarWidth * life / maxLife
    val barPosX = pos.x - lifeBarWidth / 2
    val barPosY = pos.y + getWidth / 2 + 0.3125f
    batch.shapeDrawer.filledRectangle(new Rectangle(barPosX, barPosY, lifeBarWidth, lifeBarHeight), Color.ORANGE)
    batch.shapeDrawer
      .filledRectangle(new Rectangle(barPosX, barPosY, currentLifeBarWidth, lifeBarHeight), Color.RED)

  }

  def startHealing(healingPower: Float): Unit = {
    healingTimer.restart()
    healingTickTimer.restart()
    healing = true
    this.healingPower = healingPower
  }

  def regenerateLife(): Unit = {
    if (healing) {
      if (healingTickTimer.time > healingTickTime) {
        heal(healingPower)
        healingTickTimer.restart()
      }
      if (healingTimer.time > healingItemHealTime || life >= maxLife)
        healing = false
    }
  }

  if (lifeRegenerationTimer.time > 0.5f) {
    heal(lifeRegeneration)
    lifeRegenerationTimer.restart()
  }
}
