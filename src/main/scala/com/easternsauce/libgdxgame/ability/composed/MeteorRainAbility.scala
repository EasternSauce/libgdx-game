package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem

case class MeteorRainAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f
) extends ComposedAbility {
  override type Self = MeteorRainAbility

  override val id = "meteor_rain"

  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 35f
  protected val explosionRange: Float = 9.375f

  override def onActiveStart(): Self = {
    creature.takeStaminaDamage(25f)
    this
  }

  override def createComponent(index: Int): AbilityComponent = {
    val range = 34.375f

    Meteor(
      this,
      ComponentParameters(
        startTime = 0.15f * index,
        startX = creature.pos.x + GameSystem.randomGenerator.between(-range, range),
        startY = creature.pos.y + GameSystem.randomGenerator.between(-range, range),
        radius = explosionRange,
        speed = 1.5f
      )
    )
  }

  override def copy(
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    state: AbilityState,
    onCooldown: Boolean,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self =
    copy(
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      state = state,
      onCooldown = onCooldown,
      soundParameters = soundParameters,
      timerParameters = timerParameters
    )
}
