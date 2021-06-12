package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable

class InventoryWindow(game: RpgGame) {

  var visible = false
  var inventoryItemBeingMoved: Option[Int] = None
  var equipmentItemBeingMoved: Option[Int] = None

  val backgroundTexture: Texture = RpgGame.manager.get(AssetPaths.backgroundTexture, classOf[Texture])

  val backgroundImage = new Image(backgroundTexture)

  private val background: Rectangle = new Rectangle(
    (Gdx.graphics.getWidth * 0.2).toInt,
    (Gdx.graphics.getHeight * 0.3).toInt,
    (Gdx.graphics.getWidth * 0.6).toInt,
    (Gdx.graphics.getHeight * 0.6).toInt
  )

  backgroundImage.setBounds(
    background.x - (Gdx.graphics.getWidth * 0.1).toInt,
    background.y - (Gdx.graphics.getHeight * 0.1).toInt,
    background.width + (Gdx.graphics.getWidth * 0.2).toInt,
    background.height + (Gdx.graphics.getHeight * 0.2).toInt
  )

  private val totalRows = 5
  private val totalColumns = 8
  val inventoryTotalSlots: Int = totalRows * totalColumns
  private val margin = 35
  private val slotSize = 40
  private val spaceBetweenSlots = 12
  private val spaceBeforeEquipment = 220

  private val inventoryWidth = margin + (slotSize + spaceBetweenSlots) * totalColumns
  private val inventoryHeight = margin + (slotSize + spaceBetweenSlots) * totalRows

  private val inventoryRectangles: mutable.Map[Int, Rectangle] = mutable.Map()

  private val equipmentTotalSlots = 6
  private val equipmentRectangles: mutable.Map[Int, Rectangle] = mutable.Map()

  defineSlotRectangles()

  private def defineSlotRectangles(): Unit = {
    for (i <- 0 until inventoryTotalSlots) {
      inventoryRectangles += (i -> new Rectangle(
        inventorySlotPositionX(i),
        inventorySlotPositionY(i),
        slotSize,
        slotSize
      ))
    }

    for (i <- 0 until equipmentTotalSlots) {
      equipmentRectangles += (i -> new Rectangle(
        equipmentSlotPositionX(i),
        equipmentSlotPositionY(i),
        slotSize,
        slotSize
      ))
    }

  }

  def render(batch: EsBatch): Unit = {
    if (visible) {
      backgroundImage.draw(batch.spriteBatch, 1.0f)

      inventoryRectangles.values.foreach(rect => {
        batch.shapeDrawer.filledRectangle(rect.x - 3, rect.y - 3, rect.width + 6, rect.height + 6, Color.BROWN)
        batch.shapeDrawer.filledRectangle(rect, Color.BLACK)
      })

      equipmentRectangles.foreach {
        case (index, rect) =>
          batch.shapeDrawer.filledRectangle(rect.x - 3, rect.y - 3, rect.width + 6, rect.height + 6, Color.BROWN)
          batch.shapeDrawer.filledRectangle(rect, Color.BLACK)
          RpgGame.defaultFont.setColor(Color.DARK_GRAY)
          RpgGame.defaultFont.draw(
            batch.spriteBatch,
            RpgGame.equipmentTypes(index).capitalize + ":",
            rect.x - slotSize / 2 - 70,
            rect.y + slotSize / 2 + 7
          )
      }

      renderPlayerItems(batch)
      renderDescription(batch)
    }

  }

  def renderPlayerItems(batch: EsBatch): Unit = {
    val items = game.player.inventoryItems
    val equipment = game.player.equipmentItems

    items
      .filterNot {
        case (index, _) => if (inventoryItemBeingMoved.nonEmpty) inventoryItemBeingMoved.get == index else false
      }
      .foreach {
        case (index, item) =>
          val textureRegion = item.template.textureRegion
          val x = inventorySlotPositionX(index)
          val y = inventorySlotPositionY(index)
          batch.spriteBatch.draw(textureRegion, x, y, slotSize, slotSize)
      }

    equipment
      .filterNot {
        case (index, _) => if (equipmentItemBeingMoved.nonEmpty) equipmentItemBeingMoved.get == index else false
      }
      .foreach {
        case (index, item) =>
          val textureRegion = item.template.textureRegion
          val x = equipmentSlotPositionX(index)
          val y = equipmentSlotPositionY(index)
          batch.spriteBatch.draw(textureRegion, x, y, slotSize, slotSize)
      }

    val x: Float = game.mousePositionWindowScaled.x
    val y: Float = game.mousePositionWindowScaled.y

    if (inventoryItemBeingMoved.nonEmpty) {
      batch.spriteBatch.draw(
        items(inventoryItemBeingMoved.get).template.textureRegion,
        x - slotSize / 2,
        y - slotSize / 2,
        slotSize,
        slotSize
      )
    }

    if (equipmentItemBeingMoved.nonEmpty) {
      batch.spriteBatch.draw(
        equipment(equipmentItemBeingMoved.get).template.textureRegion,
        x - slotSize / 2,
        y - slotSize / 2,
        slotSize,
        slotSize
      )
    }
  }

  def renderDescription(batch: EsBatch): Unit = {
    val x: Float = game.mousePositionWindowScaled.x
    val y: Float = game.mousePositionWindowScaled.y

    var inventorySlotMousedOver: Option[Int] = None
    var equipmentSlotMousedOver: Option[Int] = None

    inventoryRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => inventorySlotMousedOver = Some(k) }

    equipmentRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => equipmentSlotMousedOver = Some(k) }

    val item = (inventorySlotMousedOver, equipmentSlotMousedOver) match {
      case (Some(index), _) if inventoryItemBeingMoved.isEmpty || index != inventoryItemBeingMoved.get =>
        game.player.inventoryItems.get(index)
      case (_, Some(index)) if equipmentItemBeingMoved.isEmpty || index != equipmentItemBeingMoved.get =>
        game.player.equipmentItems.get(index)
      case _ => None
    }

    if (item.nonEmpty) {
      RpgGame.defaultFont.setColor(Color.DARK_GRAY)

      RpgGame.defaultFont.draw(
        batch.spriteBatch,
        item.get.template.name,
        background.x + margin,
        background.y + background.height - (inventoryHeight + 5)
      )

      RpgGame.defaultFont.draw(
        batch.spriteBatch,
        item.get.getItemInformation(trader = false),
        background.x + margin,
        background.y + background.height - (inventoryHeight + 35)
      )
    }

  }

  private def inventorySlotPositionX(index: Int): Float = {
    val currentColumn = index % totalColumns
    background.x + margin + (slotSize + spaceBetweenSlots) * currentColumn
  }

  private def inventorySlotPositionY(index: Int): Float = {
    val currentRow = index / totalColumns
    background.y + background.height - (slotSize + margin + (slotSize + spaceBetweenSlots) * currentRow)
  }

  private def equipmentSlotPositionX(index: Int): Float = {
    background.x + inventoryWidth + margin + spaceBeforeEquipment
  }

  private def equipmentSlotPositionY(index: Int): Float = {
    background.y + background.height - (slotSize + margin + (slotSize + spaceBetweenSlots) * index)
  }

  def handleMouseClicked(): Unit = {
    var inventorySlotClicked: Option[Int] = None
    var equipmentSlotClicked: Option[Int] = None

    val x: Float = game.mousePositionWindowScaled.x
    val y: Float = game.mousePositionWindowScaled.y

    inventoryRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => inventorySlotClicked = Some(k) }

    equipmentRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => equipmentSlotClicked = Some(k) }

    (inventoryItemBeingMoved, equipmentItemBeingMoved, inventorySlotClicked, equipmentSlotClicked) match {
      case (Some(from), _, Some(to), _) => swapInventorySlotContent(from, to)
      case (Some(from), _, _, Some(to)) => swapBetweenInventoryAndEquipment(from, to)
      case (_, Some(from), Some(to), _) => swapBetweenInventoryAndEquipment(to, from)
      case (_, Some(from), _, Some(to)) => swapEquipmentSlotContent(from, to)
      case (_, _, Some(index), _) =>
        if (game.player.inventoryItems.contains(index)) inventoryItemBeingMoved = Some(index)
      case (_, _, _, Some(index)) =>
        if (game.player.equipmentItems.contains(index)) equipmentItemBeingMoved = Some(index)
      case _ =>
        inventoryItemBeingMoved = None
        equipmentItemBeingMoved = None
    }

  }

  def swapInventorySlotContent(fromIndex: Int, toIndex: Int): Unit = {
    val itemFrom = game.player.inventoryItems.get(fromIndex)
    val itemTo = game.player.inventoryItems.get(toIndex)

    val temp = itemTo

    if (itemFrom.nonEmpty) game.player.inventoryItems(toIndex) = itemFrom.get
    else game.player.inventoryItems.remove(toIndex)
    if (temp.nonEmpty) game.player.inventoryItems(fromIndex) = temp.get
    else game.player.inventoryItems.remove(fromIndex)

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }

  def swapEquipmentSlotContent(fromIndex: Int, toIndex: Int): Unit = {
    val itemFrom = game.player.equipmentItems.get(fromIndex)
    val itemTo = game.player.equipmentItems.get(toIndex)

    val temp = itemTo

    val fromEquipmentTypeMatches =
      itemFrom.nonEmpty && itemFrom.get.template.parameters("equipableType").stringValue.get == RpgGame
        .equipmentTypes(toIndex)
    val toEquipmentTypeMatches =
      itemTo.nonEmpty && itemTo.get.template.parameters("equipableType").stringValue.get == RpgGame.equipmentTypes(
        fromIndex
      )

    if (fromEquipmentTypeMatches && toEquipmentTypeMatches) {
      if (itemFrom.nonEmpty) game.player.equipmentItems(toIndex) = itemFrom.get
      else game.player.equipmentItems.remove(toIndex)
      if (temp.nonEmpty) game.player.equipmentItems(fromIndex) = temp.get
      else game.player.equipmentItems.remove(fromIndex)
    }

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }

  def swapBetweenInventoryAndEquipment(inventoryIndex: Int, equipmentIndex: Int): Unit = {
    val inventoryItem = game.player.inventoryItems.get(inventoryIndex)
    val equipmentItem = game.player.equipmentItems.get(equipmentIndex)

    val temp = equipmentItem

    val equipmentTypeMatches =
      inventoryItem.nonEmpty && inventoryItem.get.template
        .parameters("equipableType")
        .stringValue
        .get == RpgGame.equipmentTypes(equipmentIndex)

    if (inventoryItem.isEmpty || equipmentTypeMatches) {
      if (temp.nonEmpty) game.player.inventoryItems(inventoryIndex) = temp.get
      else game.player.inventoryItems.remove(inventoryIndex)
      if (inventoryItem.nonEmpty) game.player.equipmentItems(equipmentIndex) = inventoryItem.get
      else game.player.equipmentItems.remove(equipmentIndex)
    }

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }
}
