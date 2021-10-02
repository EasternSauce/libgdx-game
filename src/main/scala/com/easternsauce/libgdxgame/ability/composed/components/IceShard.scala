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

case class IceShard(
  mainAbility: Ability,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(),
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val body: Option[Body] = None,
  override val destroyed: Boolean = false,
  override val dirVector: Vector2 = new Vector2(1, 0)
) extends AbilityComponent
    with Modification {
  type Self = IceShard

  override lazy val activeTime: Float = 1.5f
  override lazy val channelTime: Float = 0.6f

  val radius = 1f

  val spriteWidth = 152
  val spriteHeight = 72

  override val activeAnimation: Option[Animation] = None
  override val channelAnimation: Option[Animation] = None

  def start(): IceShard = {

    channelTimer.restart()

    copy(started = true, state = AbilityState.Channeling)
  }

  override def onUpdateActive(): IceShard = {
    modifyIf(started) {
      state match {
        case AbilityState.Channeling =>
          modifyIf(channelTimer.time > channelTime) {
            onActiveStart()
          }
        case AbilityState.Active =>
          val component = this
            .modifyIf(!destroyed && activeTimer.time >= activeTime) {
              body.get.getWorld.destroyBody(body.get)
              copy(destroyed = true)
            }
            .modifyIf(activeTimer.time > activeTime) {
              // on active stop
              copy(state = AbilityState.Inactive)
            }
          if (!destroyed) {
            body.get.setLinearVelocity(dirVector.x * componentParameters.speed, dirVector.y * componentParameters.speed)
          }
          component
        case _ => this
      }
    }

  }

  private def onActiveStart(): IceShard = {
    //Assets.sound(Assets.explosionSound).play(0.01f)
    activeTimer.restart()

    val body = initBody(componentParameters.startX, componentParameters.startY)

    copy(state = AbilityState.Active, body = body)
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

  override def render(batch: EsBatch): IceShard = {
    if (state == AbilityState.Active) {
      val scale = radius * 2 / spriteWidth
      val image = Assets.atlas.findRegion("ice_shard")
      batch.spriteBatch.draw(
        image,
        body.get.getPosition.x - radius,
        body.get.getPosition.y - radius,
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

  override def onCollideWithCreature(creature: Creature): IceShard = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(70f, immunityFrames = true)
    }

    this
  }

}
