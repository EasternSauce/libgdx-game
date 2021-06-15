package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

trait Life {
  this: Creature =>

  var maxHealthPoints = 100f
  var healthPoints: Float = maxHealthPoints

  protected val healthRegenTimer: EsTimer = EsTimer(true)
  protected val healingTimer: EsTimer = EsTimer()
  protected val healingTickTimer: EsTimer = EsTimer()

  protected val healthRegen = 0.3f

  protected var healing = false
  protected val healingTickTime = 0.005f
  protected val healingItemHealTime = 3f
  protected var healingPower = 0f

  def atFullLife: Boolean = healthPoints >= maxHealthPoints

  def alive: Boolean = healthPoints > 0f

  def heal(healValue: Float): Unit = {
    if (healthPoints < maxHealthPoints) {
      val afterHeal = healthPoints + healValue
      healthPoints = Math.min(afterHeal, maxHealthPoints)

    }
  }

  def takeHealthDamage(
    damage: Float,
    immunityFrames: Boolean,
    dealtBy: Option[Creature] = None,
    knockbackPower: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ): Unit = {
    if (alive) {
      val beforeHP = healthPoints

      val actualDamage = damage * 100f / (100f + totalArmor)

      if (healthPoints - actualDamage > 0) healthPoints -= actualDamage
      else healthPoints = 0f

      if (beforeHP != healthPoints && healthPoints == 0f) onDeath()

      if (immunityFrames) { // immunity frames on hit
        effect("immune").applyEffect(0.75f)
        // stagger on hit
        effect("immobilized").applyEffect(0.35f)
      }

      if (knockbackable) {
        knockbackVector = new Vector2(pos.x - sourceX, pos.y - sourceY).nor()
        knockbackSpeed = knockbackPower
        effect("knockedBack").applyEffect(0.15f)
      }

      if (onGettingHitSound.nonEmpty) onGettingHitSound.get.play(0.1f)
    }
  }

  def renderHealthBar(batch: EsBatch): Unit = {
    val healthBarHeight = 0.16f
    val healthBarWidth = 2.0f
    val currentHealthBarWidth = healthBarWidth * healthPoints / maxHealthPoints
    val barPosX = pos.x - healthBarWidth / 2
    val barPosY = pos.y + getWidth / 2 + 0.3125f
    batch.shapeDrawer.filledRectangle(new Rectangle(barPosX, barPosY, healthBarWidth, healthBarHeight), Color.ORANGE)
    batch.shapeDrawer
      .filledRectangle(new Rectangle(barPosX, barPosY, currentHealthBarWidth, healthBarHeight), Color.RED)

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
      if (healingTimer.time > healingItemHealTime || healthPoints >= maxHealthPoints)
        healing = false
    }
  }

  if (healthRegenTimer.time > 0.5f) {
    heal(healthRegen)
    healthRegenTimer.restart()
  }
}
