package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.traits.Attack
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.system.Assets

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ShootArrowAttack(val creature: Creature) extends Attack {

  override protected def channelTime: Float = 0.85f
  override protected def activeTime: Float = 0.1f
  override protected val cooldownTime = 0.8f

  override def onChannellingStart(): Unit = {
    super.onChannellingStart()

    creature.isAttacking = true

    Assets.sound(Assets.bowPullSound).play(0.1f)
  }

  override def onActiveStart(): Unit = {
    super.onActiveStart()

    Assets.sound(Assets.bowReleaseSound).play(0.1f)

    creature.attackVector = creature.facingVector.cpy()

    val arrowList: ListBuffer[Arrow] = creature.area.get.arrowList
    val tiles: TiledMap = creature.area.get.map
    val areaCreatures: mutable.Map[String, Creature] =
      creature.area.get.creaturesMap

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
