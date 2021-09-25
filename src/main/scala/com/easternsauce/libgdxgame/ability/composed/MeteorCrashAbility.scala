package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature

case class MeteorCrashAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f
) extends ComposedAbility {

  implicit def toMeteorCrashAbility(ability: Ability): MeteorCrashAbility = ability.asInstanceOf[MeteorCrashAbility]

  override val id = "meteor_crash"

  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime: Float = 12f

  override protected val numOfComponents = 30

  override def onChannellingStart(): MeteorCrashAbility = {
    val facingVector: Vector2 = creature.facingVector.nor()
    val meteors1 = for (i <- 0 until numOfComponents / 3) yield {
      new Meteor(
        this,
        0.1f * i,
        creature.pos.x + (3.125f * (i + 1)) * facingVector.x,
        creature.pos.y + (3.125f * (i + 1)) * facingVector.y,
        1.5625f + 0.09375f * i * i,
        2.5f
      )
    }

    val meteors2 = for (i <- 0 until numOfComponents / 3) yield {
      val vector: Vector2 = facingVector.cpy()
      vector.setAngleDeg(vector.angleDeg() + 50)
      new Meteor(
        this,
        0.1f * i,
        creature.pos.x + (3.125f * (i + 1)) * vector.x,
        creature.pos.y + (3.125f * (i + 1)) * vector.y,
        1.5625f + 0.09375f * i * i,
        2.5f
      )
    }

    val meteors3 = for (i <- 0 until numOfComponents / 3) yield {
      val vector: Vector2 = facingVector.cpy()
      vector.setAngleDeg(vector.angleDeg() - 50)
      new Meteor(
        this,
        0.1f * i,
        creature.pos.x + (3.125f * (i + 1)) * vector.x,
        creature.pos.y + (3.125f * (i + 1)) * vector.y,
        1.5625f + 0.09375f * i * i,
        2.5f
      )
    }

    val components = meteors1 ++ meteors2 ++ meteors3

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: sideeffect
    creature.activateEffect("immobilized", lastComponentFinishTime)

    copy(components = components.toList)
  }

  override def onActiveStart(): MeteorCrashAbility = {
    val ability = super.onActiveStart()

    creature.takeStaminaDamage(25f)

    ability
  }

  override def createComponent(index: Int): AbilityComponent = {
    val facingVector: Vector2 = creature.facingVector.nor()

    val vector: Vector2 = facingVector.cpy()
    // TODO: change angle based on index?
    vector.setAngleDeg(vector.angleDeg() + 50)

    new Meteor(
      this,
      0.1f * index,
      creature.pos.x + (3.125f * (index + 1)) * vector.x,
      creature.pos.y + (3.125f * (index + 1)) * vector.y,
      1.5625f + 0.09375f * index * index,
      2.5f
    )
  }

  override def setComponents(components: List[AbilityComponent]): MeteorCrashAbility = copy(components = components)

  override def setLastComponentFinishTime(lastComponentFinishTime: Float): MeteorCrashAbility =
    copy(lastComponentFinishTime = lastComponentFinishTime)

  override def setState(state: AbilityState): MeteorCrashAbility = copy(state = state)

  override def setOnCooldown(onCooldown: Boolean): MeteorCrashAbility = copy(onCooldown = onCooldown)
}
