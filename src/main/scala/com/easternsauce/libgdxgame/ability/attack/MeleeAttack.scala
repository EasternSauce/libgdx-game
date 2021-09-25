package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Polygon, Rectangle, Vector2}
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc._
import com.easternsauce.libgdxgame.ability.parameters.TimerParameters
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}

class MeleeAttack protected (
  override val creature: Creature,
  override val state: AbilityState,
  override val onCooldown: Boolean,
  override val timerParameters: TimerParameters,
  val body: Option[Body],
  val hitbox: Option[AttackHitbox],
  val toRemoveBody: Boolean,
  val bodyActive: Boolean
) extends Ability(creature = creature, state = state, onCooldown = onCooldown, timerParameters = timerParameters)
    with PhysicalHitbox
    with ActiveAnimation
    with WindupAnimation {

  //implicit def toMeleeAttack(ability: Ability): MeleeAttack = ability.asInstanceOf[MeleeAttack]

  val attackRange: Float = -1f

  // IMPORTANT: do NOT use body after already destroyed (otherwise weird behavior occurs, because, for some reason,
  // the reference can STILL be attached to some other random body after destruction, like arrow bodies)

  protected val aimed: Boolean = false
  protected val spriteWidth: Int = -1
  protected val spriteHeight: Int = -1
  protected def width: Float = spriteWidth.toFloat / Constants.PPM
  protected def height: Float = spriteHeight.toFloat / Constants.PPM
  protected val knockbackVelocity: Float = -1f
  override protected val isAttack = true

  protected val baseChannelTime: Float = -1f
  protected val baseActiveTime: Float = -1f

  override protected lazy val activeTime: Float = baseActiveTime
  override protected lazy val channelTime: Float = baseChannelTime / attackSpeed

  def attackSpeed: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackSpeed.get
    else 1.4f

  def attackScale: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackScale.get
    else 1.4f

  override def onActiveStart(): MeleeAttack = {
    super.onActiveStart()

    // TODO: clean up sideeffects

    timerParameters.abilityActiveAnimationTimer.restart()

    creature.takeStaminaDamage(15f)

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

    val ability = if (creature.area.nonEmpty) {
      val body = initBody(creature.area.get.world, hitbox.get)
      makeMeleeAttackCopy(body = Some(body))
    } else makeMeleeAttackCopy()

    ability
      .makeMeleeAttackCopy(hitbox = hitbox, toRemoveBody = false, bodyActive = true)

  }

  override def render(batch: EsBatch): MeleeAttack = {
    // TODO: remove side effect
    def renderFrame(image: TextureRegion): Unit = {
      val attackVector = creature.attackVector
      val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

      if (hitbox.nonEmpty) {
        batch.spriteBatch.draw(
          image,
          hitbox.get.x,
          hitbox.get.y - height / 2,
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

    if (state == AbilityState.Channeling) renderFrame(currentWindupAnimationFrame)
    if (state == AbilityState.Active) renderFrame(currentActiveAnimationFrame)

    makeMeleeAttackCopy()

  }

  override def onChannellingStart(): MeleeAttack = {
    val ability: MeleeAttack = super.onChannellingStart().asInstanceOf[MeleeAttack] // TODO: zzzz

    // TODO: sideeffects

    creature.attackVector = creature.facingVector.cpy()
    timerParameters.abilityWindupAnimationTimer.restart()
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

    ability.makeMeleeAttackCopy(hitbox = hitbox)
  }

  override def update(): MeleeAttack = {
    println("before casting, ability is of class: " + super.update().getClass)

    val ability: MeleeAttack = super.update().asInstanceOf[MeleeAttack] // TODO: zzzz

    if (body.nonEmpty && toRemoveBody) {
      body.get.getWorld.destroyBody(body.get)

      ability.makeMeleeAttackCopy(toRemoveBody = false, bodyActive = false)
    } else
      ability

  }

  override def updateHitbox(): MeleeAttack = {
    if (hitbox.nonEmpty) {

      val attackVector = creature.attackVector

      val normalizedAttackVector =
        if (attackVector.len() > 0f) {
          new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
        } else attackVector

      val attackShiftX = normalizedAttackVector.x * attackRange
      val attackShiftY = normalizedAttackVector.y * attackRange

      val newHitbox = Some(
        AttackHitbox(attackShiftX + creature.pos.x, attackShiftY + creature.pos.y, hitbox.get.polygon)
      )

      // TODO: remove sideeffect
      if (bodyActive) {
        body.get.setTransform(hitbox.get.x, hitbox.get.y, 0f)
      }

      makeMeleeAttackCopy(hitbox = newHitbox)
    } else
      makeMeleeAttackCopy()

  }

  override def onStop(): MeleeAttack = {
    creature.isAttacking = false

    // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash
    if (state == AbilityState.Active)
      makeMeleeAttackCopy(toRemoveBody = true)
    else
      makeMeleeAttackCopy()
  }

  override def onCollideWithCreature(otherCreature: Creature): MeleeAttack = {

    // TODO: remove sideeffect
    if (!(creature.isEnemy && otherCreature.isEnemy)) {
      if (creature != otherCreature && !otherCreature.isImmune) {
        otherCreature.takeLifeDamage(
          damage = creature.weaponDamage,
          immunityFrames = true,
          dealtBy = Some(creature),
          attackKnockbackVelocity = knockbackVelocity,
          sourceX = creature.pos.x,
          sourceY = creature.pos.y
        )
        val random = GameSystem.randomGenerator.nextFloat()

        if (creature.isWeaponEquipped && random < creature.currentWeapon.template.poisonChance.get) {
          otherCreature.activateEffect("poisoned", 10f)
          otherCreature.poisonTickTimer.restart()
        }
      }
    }

    makeMeleeAttackCopy()
  }

  private def makeMeleeAttackCopy(
    creature: Creature = creature,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    timerParameters: TimerParameters = timerParameters,
    body: Option[Body] = body,
    hitbox: Option[AttackHitbox] = hitbox,
    toRemoveBody: Boolean = toRemoveBody,
    bodyActive: Boolean = bodyActive
  ): MeleeAttack = {
    MeleeAttack(creature, state, onCooldown, timerParameters, body, hitbox, toRemoveBody, bodyActive)
  }
}

object MeleeAttack {
  def apply(
    creature: Creature,
    state: AbilityState,
    onCooldown: Boolean,
    timerParameters: TimerParameters,
    body: Option[Body],
    hitbox: Option[AttackHitbox],
    toRemoveBody: Boolean,
    bodyActive: Boolean
  ): MeleeAttack =
    new MeleeAttack(creature, state, onCooldown, timerParameters, body, hitbox, toRemoveBody, bodyActive)
}

case class AttackHitbox private (x: Float, y: Float, polygon: Polygon)
