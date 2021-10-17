package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.ability.misc.parameters._
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.templates.ComposedAbility
import com.easternsauce.libgdxgame.util.AbilityInfo

case class BubbleAbility private (
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
  override type Self = BubbleAbility

  override val id: String = BubbleAbility.id
  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override protected val numOfComponents = 3

  override def onActiveStart(): Self = {

    // TODO: sideeffects
    modifyCreature(creature => { creature.takeStaminaDamage(25f); creature })

    this
  }

  override def createComponent(index: Int): AbilityComponent = {
    Bubble(
      this,
      componentParameters = ComponentParameters(
        startX = creature.pos.x,
        startY = creature.pos.y,
        radius = 4f,
        speed = 30f,
        startTime = 0.4f * index
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
    BubbleAbility(
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

object BubbleAbility extends AbilityInfo {
  override val id = "bubble"

}
