package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, IceShard}
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters}
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}

case class IceShardAbility private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  soundParameters: SoundParameters = SoundParameters(),
  lastComponentFinishTime: Float = 0f,
  components: List[AbilityComponent] = List()
) extends ComposedAbility {
  val id = "ice_shard"
  override protected val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override protected val numOfComponents = 9

  override protected def onActiveStart(): AbilityParameters = {
    creature.takeStaminaDamage(25f)
    AbilityParameters()
  }

  override def createComponent(index: Int): AbilityComponent = {
    val facingVector =
      if (creature.asInstanceOf[Enemy].aggroedTarget.nonEmpty) {
        creature.facingVector.cpy
      } else {
        new Vector2(1.0f, 0.0f)
      }

    new IceShard(
      this,
      creature.pos.x,
      creature.pos.y,
      speed = 30f,
      startTime = 0.05f * index,
      facingVector.cpy.rotateDeg(20f * (index - 5))
    )
  }

  override def applyParams(params: AbilityParameters): IceShardAbility = {
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
