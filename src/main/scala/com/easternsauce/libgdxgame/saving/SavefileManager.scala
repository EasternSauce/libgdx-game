package com.easternsauce.libgdxgame.saving

import java.io.{File, PrintWriter}

import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SavefileManager {

  implicit val decodePlayerSpawnPointSave: Decoder[PlayerSpawnPointSavedata] = deriveDecoder[PlayerSpawnPointSavedata]
  implicit val decodeItemSave: Decoder[ItemSavedata] = deriveDecoder[ItemSavedata]
  implicit val decodeCreatureSave: Decoder[CreatureSavedata] = deriveDecoder[CreatureSavedata]
  implicit val decodeSaveFile: Decoder[SaveFile] = deriveDecoder[SaveFile]
  implicit val decodePositionSave: Decoder[PositionSavedata] = deriveDecoder[PositionSavedata]
  implicit val decodeTreasureLootedSave: Decoder[TreasureLootedSavedata] = deriveDecoder[TreasureLootedSavedata]

  implicit val encodePlayerSpawnPointSave: Encoder[PlayerSpawnPointSavedata] = deriveEncoder[PlayerSpawnPointSavedata]
  implicit val encodeItemSave: Encoder[ItemSavedata] = deriveEncoder[ItemSavedata]
  implicit val encodeCreatureSave: Encoder[CreatureSavedata] = deriveEncoder[CreatureSavedata]
  implicit val encodeSaveFile: Encoder[SaveFile] = deriveEncoder[SaveFile]
  implicit val encodePositionSave: Encoder[PositionSavedata] = deriveEncoder[PositionSavedata]
  implicit val encodeTreasureLootedSave: Encoder[TreasureLootedSavedata] = deriveEncoder[TreasureLootedSavedata]

  val saveFileLocation = "save"
  val saveFileName = "savefile.json"

  val saveFilePath = saveFileLocation + "/" + saveFileName

  def savefileFound: Boolean = new File(saveFilePath).exists

  def saveGame(): Unit = {
    val treasureLootedData: ListBuffer[TreasureLootedSavedata] = ListBuffer()
    treasureLootedList.foreach(treasureLooted => {
      val (areaId, treasureId) = treasureLooted
      treasureLootedData += TreasureLootedSavedata(areaId, treasureId)
    })

    val saveFile = SaveFile(
      globalCreaturesMap.values.filter(c => c.isPlayer || c.isAlive).map(_.saveToData()).toList,
      treasureLootedData.toList
    )

    new File(saveFileLocation).mkdir()

    val writer = new PrintWriter(new File(saveFilePath))

    writer.write(saveFile.asJson.toString())

    writer.close()

    notificationText.showNotification("Saving game...")
  }

  def loadGame(): Unit = {
    val source = scala.io.Source.fromFile(saveFilePath)
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[SaveFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode save file"))

    globalCreaturesMap = mutable.Map()
    result.creatures.foreach(creatureData => recreateCreatureFromSavedata(creatureData))
    result.treasuresLooted.foreach(
      treasureData => treasureLootedList += (treasureData.areaId -> treasureData.treasureId)
    )

    areaMap(currentAreaId.get).lootPileList.addAll(
      areaMap(currentAreaId.get).treasuresList
        .filterNot(treasure => treasureLootedList.contains((areaMap(currentAreaId.get).id, treasure.treasureId.get)))
    )

  }

  private def recreateCreatureFromSavedata(creatureData: CreatureSavedata): Unit = {
    val action = Class
      .forName(creatureData.creatureClass)
      .getMethod("apply", classOf[String])
      .invoke(null, creatureData.id)

    val creature = action.asInstanceOf[Creature]

    creature.loadFromSavedata(creatureData)

  }

}

case class PlayerSpawnPointSavedata(area: String, id: String)
case class ItemSavedata(index: Int, template: String, quantity: Int, damage: Option[Int], armor: Option[Int])
case class CreatureSavedata(
  creatureClass: String,
  id: String,
  spawnPointId: Option[String],
  playerSpawnPoint: Option[PlayerSpawnPointSavedata],
  life: Float,
  area: String,
  isPlayer: Boolean,
  position: PositionSavedata,
  equipmentItems: List[ItemSavedata],
  inventoryItems: List[ItemSavedata]
)
case class PositionSavedata(x: Float, y: Float)
case class TreasureLootedSavedata(areaId: String, treasureId: String)
case class SaveFile(creatures: List[CreatureSavedata], treasuresLooted: List[TreasureLootedSavedata])
