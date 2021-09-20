package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, Meteor}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters}
import com.easternsauce.libgdxgame.creature.Creature

import scala.collection.mutable.ListBuffer

case class MeteorCrashAbility private (
   creature: Creature,
   state: AbilityState = Inactive,
   onCooldown: Boolean = false,
  soundParameters: SoundParameters = SoundParameters(),
   components: List[AbilityComponent] = List(),
   lastComponentFinishTime: Float = 0f
) extends ComposedAbility {
  val id = "meteor_crash"
  override protected val channelTime: Float = 0.05f
  override protected val cooldownTime: Float = 12f

  override protected val numOfComponents = 30

  override def onChannellingStart(): AbilityParameters = {
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


    AbilityParameters(components = Some(components.toList))
  }

  override protected def onActiveStart(): AbilityParameters = {
    creature.takeStaminaDamage(25f)

    AbilityParameters()
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

  override def applyParams(params: AbilityParameters): MeteorCrashAbility = {
    copy(
      creature = params.creature.getOrElse(creature),
      state = params.state.getOrElse(state),
      onCooldown = params.onCooldown.getOrElse(onCooldown),
      lastComponentFinishTime = params.lastComponentFinishTime.getOrElse(lastComponentFinishTime),
      components = params.components.getOrElse(components)
    )
  }

  override def updateHitbox(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onUpdateChanneling(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onStop(): AbilityParameters = {
    AbilityParameters()
  }

  override def onCollideWithCreature(creature: Creature): AbilityParameters = {
    AbilityParameters()
  }
}
