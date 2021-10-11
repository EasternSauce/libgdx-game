package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem

case class MeteorRainAbility private (
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
  override type Self = MeteorRainAbility

  override val id = "meteor_rain"

  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 35f
  protected val explosionRange: Float = 9.375f

  override def onActiveStart(creature: Creature): Self = {
    creature.takeStaminaDamage(25f)
    this
  }

  override def createComponent(creature: Creature, index: Int): AbilityComponent = {
    val range = 34.375f

    val component = Meteor(
      creatureId = creatureId,
      mainAbility = this,
      componentParameters = ComponentParameters(
        startTime = 0.15f * index,
        startX = creature.pos.x + GameSystem.randomGenerator.between(-range, range),
        startY = creature.pos.y + GameSystem.randomGenerator.between(-range, range),
        radius = explosionRange,
        speed = 1.5f
      )
    )

    // TODO: this is a workaround
    component.bodyParameters.b2Body.get.setUserData((this, creature))

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
    MeteorRainAbility(
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
