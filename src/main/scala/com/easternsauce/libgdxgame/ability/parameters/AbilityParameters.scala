package com.easternsauce.libgdxgame.ability.parameters

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.attack.AttackHitbox
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.creature.Creature

case class AbilityParameters(
  creature: Option[Creature] = None,
  state: Option[AbilityState] = None,
  onCooldown: Option[Boolean] = None,
  dashVector: Option[Vector2] = None,
  components: Option[List[AbilityComponent]] = None,
  lastComponentFinishTime: Option[Float] = None,
  b2Body: Option[Body] = None,
  bodyCreated: Option[Boolean] = None,
  hitbox: Option[Option[AttackHitbox]] = None,
  toRemoveBody: Option[Boolean] = None,
  bodyActive: Option[Boolean] = None,
  soundParameters: Option[SoundParameters] = None,
  timerParameters: Option[TimerParameters] = None
) {
  def add(abilityParameters: AbilityParameters): AbilityParameters = {

    def ifNonEmpty[T](otherParam: Option[T], thisParam: Option[T]): Option[T] =
      if (otherParam.nonEmpty) otherParam else thisParam

    AbilityParameters(
      creature = ifNonEmpty(abilityParameters.creature, creature),
      state = ifNonEmpty(abilityParameters.state, state),
      onCooldown = ifNonEmpty(abilityParameters.onCooldown, onCooldown),
      dashVector = ifNonEmpty(abilityParameters.dashVector, dashVector),
      components = ifNonEmpty(abilityParameters.components, components),
      lastComponentFinishTime = ifNonEmpty(abilityParameters.lastComponentFinishTime, lastComponentFinishTime),
      b2Body = ifNonEmpty(abilityParameters.b2Body, b2Body),
      bodyCreated = ifNonEmpty(abilityParameters.bodyCreated, bodyCreated),
      hitbox = ifNonEmpty(abilityParameters.hitbox, hitbox),
      toRemoveBody = ifNonEmpty(abilityParameters.toRemoveBody, toRemoveBody),
      bodyActive = ifNonEmpty(abilityParameters.bodyActive, bodyActive)
    )
  }
}
