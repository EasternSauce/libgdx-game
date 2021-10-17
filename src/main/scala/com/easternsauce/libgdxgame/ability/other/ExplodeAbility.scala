package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.ability.misc.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.templates.{Ability, AbilityState}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.system.GameSystem.areaMap
import com.easternsauce.libgdxgame.util.{AbilityInfo, EsBatch}
import com.softwaremill.quicklens._

case class ExplodeAbility private (
  override val creatureId: String,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0f,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters =
    AnimationParameters(textureWidth = 64, textureHeight = 64, activeRegionName = "explosion", activeFrameCount = 21),
  override val soundParameters: SoundParameters = SoundParameters(
    channelSound = Some(Assets.sound(Assets.darkLaughSound)),
    channelSoundVolume = Some(0.2f),
    activeSound = Some(Assets.sound(Assets.explosionSound)),
    activeSoundVolume = Some(0.5f)
  ),
  override val bodyParameters: BodyParameters = BodyParameters(),
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
  type Self = ExplodeAbility

  override val id: String = ExplodeAbility.id
  override protected val cooldownTime: Float = 0.8f

  protected val explosionRange: Float = 10f

  override protected val isStoppable: Boolean = false

  override protected lazy val activeTime: Float = 0.9f

  override protected lazy val channelTime: Float = 1.3f

  override def onActiveStart(): Self = {
    super.onActiveStart()

    timerParameters.abilityActiveAnimationTimer.restart()

    // TODO: side effects
    modifyCreature(creature => { creature.takeStaminaDamage(25f); creature })
    modifyCreature(creature => {
      creature.takeLifeDamage(700f, immunityFrames = false, Some(creature), 0, 0, 0); creature
    })
    Assets.sound(Assets.explosionSound).play(0.07f)

    initBody(creature.pos.x, creature.pos.y)
  }

  override def onUpdateActive(): Self = {
    val activeTimer = timerParameters.activeTimer

    if (bodyParameters.bodyActive && activeTimer.time > 0.1f) {
      val area = areaMap(creature.areaId.get)

      destroyBody(area.world)
    } else
      this
  }

  override def render(batch: EsBatch): Self = {
    def renderFrame(image: TextureRegion): Unit = {

      val scale = explosionRange * 2 / image.getRegionWidth

      val scaledWidth = explosionRange * 2
      val scaledHeight = explosionRange * 2

      batch.spriteBatch.draw(
        image,
        creature.pos.x - scaledWidth / 2f,
        creature.pos.y - scaledHeight / 2f,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0f
      )

    }

    // TODO: remove side effect
    if (state == AbilityState.Active) {
      renderFrame(activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true))
    }

    this
  }

  def initBody(x: Float, y: Float): Self = {
    // TODO: side effects

    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    val area = areaMap(creature.areaId.get)

    val b2Body = area.world.createBody(bodyDef)
    b2Body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(explosionRange)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    b2Body.createFixture(fixtureDef)

    this
      .modify(_.bodyParameters.body)
      .setTo(Some(b2Body))
      .modify(_.bodyParameters.bodyActive)
      .setTo(true)
  }

  def destroyBody(world: World): Self = {
    if (bodyParameters.bodyActive) {
      // TODO: sideeffect?
      if (bodyParameters.body.nonEmpty) world.destroyBody(bodyParameters.body.get)

      this
        .modify(_.bodyParameters.bodyActive)
        .setTo(false)
    } else
      this
  }

  override def onCollideWithCreature(otherCreature: Creature): Self = {
    // TODO: side effect
    if (!(creature.isEnemy && otherCreature.isEnemy) && otherCreature.isAlive) { // mob can't hurt a mob?
      if (!otherCreature.isImmune) otherCreature.takeLifeDamage(700f, immunityFrames = true, Some(creature), 0, 0, 0)
    }

    this
  }

  override def copy(
    creatureId: String,
    state: AbilityState,
    onCooldown: Boolean,
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self =
    ExplodeAbility(
      creatureId = creatureId,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      soundParameters = soundParameters,
      timerParameters = timerParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    )
}

object ExplodeAbility extends AbilityInfo {
  override val id = "explode"
}
