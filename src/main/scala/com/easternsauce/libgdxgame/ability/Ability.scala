package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

trait Ability {
  val creature: Creature
  protected val isStoppable: Boolean = true
  var state: AbilityState = Inactive
  var onCooldown = false
  protected val activeTimer: EsTimer = EsTimer()
  protected val channelTimer: EsTimer = EsTimer()
  protected val cooldownTime: Float
  protected val activeTime: Float
  protected val channelTime: Float
  protected val isAttack = false

  protected val abilitySound: Option[Sound] = None

  def updateHitbox(): Unit = {}

  protected def onActiveStart(): Unit = {}

  protected def onUpdateActive(): Unit = {}

  protected def onUpdateChanneling(): Unit = {}

  def render(esBatch: EsBatch): Unit = {}

  def forceStop(): Unit = {
    if (isStoppable && state != AbilityState.Inactive) {
      onStop()

      state = AbilityState.Inactive
    }
  }

  protected def onStop(): Unit = {}

  def perform(): Unit = {
    if (creature.staminaPoints > 0 && (state == AbilityState.Inactive) && !onCooldown) {
      channelTimer.restart()
      state = AbilityState.Channeling
      onChannellingStart()

      if (isAttack) { // + 0.01 to ensure regen doesn't start if we hold attack button
        creature
          .effect("staminaRegenStopped")
          .applyEffect(channelTime + cooldownTime + 0.01f)
      } else creature.effect("staminaRegenStopped").applyEffect(1f)
    }
  }

  def update(): Unit = {}

  def onChannellingStart(): Unit = {}

  def performMovement(): Unit = {}

  def active: Boolean = {
    state == AbilityState.Active
  }

  def onCollideWithCreature(creature: Creature): Unit = {}
}
