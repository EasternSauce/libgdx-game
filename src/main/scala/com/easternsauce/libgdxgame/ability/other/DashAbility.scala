package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature

case class DashAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  dashVector: Vector2 = new Vector2(0f, 0f)
) extends Ability(creature = creature, state = state, onCooldown = onCooldown, timerParameters = timerParameters) {
  override val id = "dash"
  override val cooldownTime: Float = 1.5f
  override lazy val activeTime: Float = 0.2f
  override lazy val channelTime: Float = 0f

  val speed = 60f

  override def onActiveStart(): DashAbility = {
    super.onActiveStart()

    val dashVector = new Vector2(creature.walkingVector.x * speed, creature.walkingVector.y * speed)

    // TODO: remove sideffect
    creature.activateEffect("immobilized", channelTime + activeTime)
    creature.takeStaminaDamage(35f)

    copy(dashVector = dashVector)
  }

  override def onUpdateActive(): DashAbility = {
    // TODO: remove sideffect
    creature.sustainVelocity(dashVector)

    copy()
  }

}
