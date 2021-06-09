package com.easternsauce.libgdxgame.area.traits

import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.spawns.EnemySpawnPoint
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode

import scala.collection.mutable.ListBuffer

trait EnemySpawns {

  implicit val decodeEnemySpawnFile: Decoder[EnemySpawnFile] = deriveDecoder[EnemySpawnFile]
  implicit val decodeSpawnSavedata: Decoder[EnemySpawnSavedata] = deriveDecoder[EnemySpawnSavedata]
  implicit val decodeSpawnLocation: Decoder[EnemySpawnLocation] = deriveDecoder[EnemySpawnLocation]

  val enemySpawns: ListBuffer[EnemySpawnPoint] = ListBuffer()

  def loadEnemySpawns(area: Area, areaFilesLocation: String): Unit = {
    val source = scala.io.Source.fromFile(areaFilesLocation + "/enemy_spawns.json")
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[EnemySpawnFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode spawns file"))

    enemySpawns.addAll(result.spawnpoints.map(EnemySpawnPoint.loadFromSavedata(area, _)))

  }

}

case class EnemySpawnFile(spawnpoints: List[EnemySpawnSavedata])
case class EnemySpawnSavedata(
  id: String,
  creatureClass: String,
  location: EnemySpawnLocation,
  weaponType: Option[String]
)
case class EnemySpawnLocation(x: Float, y: Float)
