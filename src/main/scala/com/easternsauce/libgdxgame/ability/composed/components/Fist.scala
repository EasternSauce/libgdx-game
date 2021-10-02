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

case class Fist(
  mainAbility: Ability,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(
    textureWidth = 40,
    textureHeight = 80,
    activeRegionName = "fist_slam",
    activeFrameCount = 5,
    channelRegionName = "fist_slam_windup",
    channelFrameCount = 5
  ),
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val body: Option[Body] = None,
  override val destroyed: Boolean = false,
  override val dirVector: Vector2 = new Vector2(1, 0)
) extends AbilityComponent
    with Modification {
  type Self = Fist

  override lazy val activeTime: Float = 0.2f
  override lazy val channelTime: Float = 0.4f

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = Some(
    Animation.channelAnimationFromParameters(animationParameters, channelTime)
  )

  def start(): Fist = {

    channelTimer.restart()
    timerParameters.abilityChannelAnimationTimer.restart()

    copy(started = true, state = AbilityState.Channeling)
  }

  override def onUpdateActive(): Fist = {
    modifyIf(started) {
      state match {
        case AbilityState.Channeling =>
          modifyIf(channelTimer.time > channelTime) {

            Assets.sound(Assets.glassBreakSound).play(0.1f)
            timerParameters.abilityActiveAnimationTimer.restart()
            activeTimer.restart()
            val body = initBody(componentParameters.startX, componentParameters.startY)

            copy(state = AbilityState.Active, body = body)
          }
        case AbilityState.Active =>
          this
            .modifyIf(activeTimer.time > activeTime) {
              copy(state = AbilityState.Inactive)
            }
            .modifyIf(!destroyed && activeTimer.time >= 0.2f) {
              body.get.getWorld.destroyBody(body.get)
              copy(destroyed = true)
            }
            .modifyIf(activeTimer.time > activeTime) {
              // on active stop
              copy(state = AbilityState.Inactive)
            }
        case _ => this
      }
    }
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

  override def render(batch: EsBatch): Fist = {

    if (state == AbilityState.Channeling) {

      val image = channelAnimation.get.currentFrame(time = timerParameters.channelTimer.time, loop = true)

      val scale = componentParameters.radius * 2f / image.getRegionWidth

      val shift = componentParameters.radius
      batch.spriteBatch.draw(
        image,
        componentParameters.startX - shift,
        componentParameters.startY - shift,
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

      val image = activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true)

      val scale = componentParameters.radius * 2f / image.getRegionWidth

      val shift = componentParameters.radius

      batch.spriteBatch.draw(
        image,
        componentParameters.startX - shift,
        componentParameters.startY - shift,
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

  override def onCollideWithCreature(creature: Creature): Fist = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive && activeTimer.time < 0.15f) {
      if (!creature.isImmune) creature.takeLifeDamage(100f, immunityFrames = true)
    }

    this
  }

}
