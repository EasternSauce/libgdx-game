package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Bubble}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.collision.AbilityCollision
import com.easternsauce.libgdxgame.creature.Creature

case class BubbleAbility private (
  override val creatureId: String,
  override val state: AbilityState = Inactive,
  override val creatureOperations: List[Creature => Creature] = List(),
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
      creatureOperations = creatureOperations,
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

  override val id = "bubble"
  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override protected val numOfComponents = 3

  override def onActiveStart(creature: Creature): Self = {

    // TODO: sideeffects
    creature.takeStaminaDamage(25f)

    this
  }

  override def createComponent(creature: Creature, index: Int): AbilityComponent = {
    val component = Bubble(
      creatureId = creatureId,
      mainAbility = this,
      componentParameters = ComponentParameters(
        startX = creature.pos.x,
        startY = creature.pos.y,
        radius = 4f,
        speed = 30f,
        startTime = 0.4f * index
      )
    )

    // TODO: this is a workaround
    component.bodyParameters.b2Body.get.setUserData(AbilityCollision(creatureId, id))

    component
  }

  override def copy(
    creatureId: String,
    state: AbilityState,
    creatureOperations: List[Creature => Creature] = creatureOperations,
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
      creatureOperations = creatureOperations,
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
