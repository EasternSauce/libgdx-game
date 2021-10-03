package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{
  AnimationParameters,
  BodyParameters,
  ComponentParameters,
  TimerParameters
}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens.ModifyPimp

case class IceShard(
  override val mainAbility: Ability,
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(),
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
  type Self = IceShard

  override lazy val activeTime: Float = 1.5f
  override lazy val channelTime: Float = 0.6f

  val radius = 1f

  val spriteWidth = 152
  val spriteHeight = 72

  // TODO: workaround: change user data when ability is modified
  if (bodyParameters.body.nonEmpty && bodyParameters.body.get != null) {
    bodyParameters.body.get.setUserData(this)
  }

  override val activeAnimation: Option[Animation] = None
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
    val component0: IceShard   = this

    val component1: IceShard = if (component0.started) {
      component0.state match {
        case AbilityState.Channeling =>
          val component1_1 = if (component0.timerParameters.channelTimer.time > component0.channelTime) {
            component0.onActiveStart()
          } else component0
          component1_1
        case AbilityState.Active =>
          val component1_2: IceShard =
            if (!component0.bodyParameters.destroyed && component0.timerParameters.activeTimer.time >= activeTime) {
              component0.bodyParameters.body.get.getWorld.destroyBody(component0.bodyParameters.body.get)
              component0
                .modify(_.bodyParameters.destroyed)
                .setTo(true)
                .modify(_.bodyParameters.body)
                .setTo(Some(null))
            } else component0

          val component1_3: IceShard = if (component1_2.timerParameters.activeTimer.time > component1_2.activeTime) {
              // on active stop
            component1_2
                .modify(_.state)
                .setTo(AbilityState.Inactive)
            } else component1_2

          val component1_4: IceShard = if (!component1_3.bodyParameters.destroyed) {
            component1_3.bodyParameters.body.get
              .setLinearVelocity(
                component1_3.dirVector.x * component1_3.componentParameters.speed,
                component1_3.dirVector.y * component1_3.componentParameters.speed
              )
            component1_3
          } else component1_3
          component1_4
        case _ => component0
      }
    } else component0

    component1

  }

  private def onActiveStart(): Self = {

    //Assets.sound(Assets.explosionSound).play(0.01f)
    timerParameters.activeTimer.restart()

    val body = initBody(
      mainAbility.creature.pos.x + mainAbility.creature.creatureWidth / 2f,
      mainAbility.creature.pos.y + mainAbility.creature.creatureHeight / 2f
    )

    this
      .modify(_.state)
      .setTo(AbilityState.Active)
      .modify(_.bodyParameters.body)
      .setTo(body)
  }

  def initBody(x: Float, y: Float): Option[Body] = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body = mainAbility.creature.area.get.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.createFixture(fixtureDef)

    Some(body)
  }

  override def render(batch: EsBatch): Self = {

    if (state == AbilityState.Active) {
      val scale = radius * 2 / spriteWidth
      val image = Assets.atlas.findRegion("ice_shard")
      batch.spriteBatch.draw(
        image,
        bodyParameters.body.get.getPosition.x - radius,
        bodyParameters.body.get.getPosition.y - radius,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        dirVector.angleDeg()
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
