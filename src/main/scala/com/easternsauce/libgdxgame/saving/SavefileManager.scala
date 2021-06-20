package com.easternsauce.libgdxgame.saving

import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.GameSystem._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

import java.io.{File, PrintWriter}
import scala.collection.mutable

class SavefileManager {

  implicit val decodePlayerSpawnPointSave: Decoder[PlayerSpawnPointSavedata] = deriveDecoder[PlayerSpawnPointSavedata]
  implicit val decodeItemSave: Decoder[ItemSavedata] = deriveDecoder[ItemSavedata]
  implicit val decodeCreatureSave: Decoder[CreatureSavedata] = deriveDecoder[CreatureSavedata]
  implicit val decodeSaveFile: Decoder[SaveFile] = deriveDecoder[SaveFile]
  implicit val decodePositionSave: Decoder[PositionSavedata] = deriveDecoder[PositionSavedata]

  implicit val encodePlayerSpawnPointSave: Encoder[PlayerSpawnPointSavedata] = deriveEncoder[PlayerSpawnPointSavedata]
  implicit val encodeItemSave: Encoder[ItemSavedata] = deriveEncoder[ItemSavedata]
  implicit val encodeCreatureSave: Encoder[CreatureSavedata] = deriveEncoder[CreatureSavedata]
  implicit val encodeSaveFile: Encoder[SaveFile] = deriveEncoder[SaveFile]
  implicit val encodePositionSave: Encoder[PositionSavedata] = deriveEncoder[PositionSavedata]

  val saveFileLocation = "save/savefile.json"

  def savefileFound: Boolean = new File(saveFileLocation).exists

  def saveGame(): Unit = {
    val saveFile = SaveFile(allAreaCreaturesMap.values.filter(c => c.isPlayer || c.alive).map(_.saveToData()).toList)

    val writer = new PrintWriter(new File(saveFileLocation))

    writer.write(saveFile.asJson.toString())

    writer.close()

    notificationText.showNotification("Saving game...")
  }

  def loadGame(): Unit = {
    val source = scala.io.Source.fromFile(saveFileLocation)
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[SaveFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode save file"))

    allAreaCreaturesMap = mutable.Map()
    result.creatures.foreach(creatureData => recreateCreatureFromSavedata(creatureData))

  }

  private def recreateCreatureFromSavedata(creatureData: CreatureSavedata): Unit = {
    val action = Class
      .forName(creatureData.creatureClass)
      .getDeclaredConstructor(classOf[String])
      .newInstance(creatureData.id)
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
case class SaveFile(creatures: List[CreatureSavedata])
