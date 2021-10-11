package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.{Ability, _}
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}
import com.softwaremill.quicklens.ModifyPimp

abstract class MeleeAttack(
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
    )
    with PhysicalHitbox {
  type Self >: this.type <: MeleeAttack

  implicit def toMeleeAttack(ability: Ability): Self = ability.asInstanceOf[Self]

  val attackRange: Float

  // IMPORTANT: do NOT use body after already destroyed (otherwise weird behavior occurs, because, for some reason,
  // the reference can STILL be attached to some other random body after destruction, like arrow bodies)

  protected val aimed: Boolean
  protected val spriteWidth: Int
  protected val spriteHeight: Int

  protected def width: Float = spriteWidth.toFloat / Constants.PPM

  protected def height: Float = spriteHeight.toFloat / Constants.PPM

  protected val knockbackVelocity: Float
  override protected val isAttack = true

  protected val baseChannelTime: Float
  protected val baseActiveTime: Float

  // TODO: dependent on creature?
  val attackSpeed = 1.4f
  val attackScale = 1.4f

  override protected lazy val activeTime: Float = baseActiveTime
  override protected lazy val channelTime: Float = baseChannelTime / attackSpeed

  //TODO: move to creature
  def attackSpeed(creature: Creature): Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackSpeed.get
    else 1.4f

  //TODO: move to creature
  def attackScale(creature: Creature): Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackScale.get
    else 1.4f

  override def onActiveStart(creature: Creature): Self = {
    val ability = super.onActiveStart(creature)

    // TODO: clean up sideeffects

    timerParameters.abilityActiveAnimationTimer.restart()

    val creatureOps = (creature: Creature) => { creature.takeStaminaDamage(15f); creature }

    val attackVector = creature.attackVector

    val normalizedAttackVector =
      if (attackVector.len() > 0f) {
        new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      } else attackVector

    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    val attackShiftX = normalizedAttackVector.x * attackRange
    val attackShiftY = normalizedAttackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(attackScale, attackScale)

    val hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    val body = if (creature.area.nonEmpty) {
      initBody(creature.area.get.world, bodyParameters.hitbox.get)
    } else None

    ability
      .modify(_.bodyParameters.b2Body)
      .setTo(body)
      .modify(_.bodyParameters.toBeRemoved)
      .setTo(false)
      .modify(_.bodyParameters.hitbox)
      .setTo(hitbox)
      .modify(_.creatureOperations)
      .setTo(creatureOps :: creatureOperations)
  }

  override def render(creature: Creature, batch: EsBatch): Self = {
    // TODO: remove side effect
    def renderFrame(image: TextureRegion): Unit = {
      val attackVector = creature.attackVector
      val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

      if (bodyParameters.hitbox.nonEmpty) {
        batch.spriteBatch.draw(
          image,
          bodyParameters.hitbox.get.x,
          bodyParameters.hitbox.get.y - height / 2,
          0,
          height / 2,
          width,
          height,
          attackScale,
          attackScale,
          theta
        )
      }
    }

    if (state == AbilityState.Channeling && channelAnimation.nonEmpty)
      renderFrame(
        channelAnimation.get.currentFrame(time = timerParameters.abilityChannelAnimationTimer.time, loop = true)
      )
    if (state == AbilityState.Active && activeAnimation.nonEmpty)
      renderFrame(
        activeAnimation.get.currentFrame(time = timerParameters.abilityActiveAnimationTimer.time, loop = true)
      )

    this
  }

  override def onChannellingStart(creature: Creature): Self = {
    val ability: Self = super.onChannellingStart(creature)

    // TODO: sideeffects

    creature.attackVector = creature.facingVector.cpy()
    timerParameters.abilityChannelAnimationTimer.restart()
    creature.isAttacking = true

    val attackVector = creature.attackVector
    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    val normalizedAttackVector =
      if (attackVector.len() > 0f) {
        new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      } else attackVector

    val attackShiftX = normalizedAttackVector.x * attackRange
    val attackShiftY = normalizedAttackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(attackScale, attackScale)

    val hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    ability.modify(_.bodyParameters.hitbox).setTo(hitbox)
  }

  override def update(creature: Creature): Self = {

    val ability: Self = super.update(creature)

    if (bodyParameters.b2Body.nonEmpty && bodyParameters.toBeRemoved) {
      bodyParameters.b2Body.get.getWorld.destroyBody(bodyParameters.b2Body.get)

      ability
        .modify(_.bodyParameters.toBeRemoved)
        .setTo(false)
        .modify(_.bodyParameters.b2Body)
        .setTo(None)
    } else
      ability

  }

  override def updateHitbox(creature: Creature): Self = {
    if (bodyParameters.hitbox.nonEmpty) {

      val attackVector = creature.attackVector

      val normalizedAttackVector =
        if (attackVector.len() > 0f) {
          new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
        } else attackVector

      val attackShiftX = normalizedAttackVector.x * attackRange
      val attackShiftY = normalizedAttackVector.y * attackRange

      val newHitbox = Some(
        AttackHitbox(attackShiftX + creature.pos.x, attackShiftY + creature.pos.y, bodyParameters.hitbox.get.polygon)
      )

      // TODO: remove sideeffect
      if (bodyParameters.b2Body.nonEmpty) {
        bodyParameters.b2Body.get.setTransform(bodyParameters.hitbox.get.x, bodyParameters.hitbox.get.y, 0f)
      }

      this
        .modify(_.bodyParameters.hitbox)
        .setTo(newHitbox)

    } else
      this

  }

  override def onStop(creature: Creature): Self = {

    val creatureOps = (creature: Creature) => { creature.isAttacking = false; creature }

    // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash
    val ability = if (state == AbilityState.Active) {
      this
        .modify(_.bodyParameters.toBeRemoved)
        .setTo(true)
    } else
      this

    ability
      .modify(_.creatureOperations)
      .setTo(creatureOps :: creatureOperations)
  }


  // TODO: fix
//  override def onCollideWithCreature(creature: Creature, otherCreature: Creature): Self = {
//
//    // TODO: remove sideeffect
//    if (!(creature.isEnemy && otherCreature.isEnemy)) {
//      if (creature != otherCreature && !otherCreature.isImmune) {
//        otherCreature.takeLifeDamage(
//          damage = creature.weaponDamage,
//          immunityFrames = true,
//          dealtBy = Some(creature),
//          attackKnockbackVelocity = knockbackVelocity,
//          sourceX = creature.pos.x,
//          sourceY = creature.pos.y
//        )
//        val random = GameSystem.randomGenerator.nextFloat()
//
//        if (creature.isWeaponEquipped && random < creature.currentWeapon.template.poisonChance.get) {
//          otherCreature.activateEffect("poisoned", 10f)
//          otherCreature.poisonTickTimer.restart()
//        }
//      }
//    }
//
//    this
//  }

  override def copy(
    creatureId: String,
    state: AbilityState,
    creatureOperations: List[Creature => Creature],
    onCooldown: Boolean,
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self
}
