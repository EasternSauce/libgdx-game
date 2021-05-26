package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.easternsauce.libgdxgame.area.traits.CollisionDetection
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable

class Area(val mapLoader: TmxMapLoader, val fileName: String, val id: String, val mapScale: Float)
    extends CollisionDetection {

  val creatureMap: mutable.Map[String, Creature] = mutable.Map()

  initPhysicalTerrain()

  createContactListener()

  def render(batch: EsBatch): Unit = {
    tiledMapRenderer.render()

    creatureMap.values.foreach(_.draw(batch.spriteBatch))

    for (creature <- creatureMap.values) {
      creature.renderAbilities(batch)
    }
  }

  def setView(camera: OrthographicCamera): Unit = {
    tiledMapRenderer.setView(camera)
  }

  def dispose(): Unit = {
    world.dispose()
    tiledMapRenderer.dispose()
    map.dispose()
  }

}
