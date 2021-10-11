package com.easternsauce.libgdxgame.ability.composed

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{
  AnimationParameters,
  BodyParameters,
  SoundParameters,
  TimerParameters
}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

abstract class ComposedAbility(
  override val creatureId: String,
  override val state: AbilityState = Inactive,
  override val creatureOperations: List[Creature => Creature] = List(),
  override val onCooldown: Boolean = false,
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(),
  override val dirVector: Vector2 = new Vector2(0f, 0f)
) extends Ability(
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
  type Self >: this.type <: ComposedAbility

  implicit def toComposedAbility(ability: Ability): Self = ability.asInstanceOf[Self]

  protected val numOfComponents: Int = -1

  override protected lazy val activeTime: Float = 0

  override def update(creature: Creature): Self = {
    val channelTimer = timerParameters.channelTimer
    val activeTimer = timerParameters.activeTimer

    import AbilityState._

    val res: Self = state match {
      case Channeling =>
        val ability: Self = if (channelTimer.time > channelTime) {
          activeTimer.restart()

          this
            .modify(_.state)
            .setTo(AbilityState.Active)
            .modify(_.onCooldown)
            .setTo(true)
            .onActiveStart(creature)

        } else
          this

        ability
          .onUpdateChanneling(creature)

      case Active =>
        //stop when all components are stopped
        val ability =
          if (activeTimer.time > lastComponentFinishTime)
            this
              .modify(_.state)
              .setTo(AbilityState.Inactive)
              .onStop(creature)
          else
            this

        ability
          .onUpdateActive(creature)

      case Inactive if onCooldown =>
        if (activeTimer.time > cooldownTime) {
          this
            .modify(_.onCooldown)
            .setTo(false)
        } else
          this
      case _ => this
    }

    res
  }

  override def render(creature: Creature, batch: EsBatch): Self = {
    this
      .modify(_.components.each)
      .usingIf(this.state == AbilityState.Active)(_.render(creature, batch))

  }

  override def onUpdateActive(creature: Creature): Self = {
    val activeTimer = timerParameters.activeTimer

    val condition: AbilityComponent => Boolean = component =>
      !component.started && activeTimer.time > component.componentParameters.startTime

    this
      .modify(_.components.eachWhere(condition))
      .using(_.start())
      .modify(_.components.each)
      .using(_.onUpdateActive(creature))
  }

  override def onChannellingStart(creature: Creature): Self = {

    val components = for (i <- 0 until numOfComponents) yield createComponent(creature, i)

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: remove side effect
    creature.activateEffect("immobilized", lastComponentFinishTime)

    this
      .modify(_.components)
      .setTo(components.toList)
      .modify(_.lastComponentFinishTime)
      .setTo(lastComponentFinishTime)
  }

  def createComponent(creature: Creature, index: Int): AbilityComponent = ???

}
