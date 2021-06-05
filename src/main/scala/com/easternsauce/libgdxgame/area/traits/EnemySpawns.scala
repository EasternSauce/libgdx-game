package com.easternsauce.libgdxgame.area.traits

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

  def loadEnemySpawns(areaFilesLocation: String): Unit = {
    val source = scala.io.Source.fromFile(areaFilesLocation + "/spawns.json")
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[EnemySpawnFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode spawns file"))

    println(result)

  }

}

case class EnemySpawnFile(spawnpoints: List[EnemySpawnSavedata])
case class EnemySpawnSavedata(creatureClass: String, location: EnemySpawnLocation, weaponType: Option[String])
case class EnemySpawnLocation(x: Float, y: Float)
