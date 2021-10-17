package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.components.{AbilityComponent, Fist}
import com.easternsauce.libgdxgame.ability.misc.parameters._
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.templates.ComposedAbility
import com.easternsauce.libgdxgame.creature.Enemy
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.AbilityInfo

case class FistSlamAbility private (
  override val creatureId: String,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(),
  override val dirVector: Vector2 = new Vector2(0f, 0f)
) extends ComposedAbility(
      creatureId = creatureId,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      timerParameters = timerParameters,
      soundParameters = soundParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    ) {
  override type Self = FistSlamAbility

  override val id: String = FistSlamAbility.id

  override protected val cooldownTime: Float = 10f
  override protected lazy val channelTime: Float = 0.15f

  override protected val numOfComponents = 20

  override def onActiveStart(): Self = {

    // TODO: sideeffect
    modifyCreature(creature => { creature.takeStaminaDamage(25f); creature })

    this
  }

  override def createComponent(index: Int): AbilityComponent = {
    val range: Float = 7.8125f
    val aggroedCreature = creature.asInstanceOf[Enemy].aggroedTarget.get // TODO targeting?
    Fist(
      mainAbility = this,
      componentParameters = ComponentParameters(
        startTime = 0.1f * index,
        startX = aggroedCreature.pos.x + GameSystem.randomGenerator.between(-range, range),
        startY = aggroedCreature.pos.y + GameSystem.randomGenerator.between(-range, range),
        radius = 2f
      )
    )
  }

  override def copy(
    creatureId: String,
    state: AbilityState,
    onCooldown: Boolean,
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self =
    FistSlamAbility(
      creatureId = creatureId,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      soundParameters = soundParameters,
      timerParameters = timerParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    )
}

object FistSlamAbility extends AbilityInfo {
  override val id = "fist_slam"
}
