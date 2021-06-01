package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.easternsauce.libgdxgame.ability.traits.{Ability, Attack}
import com.easternsauce.libgdxgame.ability.{BowAttack, SwordAttack, TridentAttack, UnarmedAttack}
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.{EsBatch, EsDirection, EsTimer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait Creature extends Sprite with PhysicalBody with AnimatedWalk with Inventory {

  val screen: PlayScreen

  val isEnemy = false
  val isPlayer = false
  val isNPC = false

  val id: String

  val creatureWidth: Float
  val creatureHeight: Float

  var currentDirection: EsDirection.Value = EsDirection.Down

  var isWalkAnimationActive = false
  val timeSinceMovedTimer: EsTimer = EsTimer()

  val directionalSpeed = 18f

  var area: Option[Area] = None

  var maxHealthPoints = 100f
  var healthPoints: Float = maxHealthPoints
  val maxStaminaPoints = 100f
  var staminaPoints: Float = maxStaminaPoints

  var isAttacking = false

  val totalArmor = 100f // TODO
  val knockbackable = false // TODO
  var knockbackVector = new Vector2() //TODO

  val onGettingHitSound: Option[Sound]

  val weaponDamage = 50f // TODO

  var attackVector: Vector2 = new Vector2(0f, 0f)

  var facingVector: Vector2 = new Vector2(0f, 0f)

  var staminaOveruse = false

  var passedGateRecently = false
  var toSetBodyNonInteractive = false

  var abilityList: mutable.ListBuffer[Ability] = ListBuffer()
  var attackList: mutable.ListBuffer[Attack] = ListBuffer()

  protected val effectMap: mutable.Map[String, Effect] = mutable.Map()

  var swordAttack: SwordAttack = _
  var unarmedAttack: UnarmedAttack = _
  var bowAttack: BowAttack = _
  var tridentAttack: TridentAttack = _

  protected val healthRegenTimer: EsTimer = EsTimer(true)
  protected val staminaRegenTimer: EsTimer = EsTimer(true)
  protected val poisonTickTimer: EsTimer = EsTimer()
  protected val staminaOveruseTimer: EsTimer = EsTimer()
  protected val healingTimer: EsTimer = EsTimer()
  protected val healingTickTimer: EsTimer = EsTimer()

  protected val healthRegen = 0.3f
  protected val staminaRegen = 3f
  protected val staminaOveruseTime = 1.3f
  protected val poisonTickTime = 1.5f
  protected val poisonTime = 20f
  protected val knockbackPower = 0f
  protected var healing = false
  protected val healingTickTime = 0.3f
  protected val healingTime = 8f
  protected var healingPower = 0f

  protected var knocbackable = true
  protected var knockbackSpeed: Float = 0f

  protected var staminaDrain = 0.0f
  var sprinting = false

  def isImmune: Boolean = effect("immune").isActive

  def atFullLife: Boolean = healthPoints >= maxHealthPoints

  def alive: Boolean = {
    true // TODO
  }

  def initParams(mass: Float): Unit = {
    this.mass = mass

  }

  def onUpdateStart(): Unit = {
    knockbackSpeed = knockbackPower * Gdx.graphics.getDeltaTime

    if (sprinting && staminaPoints > 0) {
      staminaDrain += Gdx.graphics.getDeltaTime
    }

  }

  def isAlive: Boolean = healthPoints > 0f

  def update(): Unit = {
    if (isAlive) {
      onUpdateStart()

      calculateFacingVector()

      regenerate()
    }

    for (effect <- effectMap.values) {
      effect.update()
    }

    for (ability <- abilityList) {
      ability.update()
    }

    currentAttack.update()

    if (staminaDrain >= 0.3f) {
      takeStaminaDamage(11f)

      staminaDrain = 0.0f
    }

    if (toSetBodyNonInteractive) {
      setNonInteractive()
      toSetBodyNonInteractive = false
    }

    if (isWalkAnimationActive) setRegion(walkAnimationFrame(currentDirection))
    else setRegion(standStillImage(currentDirection))

    if (bodyExists) {
      setPosition(pos.x - getWidth / 2f, pos.y - getHeight / 2f)
    }

    if (isWalkAnimationActive && timeSinceMovedTimer.time > 0.25f) isWalkAnimationActive = false
  }

  def abilityActive: Boolean = {
    var abilityActive = false

    for (ability <- abilityList) {
      if (!abilityActive && ability.active) {
        abilityActive = true

      }
    }

    if (currentAttack.active) return true

    abilityActive
  }

  def regenerate(): Unit = {
    if (healthRegenTimer.time > 0.5f) {
      heal(healthRegen)
      healthRegenTimer.restart()
    }

    if (!effect("staminaRegenStopped").isActive && !sprinting)
      if (staminaRegenTimer.time > 0.05f && !abilityActive && !staminaOveruse) {
        if (staminaPoints < maxStaminaPoints) {
          val afterRegen = staminaPoints + staminaRegen
          staminaPoints = Math.min(afterRegen, maxStaminaPoints)
        }
        staminaRegenTimer.restart()
      }

    if (staminaOveruse)
      if (staminaOveruseTimer.time > staminaOveruseTime) staminaOveruse = false

    if (effect("poisoned").isActive)
      if (poisonTickTimer.time > poisonTickTime) {
        takeHealthDamage(15f, immunityFrames = false)
        poisonTickTimer.restart()
      }

    if (healing) {
      if (healingTickTimer.time > healingTickTime) {
        heal(healingPower)
        healingTickTimer.restart()
      }
      if (healingTimer.time > healingTime || healthPoints >= maxHealthPoints)
        healing = false
    }
  }

  def heal(healValue: Float): Unit = {
    if (healthPoints < maxHealthPoints) {
      val afterHeal = healthPoints + healValue
      healthPoints = Math.min(afterHeal, maxHealthPoints)

    }
  }

  def calculateFacingVector(): Unit

  def defineStandardAbilities(): Unit = {
    swordAttack = new SwordAttack(this)
    unarmedAttack = new UnarmedAttack(this)
    bowAttack = new BowAttack(this)
    tridentAttack = new TridentAttack(this)

    attackList += swordAttack
  }

  def currentAttack: Ability = {
    if (isWeaponEquipped) {
      currentWeapon.template.attackType match {
        case Some("sword")   => swordAttack
        case Some("bow")     => bowAttack
        case Some("trident") => tridentAttack
        case _               => throw new RuntimeException("Unrecognized attack type")
      }
    } else {
      unarmedAttack
    }
  }

  protected def defineEffects(): Unit = {
    initEffect("immune")
    initEffect("immobilized")
    initEffect("staminaRegenStopped")
    initEffect("poisoned")

  }

  protected def initEffect(effectName: String): Unit = {
    effectMap.put(effectName, Effect())
  }

  def onDeath(): Unit = {
    isWalkAnimationActive = false

    for (ability <- abilityList) {
      ability.forceStop()
    }
    currentAttack.forceStop()

    setRotation(90f)

    toSetBodyNonInteractive = true
  }

  def takeHealthDamage(
    damage: Float,
    immunityFrames: Boolean,
    knockbackPower: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ): Unit = {
    if (alive) {
      val beforeHP = healthPoints

      val actualDamage = damage * 100f / (100f + totalArmor)

      if (healthPoints - actualDamage > 0) healthPoints -= actualDamage
      else healthPoints = 0f

      if (beforeHP != healthPoints && healthPoints == 0f) onDeath()

      if (immunityFrames) { // immunity frames on hit
        effect("immune").applyEffect(0.75f)
        // stagger on hit
        effect("immobilized").applyEffect(0.35f)
      }

      if (knockbackable) {
        knockbackVector = new Vector2(pos.x - sourceX, pos.y - sourceY).nor()

        b2Body.applyLinearImpulse(
          new Vector2(knockbackVector.x * knockbackPower, knockbackVector.y * knockbackPower),
          b2Body.getWorldCenter,
          true
        )
      }

      if (onGettingHitSound.nonEmpty) onGettingHitSound.get.play(0.1f)
    }
  }

  def takeStaminaDamage(staminaDamage: Float): Unit = {
    if (staminaPoints - staminaDamage > 0) staminaPoints -= staminaDamage
    else {
      staminaPoints = 0f
      staminaOveruse = true
      staminaOveruseTimer.restart()
    }
  }

  def moveInDirection(dirs: List[EsDirection.Value]): Unit = {

    if (isAlive) {
      timeSinceMovedTimer.restart()

      if (!isWalkAnimationActive) {
        animationTimer.restart()
        isWalkAnimationActive = true
      }

      val vector = new Vector2()

      currentDirection = dirs.last

      val modifiedSpeed =
        if (isAttacking) directionalSpeed / 2f
        else if (sprinting && staminaPoints > 0) directionalSpeed * 1.75f
        else directionalSpeed

      dirs.foreach {
        case EsDirection.Up    => vector.y += modifiedSpeed
        case EsDirection.Down  => vector.y -= modifiedSpeed
        case EsDirection.Left  => vector.x -= modifiedSpeed
        case EsDirection.Right => vector.x += modifiedSpeed
      }

      val horizontalCount = dirs.count(EsDirection.isHorizontal)
      val verticalCount = dirs.count(EsDirection.isVertical)

      if (horizontalCount < 2 && verticalCount < 2) {
        if (horizontalCount > 0 && verticalCount > 0) {
          sustainVelocity(new Vector2(vector.x / Math.sqrt(2).toFloat, vector.y / Math.sqrt(2).toFloat))
        } else {
          sustainVelocity(vector)
        }
      }
    }

  }

  def assignToArea(area: Area, x: Float, y: Float): Unit = {
    if (this.area.isEmpty) {
      this.area = Some(area)
      initCircularBody(area.world, x, y, creatureWidth / 2f)

      area.creatureMap += (id -> this)

    } else {
      val oldArea = this.area.get

      destroyBody(oldArea.world)
      oldArea.creatureMap -= id

      this.area = Some(area)
      initCircularBody(area.world, x, y, creatureWidth / 2f)

      area.creatureMap += (id -> this)

    }

  }

  def effect(effectName: String): Effect = {
    effectMap.get(effectName) match {
      case Some(effect) => effect
      case _ =>
        throw new RuntimeException("tried to access non-existing effect: " + effectName)
    }
  }

  def renderAbilities(batch: EsBatch): Unit = {
    for (ability <- abilityList) {
      ability.render(batch)
    }
    currentAttack.render(batch)
  }

  def renderHealthBar(batch: EsBatch): Unit = {
    val healthBarHeight = 0.16f
    val healthBarWidth = 2.0f
    val currentHealthBarWidth = healthBarWidth * healthPoints / maxHealthPoints
    val barPosX = pos.x - healthBarWidth / 2
    val barPosY = pos.y + getWidth / 2 + 0.3125f
    batch.shapeDrawer.filledRectangle(new Rectangle(barPosX, barPosY, healthBarWidth, healthBarHeight), Color.ORANGE)
    batch.shapeDrawer
      .filledRectangle(new Rectangle(barPosX, barPosY, currentHealthBarWidth, healthBarHeight), Color.RED)

  }

}
