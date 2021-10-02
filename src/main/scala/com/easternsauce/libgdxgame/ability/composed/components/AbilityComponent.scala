package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.Modification
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, ComponentParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

trait AbilityComponent {

  val componentParameters: ComponentParameters

  protected val activeTimer: EsTimer = EsTimer()
  protected val channelTimer: EsTimer = EsTimer()
  val activeTime: Float
  val channelTime: Float

  val state: AbilityState
  val started: Boolean
  val body: Option[Body]
  val destroyed: Boolean
  val dirVector: Vector2


  val timerParameters: TimerParameters = TimerParameters()
  val animationParameters: AnimationParameters = AnimationParameters()
  val activeAnimation: Option[Animation]
  val channelAnimation: Option[Animation]

  def onUpdateActive(): AbilityComponent

  def render(batch: EsBatch): AbilityComponent

  def onCollideWithCreature(creature: Creature): AbilityComponent

  def start(): AbilityComponent

  def totalTime: Float = componentParameters.startTime + channelTime + activeTime + 0.05f // with buffer

}
