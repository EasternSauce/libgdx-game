package com.easternsauce.libgdxgame.area.traits

import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.items.{Item, LootPile}
import com.easternsauce.libgdxgame.system.GameSystem._
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.jawn.decode

import scala.collection.mutable.ListBuffer

trait LootManagement {
  this: Area =>

  implicit val decodeTreasuresFile: Decoder[TreasuresFile] = deriveDecoder[TreasuresFile]
  implicit val decodeTreasuresSavedata: Decoder[TreasureSavedata] = deriveDecoder[TreasureSavedata]
  implicit val decodeTreasureLocation: Decoder[TreasureLocation] = deriveDecoder[TreasureLocation]

  val treasuresList: ListBuffer[LootPile] = ListBuffer()

  var lootPileList: ListBuffer[LootPile] = ListBuffer()

  def spawnLootPile(x: Float, y: Float, dropTable: Map[String, Float]): Unit = {
    val lootPile = LootPile(this, x, y)
    for ((key, value) <- dropTable) {
      if (randomGenerator.nextFloat() < value) {
        val item = Item.generateFromTemplate(key, Some(lootPile))
        lootPile.itemList += item
      }
    }
    if (lootPile.itemList.nonEmpty) lootPileList += lootPile
  }

  def spawnLootPile(x: Float, y: Float, item: Item): Unit = {
    val lootPile = LootPile(this, x, y)
    lootPile.itemList += item
    item.lootPile = Some(lootPile)
    lootPileList += lootPile
  }

  def updateLoot(): Unit = {
    lootPileList.foreach(
      lootPile =>
        if (!lootPile.bodyCreated) {
          lootPile.initBody()
          lootPile.bodyCreated = true
        }
    )

  }

  def loadTreasures(): Unit = {

    val source = scala.io.Source.fromFile(areaFilesLocation + "/treasures.json")
    val lines =
      try source.mkString
      finally source.close()

    val decoded = decode[TreasuresFile](lines)

    val result = decoded.getOrElse(throw new RuntimeException("failed to decode treasures file"))

    treasuresList.addAll(result.treasures.map(LootPile.loadFromTreasureSavedata(this, _)))

  }
}

case class TreasuresFile(treasures: List[TreasureSavedata])
case class TreasureSavedata(
  id: String,
  location: TreasureLocation,
  template: String,
  quantity: Int,
  damage: Option[Int],
  armor: Option[Int]
)
case class TreasureLocation(x: Float, y: Float)
