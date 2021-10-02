package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Fist}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem

case class FistSlamAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f
) extends ComposedAbility {
  override type Self = FistSlamAbility

  override val id: String = "fist_slam"

  override protected val cooldownTime: Float = 10f
  override protected lazy val channelTime: Float = 0.15f

  override protected val numOfComponents = 20

  override def onActiveStart(): Self = {

    // TODO: sideeffect
    creature.takeStaminaDamage(25f)

    this
  }

  override def createComponent(index: Int): AbilityComponent = {
    val range: Float = 7.8125f
    val aggroedCreature = creature.asInstanceOf[Enemy].aggroedTarget.get // TODO targeting?
    Fist(
      this,
      ComponentParameters(
        startTime = 0.1f * index,
        startX = aggroedCreature.pos.x + GameSystem.randomGenerator.between(-range, range),
        startY = aggroedCreature.pos.y + GameSystem.randomGenerator.between(-range, range),
        radius = 2f
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
    animationParameters: AnimationParameters, dirVector: Vector2
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
