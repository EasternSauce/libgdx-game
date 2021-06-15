package com.easternsauce.libgdxgame.area.traits

import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.spawns.PlayerSpawnPoint
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.jawn.decode

import scala.collection.mutable.ListBuffer

trait PlayerSpawns {
  this: Area =>

  implicit val decodePlayerSpawnFile: Decoder[PlayerSpawnFile] = deriveDecoder[PlayerSpawnFile]
  implicit val decodePlayerSavedata: Decoder[PlayerSpawnSavedata] = deriveDecoder[PlayerSpawnSavedata]
  implicit val decodePlayerLocation: Decoder[PlayerSpawnLocation] = deriveDecoder[PlayerSpawnLocation]

  val playerSpawns: ListBuffer[PlayerSpawnPoint] = ListBuffer()

  def loadPLayerSpawns(): Unit = {
    val source = scala.io.Source.fromFile(areaFilesLocation + "/player_spawns.json")
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[PlayerSpawnFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode spawns file"))

    playerSpawns.addAll(result.spawnpoints.map(PlayerSpawnPoint.loadFromSavedata(this, _)))

  }
}

case class PlayerSpawnFile(spawnpoints: List[PlayerSpawnSavedata])
case class PlayerSpawnSavedata(id: String, location: PlayerSpawnLocation)
case class PlayerSpawnLocation(x: Float, y: Float)
