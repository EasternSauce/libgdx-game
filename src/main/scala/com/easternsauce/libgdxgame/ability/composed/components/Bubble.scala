package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, BodyParameters, ComponentParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

case class Bubble(
  override val mainAbility: Ability,
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters =
    AnimationParameters(textureWidth = 64, textureHeight = 64, activeRegionName = "bubble", activeFrameCount = 2),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val dirVector: Vector2 = new Vector2(0, 0)
) extends AbilityComponent(
      mainAbility = mainAbility,
      state = state,
      started = started,
      componentParameters = componentParameters,
      timerParameters = timerParameters,
      animationParameters = animationParameters,
      bodyParameters = bodyParameters,
      dirVector = dirVector
    ) {
  type Self = Bubble

  override lazy val activeTime: Float = 1.5f
  override lazy val channelTime: Float = 0.6f

  val loopTime = 0.2f

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = None

  def start(): Self = {
    timerParameters.channelTimer.restart()

    this
      .modify(_.started)
      .setTo(true)
      .modify(_.state)
      .setTo(AbilityState.Channeling)
  }

  override def onUpdateActive(): Self = {
    modifyIf(started) {
      state match {
        case AbilityState.Channeling =>
          modifyIf(timerParameters.channelTimer.time > channelTime) { onActiveStart() }
        case AbilityState.Active =>
          val component: Bubble =
            this
              .modifyIf(!bodyParameters.destroyed && timerParameters.activeTimer.time >= activeTime) {
                bodyParameters.body.get.getWorld.destroyBody(bodyParameters.body.get)
                this
                  .modify(_.bodyParameters.destroyed)
                  .setTo(true)
              }
              .modifyIf(timerParameters.activeTimer.time > activeTime) {
                // on active stop
                this
                  .modify(_.state)
                  .setTo(AbilityState.Inactive)
              }

          if (!bodyParameters.destroyed) {
            bodyParameters.body.get
              .setLinearVelocity(dirVector.x * componentParameters.speed, dirVector.y * componentParameters.speed)
          }

          component

        case _ => this
      }
    }

  }

  private def onActiveStart(): Self = {
    //Assets.sound(Assets.explosionSound).play(0.01f)

    timerParameters.abilityActiveAnimationTimer.restart()

    timerParameters.activeTimer.restart()

    val body = initBody(componentParameters.startX, componentParameters.startY)
    val dirVector = if (mainAbility.creature.asInstanceOf[Enemy].aggroedTarget.nonEmpty) {
      mainAbility.creature.facingVector.cpy
    } else {
      new Vector2(1.0f, 0.0f)
    }

    this
      .modify(_.state)
      .setTo(AbilityState.Active)
      .modify(_.bodyParameters.body)
      .setTo(body)
      .modify(_.dirVector)
      .setTo(dirVector)
  }

  def initBody(x: Float, y: Float): Option[Body] = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body = Some(mainAbility.creature.area.get.world.createBody(bodyDef))
    body.get.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(componentParameters.radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.get.createFixture(fixtureDef)

    body
  }

  override def render(batch: EsBatch): Self = {
    if (state == AbilityState.Active) {
      val spriteWidth = 64
      val scale = componentParameters.radius * 2 / spriteWidth
      val image = activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true)

      batch.spriteBatch.draw(
        image,
        bodyParameters.body.get.getPosition.x - componentParameters.radius,
        bodyParameters.body.get.getPosition.y - componentParameters.radius,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0.0f
      )
    }

    this
  }

  override def onCollideWithCreature(creature: Creature): Self = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(70f, immunityFrames = true)
    }

    this
  }

}
