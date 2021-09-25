package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.{AbilityComponent, IceShard}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}

case class IceShardAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f
) extends ComposedAbility {
  override val id = "ice_shard"

  override protected lazy val channelTime: Float = 0.05f
  override protected val cooldownTime = 5f

  override protected val numOfComponents = 9

  override def onActiveStart(): IceShardAbility = {
    creature.takeStaminaDamage(25f)
    copy()
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

  override def setComponents(components: List[AbilityComponent]): IceShardAbility = copy(components = components)

  override def setLastComponentFinishTime(lastComponentFinishTime: Float): IceShardAbility =
    copy(lastComponentFinishTime = lastComponentFinishTime)

  override def setState(state: AbilityState): IceShardAbility = copy(state = state)

  override def setOnCooldown(onCooldown: Boolean): IceShardAbility = copy(onCooldown = onCooldown)
}
