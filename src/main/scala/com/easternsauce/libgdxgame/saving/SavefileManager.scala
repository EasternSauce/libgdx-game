package com.easternsauce.libgdxgame.saving

import java.io.{File, PrintWriter}

import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.screens.PlayScreen
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

import scala.collection.mutable

class SavefileManager(val game: RpgGame) {

  implicit val decodeItemSave: Decoder[ItemSavedata] = deriveDecoder[ItemSavedata]
  implicit val decodeCreatureSave: Decoder[CreatureSavedata] = deriveDecoder[CreatureSavedata]
  implicit val decodeSaveFile: Decoder[SaveFile] = deriveDecoder[SaveFile]
  implicit val decodePositionSave: Decoder[PositionSavedata] = deriveDecoder[PositionSavedata]

  implicit val encodeItemSave: Encoder[ItemSavedata] = deriveEncoder[ItemSavedata]
  implicit val encodeCreatureSave: Encoder[CreatureSavedata] = deriveEncoder[CreatureSavedata]
  implicit val encodeSaveFile: Encoder[SaveFile] = deriveEncoder[SaveFile]
  implicit val encodePositionSave: Encoder[PositionSavedata] = deriveEncoder[PositionSavedata]

  val saveFileLocation = "save/savefile.json"

  def savefileFound: Boolean = new File(saveFileLocation).exists

  def saveGame(): Unit = {
    val saveFile = SaveFile(
      game.allAreaCreaturesMap.values.filter(c => c.isPlayer || c.isAlive).map(_.saveToData()).toList
    )

    val writer = new PrintWriter(new File(saveFileLocation))

    writer.write(saveFile.asJson.toString())

    writer.close()
  }

  def loadGame(): Unit = {
    val source = scala.io.Source.fromFile(saveFileLocation)
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[SaveFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode save file"))

    game.allAreaCreaturesMap = mutable.Map()
    result.creatures.foreach(creatureData => recreateCreatureFromSavedata(game, creatureData))

  }

  private def recreateCreatureFromSavedata(game: RpgGame, creatureData: CreatureSavedata): Unit = {
    val action = Class
      .forName(creatureData.creatureClass)
      .getDeclaredConstructor(classOf[RpgGame], classOf[String])
      .newInstance(game, creatureData.id)
    val creature = action.asInstanceOf[Creature]

    creature.loadFromSavedata(creatureData, game)

  }

}

case class ItemSavedata(index: Int, template: String, damage: Option[Int], armor: Option[Int])
case class CreatureSavedata(
  creatureClass: String,
  id: String,
  spawnPointId: Option[String],
  healthPoints: Float,
  area: String,
  isPlayer: Boolean,
  position: PositionSavedata,
  equipmentItems: List[ItemSavedata],
  inventoryItems: List[ItemSavedata]
)
case class PositionSavedata(x: Float, y: Float)
case class SaveFile(creatures: List[CreatureSavedata])
