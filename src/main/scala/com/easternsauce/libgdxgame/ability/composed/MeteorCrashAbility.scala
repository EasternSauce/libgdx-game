package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.creature.Creature
import com.softwaremill.quicklens.ModifyPimp

case class MeteorCrashAbility private (
  override val creature: Creature,
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
      creature = creature,
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
  override type Self = MeteorCrashAbility

  override val id = "meteor_crash"

  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime: Float = 12f

  override protected val numOfComponents = 30

  override def onChannellingStart(): Self = {
    val facingVector: Vector2 = creature.facingVector.nor()
    val meteors1 = for (i <- 0 until numOfComponents / 3) yield {
      Meteor(
        this,
        ComponentParameters(
          startTime = 0.1f * i,
          startX = creature.pos.x + (3.125f * (i + 1)) * facingVector.x,
          startY = creature.pos.y + (3.125f * (i + 1)) * facingVector.y,
          radius = 1.5625f + 0.09375f * i * i,
          speed = 2.5f
        )
      )
    }

    val meteors2 = for (i <- 0 until numOfComponents / 3) yield {
      val vector: Vector2 = facingVector.cpy()
      vector.setAngleDeg(vector.angleDeg() + 50)
      Meteor(
        this,
        ComponentParameters(
          startTime = 0.1f * i,
          startX = creature.pos.x + (3.125f * (i + 1)) * vector.x,
          startY = creature.pos.y + (3.125f * (i + 1)) * vector.y,
          radius = 1.5625f + 0.09375f * i * i,
          speed = 2.5f
        )
      )
    }

    val meteors3 = for (i <- 0 until numOfComponents / 3) yield {
      val vector: Vector2 = facingVector.cpy()
      vector.setAngleDeg(vector.angleDeg() - 50)
      Meteor(
        this,
        ComponentParameters(
          startTime = 0.1f * i,
          startX = creature.pos.x + (3.125f * (i + 1)) * vector.x,
          startY = creature.pos.y + (3.125f * (i + 1)) * vector.y,
          radius = 1.5625f + 0.09375f * i * i,
          speed = 2.5f
        )
      )
    }

    val components = meteors1 ++ meteors2 ++ meteors3

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: sideeffect
    creature.activateEffect("immobilized", lastComponentFinishTime)

    this.modify(_.components).setTo(components.toList)
  }

  override def onActiveStart(): Self = {
    val ability = super.onActiveStart().asInstanceOf[Self]

    creature.takeStaminaDamage(25f)

    ability
  }

  override def createComponent(index: Int): AbilityComponent = {
    val facingVector: Vector2 = creature.facingVector.nor()

    val vector: Vector2 = facingVector.cpy()
    // TODO: change angle based on index?
    vector.setAngleDeg(vector.angleDeg() + 50)

    Meteor(
      this,
      ComponentParameters(
        startTime = 0.1f * index,
        startX = creature.pos.x + (3.125f * (index + 1)) * vector.x,
        startY = creature.pos.y + (3.125f * (index + 1)) * vector.y,
        radius = 1.5625f + 0.09375f * index * index,
        speed = 2.5f
      )
    )
  }

  override def copy(
    creature: Creature,
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
    MeteorCrashAbility(
      creature = creature,
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
