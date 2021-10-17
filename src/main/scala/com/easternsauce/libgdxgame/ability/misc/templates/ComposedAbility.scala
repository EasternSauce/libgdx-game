package com.easternsauce.libgdxgame.ability.misc.templates

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

abstract class ComposedAbility(
  override val creatureId: String,
  override val state: AbilityState = Inactive,
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

  override def update(): Self = {
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
            .onActiveStart()

        } else
          this

        ability
          .onUpdateChanneling()

      case Active =>
        //stop when all components are stopped
        val ability =
          if (activeTimer.time > lastComponentFinishTime)
            this
              .modify(_.state)
              .setTo(AbilityState.Inactive)
              .onStop()
          else
            this

        ability
          .onUpdateActive()

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

  override def render(batch: EsBatch): Self = {
    this
      .modify(_.components.each)
      .usingIf(this.state == AbilityState.Active)(_.render(batch))

  }

  override def onUpdateActive(): Self = {
    val activeTimer = timerParameters.activeTimer

    val condition: AbilityComponent => Boolean = component =>
      !component.started && activeTimer.time > component.componentParameters.startTime

    this
      .modify(_.components.eachWhere(condition))
      .using(_.start())
      .modify(_.components.each)
      .using(_.onUpdateActive())
  }

  override def onChannellingStart(): Self = {

    val components = for (i <- 0 until numOfComponents) yield createComponent(i)

    val lastComponent = components.maxBy(_.totalTime)
    val lastComponentFinishTime = lastComponent.totalTime

    // TODO: remove side effect
    modifyCreature(creature => { creature.activateEffect("immobilized", lastComponentFinishTime); creature })

    this
      .modify(_.components)
      .setTo(components.toList)
      .modify(_.lastComponentFinishTime)
      .setTo(lastComponentFinishTime)
  }

  def createComponent(index: Int): AbilityComponent = ???

}