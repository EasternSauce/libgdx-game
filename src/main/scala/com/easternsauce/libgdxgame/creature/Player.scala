package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.system.GameSystem.areaMap
import com.easternsauce.libgdxgame.system.{Assets, GameSystem}
import com.easternsauce.libgdxgame.util.{CreatureInfo, EsDirection, EsTimer}

class Player private (val id: String) extends Creature {

  override val creatureWidth = 1.85f
  override val creatureHeight = 1.85f

  override val maxLife = 200f

  override val isPlayer: Boolean = true

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.painSound))

  override val walkSound: Option[Sound] = Some(Assets.sound(Assets.runningSound))

  var onSpawnPointId: Option[String] = None

  var respawning: Boolean = false
  val respawnTimer: EsTimer = EsTimer()

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "male1",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.1f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  override def calculateFacingVector(): Unit = {
    val mouseX = Gdx.input.getX
    val mouseY = Gdx.input.getY

    val centerX = Gdx.graphics.getWidth / 2f
    val centerY = Gdx.graphics.getHeight / 2f

    // we need to reverse y due to mouse coordinates being in different system
    facingVector = new Vector2(mouseX - centerX, (Gdx.graphics.getHeight - mouseY) - centerY).nor()
  }

  override def onDeath(): Unit = {
    super.onDeath()

    respawnTimer.restart()
    respawning = true
    sprinting = false

  }

  def onRespawn(): Unit = {
    GameSystem.bossfightManager.stopBossfight()
  }

  def interact(): Unit = {
    if (onSpawnPointId.nonEmpty) {
      playerSpawnPoint = Some(areaMap(areaId.get).playerSpawns.filter(_.id == onSpawnPointId.get).head)
      playerSpawnPoint.get.onRespawnSet()
    }
  }

  override def calculateWalkingVector(): Unit = {
    val dirs: List[EsDirection.Value] = GameSystem.playerMovementDirections

    val vector = new Vector2(0f, 0f)

    dirs.foreach {
      case EsDirection.Up    => vector.y += 1f
      case EsDirection.Down  => vector.y -= 1f
      case EsDirection.Left  => vector.x -= 1f
      case EsDirection.Right => vector.x += 1f
    }

    vector.nor()

    walkingVector = vector

  }

  override val additionalAbilities: List[String] = Player.additionalAbilities

}

object Player extends CreatureInfo {
  def apply(id: String): Player = {
    val obj = new Player(id)
    obj.init()
    obj
  }

  override val additionalAbilities: List[String] = List("dash")
}
