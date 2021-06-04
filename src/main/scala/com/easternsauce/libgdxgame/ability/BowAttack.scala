package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.ability.traits.Attack
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.projectile.Arrow

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class BowAttack(val creature: Creature) extends Attack {

  override protected val channelTime: Float = 0.85f
  override protected val activeTime: Float = 0.1f
  override protected val cooldownTime = 0.8f

  override def onChannellingStart(): Unit = {
    super.onChannellingStart()

    creature.isAttacking = true

    RpgGame.manager.get(AssetPaths.bowPullSound, classOf[Sound]).play(0.1f)
  }

  override def onActiveStart(): Unit = {
    super.onActiveStart()

    RpgGame.manager.get(AssetPaths.bowReleaseSound, classOf[Sound]).play(0.1f)

    creature.attackVector = creature.facingVector.cpy()

    val arrowList: ListBuffer[Arrow] = creature.area.get.arrowList
    val tiles: TiledMap = creature.area.get.map
    val areaCreatures: mutable.Map[String, Creature] =
      creature.area.get.creatureMap

    if (!creature.facingVector.equals(new Vector2(0.0f, 0.0f))) {
      val arrowStartX = creature.pos.x
      val arrowStartY = creature.pos.y

      val arrow: Arrow = Arrow(
        arrowStartX,
        arrowStartY,
        creature.area.get,
        creature.facingVector,
        arrowList,
        tiles,
        areaCreatures,
        this.creature
      )
      arrowList += arrow
    }

    creature.takeStaminaDamage(20f)
  }

}
