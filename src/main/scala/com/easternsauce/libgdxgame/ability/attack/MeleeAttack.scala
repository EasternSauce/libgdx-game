package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Polygon, Rectangle, Vector2}
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc._
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}

trait MeleeAttack extends Ability with PhysicalHitbox with ActiveAnimation with WindupAnimation {

  val body: Option[Body]
  val hitbox: Option[AttackHitbox]
  val toRemoveBody: Boolean
  val bodyActive: Boolean

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
      setBody(body = Some(body))
    } else this

    ability
      .setHitbox(hitbox = hitbox)
      .setToRemoveBody(toRemoveBody = false)
      .setBodyActive(bodyActive = true)

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

    this

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

    ability.setHitbox(hitbox = hitbox)
  }

  override def update(): MeleeAttack = {

    val ability: MeleeAttack = super.update().asInstanceOf[MeleeAttack] // TODO: zzzz

    if (body.nonEmpty && toRemoveBody) {
      body.get.getWorld.destroyBody(body.get)

      ability.setToRemoveBody(toRemoveBody = false).setBodyActive(bodyActive = false)
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

      setHitbox(hitbox = newHitbox)
    } else
      this

  }

  override def onStop(): MeleeAttack = {
    creature.isAttacking = false

    // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash
    if (state == AbilityState.Active)
      setToRemoveBody(toRemoveBody = true)
    else
      this
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

    this
  }

  def setToRemoveBody(toRemoveBody: Boolean): MeleeAttack

  def setBody(body: Option[Body]): MeleeAttack

  def setHitbox(hitbox: Option[AttackHitbox]): MeleeAttack

  def setBodyActive(bodyActive: Boolean): MeleeAttack

}

case class AttackHitbox private (x: Float, y: Float, polygon: Polygon)
