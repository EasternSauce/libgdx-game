package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

case class DashAbility private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  soundParameters: SoundParameters = SoundParameters(),
  timerParameters: TimerParameters = TimerParameters(),
  dashVector: Vector2 = new Vector2(0f, 0f)
) extends Ability {
  override val id = "dash"
  override val cooldownTime: Float = 1.5f
  override lazy val activeTime: Float = 0.2f
  override lazy val channelTime: Float = 0f

  val speed = 60f

  override def onActiveStart(): AbilityParameters = {
    super.onActiveStart()

    val dashVector = new Vector2(creature.walkingVector.x * speed, creature.walkingVector.y * speed)

    // TODO: remove sideffect
    creature.activateEffect("immobilized", channelTime + activeTime)
    creature.takeStaminaDamage(35f)

    AbilityParameters(dashVector = Some(dashVector))
  }

  override def onUpdateActive(): AbilityParameters = {
    // TODO: remove sideffect
    creature.sustainVelocity(dashVector)

    AbilityParameters()
  }

  override def applyParams(params: AbilityParameters): DashAbility = {
    copy(
      creature = params.creature.getOrElse(creature),
      state = params.state.getOrElse(state),
      onCooldown = params.onCooldown.getOrElse(onCooldown),
      soundParameters = params.soundParameters.getOrElse(soundParameters),
      timerParameters = params.timerParameters.getOrElse(timerParameters),
      dashVector = params.dashVector.getOrElse(dashVector)
    )
  }

  override def updateHitbox(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onUpdateChanneling(): AbilityParameters = {
    AbilityParameters()
  }

  override def render(esBatch: EsBatch): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onStop(): AbilityParameters = {
    AbilityParameters()
  }

  override def onCollideWithCreature(creature: Creature): AbilityParameters = {
    AbilityParameters()
  }
}
