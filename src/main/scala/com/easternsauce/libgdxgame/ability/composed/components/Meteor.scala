package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState, Modification}
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, ComponentParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens.ModifyPimp

case class Meteor(
  mainAbility: Ability,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(
    textureWidth = 64,
    textureHeight = 64,
    activeRegionName = "explosion",
    activeFrameCount = 21,
    channelRegionName = "explosion_windup",
    channelFrameCount = 7
  ),
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val body: Option[Body] = None,
  override val destroyed: Boolean = false,
  override val dirVector: Vector2 = new Vector2(1, 0)
) extends AbilityComponent
    with Modification {
  type Self = Meteor

  override lazy val activeTime: Float = 1.8f / componentParameters.speed
  override lazy val channelTime: Float = 1.2f / componentParameters.speed

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = Some(
    Animation.channelAnimationFromParameters(animationParameters, channelTime)
  )

  def start(): Meteor = {

    channelTimer.restart()
    timerParameters.abilityChannelAnimationTimer.restart()

    this
      .modify(_.started)
      .setTo(true)
      .modify(_.state)
      .setTo(AbilityState.Channeling)
  }

  override def onUpdateActive(): Meteor = {
    modifyIf(started) {
      state match {
        case AbilityState.Channeling =>
          this
            .modifyIf(channelTimer.time > channelTime) {
              onActiveStart()
            }
        case AbilityState.Active =>
          this
            .modifyIf(!destroyed && activeTimer.time >= 0.2f) {
              body.get.getWorld.destroyBody(body.get)
              this
                .modify(_.destroyed)
                .setTo(true)
            }
            .modifyIf(activeTimer.time > activeTime) {
              // on active stop

              this
                .modify(_.state)
                .setTo(AbilityState.Inactive)
            }
        case _ => this
      }
    }

  }

  private def onActiveStart(): Meteor = {
    Assets.sound(Assets.explosionSound).play(0.01f)
    timerParameters.abilityActiveAnimationTimer.restart()
    activeTimer.restart()
    val body = initBody(componentParameters.startX, componentParameters.startY)

    this
      .modify(_.state)
      .setTo(AbilityState.Active)
      .modify(_.body)
      .setTo(body)
  }

  def initBody(x: Float, y: Float): Option[Body] = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    val body = mainAbility.creature.area.get.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(componentParameters.radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.createFixture(fixtureDef)

    Some(body)
  }

  override def render(batch: EsBatch): Meteor = {
    if (state == AbilityState.Channeling) {
      val spriteWidth = 64
      val scale = componentParameters.radius * 2 / spriteWidth
      val image = channelAnimation.get.currentFrame(time = timerParameters.channelTimer.time, loop = true)
      batch.spriteBatch.draw(
        image,
        componentParameters.startX - componentParameters.radius,
        componentParameters.startY - componentParameters.radius,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0.0f
      )
    }
    if (state == AbilityState.Active) {
      val spriteWidth = 64
      val scale = componentParameters.radius * 2 / spriteWidth
      val image = activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true)
      batch.spriteBatch.draw(
        image,
        componentParameters.startX - componentParameters.radius,
        componentParameters.startY - componentParameters.radius,
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

  override def onCollideWithCreature(creature: Creature): Meteor = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(70f, immunityFrames = true)
    }

    this
  }

}
