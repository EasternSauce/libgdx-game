package com.easternsauce.libgdxgame.area.traits

import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.bossfight.BossArenaBlockade
import com.easternsauce.libgdxgame.spawns.EnemySpawnPoint
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode

import scala.collection.mutable.ListBuffer

trait EnemySpawns {
  this: Area =>

  implicit val decodeEnemySpawnFile: Decoder[EnemySpawnFile] = deriveDecoder[EnemySpawnFile]
  implicit val decodeSpawnSavedata: Decoder[EnemySpawnSavedata] = deriveDecoder[EnemySpawnSavedata]
  implicit val decodeSpawnLocation: Decoder[EnemySpawnLocation] = deriveDecoder[EnemySpawnLocation]
  implicit val blockadeLocation: Decoder[BlockadeLocation] = deriveDecoder[BlockadeLocation]

  val enemySpawns: ListBuffer[EnemySpawnPoint] = ListBuffer()
  val bossArenaBlockades: ListBuffer[BossArenaBlockade] = ListBuffer()

  def loadEnemySpawns(): Unit = {
    val source = scala.io.Source.fromFile(areaFilesLocation + "/enemy_spawns.json")
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[EnemySpawnFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode spawns file"))

    enemySpawns.addAll(result.spawnpoints.map(EnemySpawnPoint.loadFromSavedata(this, _)))
    bossArenaBlockades.addAll(
      enemySpawns.flatMap(_.blockades).map(blockade => new BossArenaBlockade(this, blockade.x, blockade.y))
    )
  }

}

case class EnemySpawnFile(spawnpoints: List[EnemySpawnSavedata])
case class EnemySpawnSavedata(
  id: String,
  creatureClass: String,
  location: EnemySpawnLocation,
  weaponType: Option[String],
  blockades: Option[List[BlockadeLocation]]
)
case class EnemySpawnLocation(x: Float, y: Float)
case class BlockadeLocation(x: Float, y: Float)
