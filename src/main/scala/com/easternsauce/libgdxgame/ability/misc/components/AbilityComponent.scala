package com.easternsauce.libgdxgame.ability.misc.components

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.parameters.{AnimationParameters, BodyParameters, ComponentParameters, TimerParameters}
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.templates.{Ability, AbilityState}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

abstract class AbilityComponent(
  val mainAbility: Ability,
  val state: AbilityState = AbilityState.Inactive,
  val started: Boolean = false,
  val componentParameters: ComponentParameters = ComponentParameters(),
  val timerParameters: TimerParameters = TimerParameters(),
  val animationParameters: AnimationParameters = AnimationParameters(),
  val bodyParameters: BodyParameters = BodyParameters(),
  val dirVector: Vector2 = new Vector2(0, 0)
) {
  type Self >: this.type <: AbilityComponent

  val activeTime: Float
  val channelTime: Float

  val activeAnimation: Option[Animation]
  val channelAnimation: Option[Animation]

  def onUpdateActive(): Self

  def render(batch: EsBatch): Self

  def onCollideWithCreature(creature: Creature): Self

  def start(): Self

  def totalTime: Float = componentParameters.startTime + channelTime + activeTime + 0.05f // with buffer

}
