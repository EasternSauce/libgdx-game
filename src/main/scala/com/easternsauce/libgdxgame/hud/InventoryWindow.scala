package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.GameSystem
import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.assets.Assets
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable

class InventoryWindow {

  var visible = false
  var inventoryItemBeingMoved: Option[Int] = None
  var equipmentItemBeingMoved: Option[Int] = None

  val backgroundTexture: Texture = texture(Assets.backgroundTexture)

  val backgroundImage = new Image(backgroundTexture)

  private val backgroundRect: Rectangle = new Rectangle(
    (Gdx.graphics.getWidth * 0.2).toInt,
    (Gdx.graphics.getHeight * 0.3).toInt,
    (Gdx.graphics.getWidth * 0.6).toInt,
    (Gdx.graphics.getHeight * 0.6).toInt
  )

  private val backgroundOuterRect: Rectangle = new Rectangle(
    backgroundRect.x - (Gdx.graphics.getWidth * 0.1).toInt,
    backgroundRect.y - (Gdx.graphics.getHeight * 0.1).toInt,
    backgroundRect.width + (Gdx.graphics.getWidth * 0.2).toInt,
    backgroundRect.height + (Gdx.graphics.getHeight * 0.2).toInt
  )

  backgroundImage.setBounds(
    backgroundOuterRect.x,
    backgroundOuterRect.y,
    backgroundOuterRect.width,
    backgroundOuterRect.height
  )

  private val totalRows = 5
  private val totalColumns = 8
  val inventoryTotalSlots: Int = totalRows * totalColumns
  private val margin = 20
  private val slotSize = 40
  private val spaceBetweenSlots = 12
  private val spaceBeforeEquipment = 270

  private val inventoryWidth = margin + (slotSize + spaceBetweenSlots) * totalColumns
  private val inventoryHeight = margin + (slotSize + spaceBetweenSlots) * totalRows

  private val inventoryRectangles: mutable.Map[Int, Rectangle] = mutable.Map()

  private val equipmentTotalSlots = 8
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
          defaultFont.setColor(Color.DARK_GRAY)
          defaultFont.draw(
            batch.spriteBatch,
            equipmentTypeNames(index) + ":",
            rect.x - slotSize / 2 - 170,
            rect.y + slotSize / 2 + 7
          )
      }

      renderPlayerItems(batch)
      renderDescription(batch)
    }

  }

  def renderPlayerItems(batch: EsBatch): Unit = {
    val items = player.inventoryItems
    val equipment = player.equipmentItems

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

          if (item.quantity > 1) {
            defaultFont.setColor(Color.WHITE)
            defaultFont.draw(batch.spriteBatch, item.quantity.toString, x, y + 15)
          }
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

          if (item.quantity > 1) {
            defaultFont.setColor(Color.WHITE)
            defaultFont.draw(batch.spriteBatch, item.quantity.toString, x, y + 15)
          }
      }

    val x: Float = mousePositionWindowScaled.x
    val y: Float = mousePositionWindowScaled.y

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
    val x: Float = mousePositionWindowScaled.x
    val y: Float = mousePositionWindowScaled.y

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
        player.inventoryItems.get(index)
      case (_, Some(index)) if equipmentItemBeingMoved.isEmpty || index != equipmentItemBeingMoved.get =>
        player.equipmentItems.get(index)
      case _ => None
    }

    if (item.nonEmpty) {
      defaultFont.setColor(Color.DARK_GRAY)

      defaultFont.draw(
        batch.spriteBatch,
        item.get.template.name,
        backgroundRect.x + margin,
        backgroundRect.y + backgroundRect.height - (inventoryHeight + 5)
      )

      defaultFont.draw(
        batch.spriteBatch,
        item.get.getItemInformation(trader = false),
        backgroundRect.x + margin,
        backgroundRect.y + backgroundRect.height - (inventoryHeight + 35)
      )
    }

  }

  private def inventorySlotPositionX(index: Int): Float = {
    val currentColumn = index % totalColumns
    backgroundRect.x + margin + (slotSize + spaceBetweenSlots) * currentColumn
  }

  private def inventorySlotPositionY(index: Int): Float = {
    val currentRow = index / totalColumns
    backgroundRect.y + backgroundRect.height - (slotSize + margin + (slotSize + spaceBetweenSlots) * currentRow)
  }

  private def equipmentSlotPositionX(index: Int): Float = {
    backgroundRect.x + inventoryWidth + margin + spaceBeforeEquipment
  }

  private def equipmentSlotPositionY(index: Int): Float = {
    backgroundRect.y + backgroundRect.height - (slotSize + margin + (slotSize + spaceBetweenSlots) * index)
  }

  def moveItemClick(): Unit = {
    var inventorySlotClicked: Option[Int] = None
    var equipmentSlotClicked: Option[Int] = None

    val x: Float = mousePositionWindowScaled.x
    val y: Float = mousePositionWindowScaled.y

    if (backgroundOuterRect.contains(x, y)) {
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
          if (player.inventoryItems.contains(index)) inventoryItemBeingMoved = Some(index)
        case (_, _, _, Some(index)) =>
          if (player.equipmentItems.contains(index)) equipmentItemBeingMoved = Some(index)
        case _ =>
          inventoryItemBeingMoved = None
          equipmentItemBeingMoved = None
      }
    } else {
      if (inventoryItemBeingMoved.nonEmpty) {
        val item = player.inventoryItems(inventoryItemBeingMoved.get)
        currentArea.get.spawnLootPile(player.pos.x, player.pos.y, item)

        player.inventoryItems.remove(inventoryItemBeingMoved.get)

        sound(Assets.coinBagSound).play(0.3f)

        inventoryItemBeingMoved = None
      }
      if (equipmentItemBeingMoved.nonEmpty) {
        val item = player.inventoryItems(equipmentItemBeingMoved.get)
        currentArea.get.spawnLootPile(player.pos.x, player.pos.y, item)

        player.equipmentItems.remove(equipmentItemBeingMoved.get)

        sound(Assets.coinBagSound).play(0.3f)

        equipmentItemBeingMoved = None
      }
      promoteSecondaryToPrimaryWeapon()
    }

  }

  private def promoteSecondaryToPrimaryWeapon(): Unit = {
    if (
      !player.equipmentItems
        .contains(primaryWeaponIndex) && player.equipmentItems.contains(secondaryWeaponIndex)
    ) {
      player.equipmentItems(primaryWeaponIndex) = player.equipmentItems(secondaryWeaponIndex)
      player.equipmentItems.remove(secondaryWeaponIndex)
    }
  }

  def swapInventorySlotContent(fromIndex: Int, toIndex: Int): Unit = {
    val itemFrom = player.inventoryItems.get(fromIndex)
    val itemTo = player.inventoryItems.get(toIndex)

    val temp = itemTo

    if (itemFrom.nonEmpty) player.inventoryItems(toIndex) = itemFrom.get
    else player.inventoryItems.remove(toIndex)
    if (temp.nonEmpty) player.inventoryItems(fromIndex) = temp.get
    else player.inventoryItems.remove(fromIndex)

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }

  def swapEquipmentSlotContent(fromIndex: Int, toIndex: Int): Unit = {
    val itemFrom = player.equipmentItems.get(fromIndex)
    val itemTo = player.equipmentItems.get(toIndex)

    val temp = itemTo

    val fromEquipmentTypeMatches =
      itemFrom.nonEmpty && itemFrom.get.template.parameters("equipableType").stringValue.get == GameSystem
        .equipmentTypes(toIndex)
    val toEquipmentTypeMatches =
      itemTo.nonEmpty && itemTo.get.template.parameters("equipableType").stringValue.get == equipmentTypes(fromIndex)

    if (fromEquipmentTypeMatches && toEquipmentTypeMatches) {
      if (itemFrom.nonEmpty) player.equipmentItems(toIndex) = itemFrom.get
      else player.equipmentItems.remove(toIndex)
      if (temp.nonEmpty) player.equipmentItems(fromIndex) = temp.get
      else player.equipmentItems.remove(fromIndex)
    }

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }

  def swapBetweenInventoryAndEquipment(inventoryIndex: Int, equipmentIndex: Int): Unit = {
    val inventoryItem = player.inventoryItems.get(inventoryIndex)
    val equipmentItem = player.equipmentItems.get(equipmentIndex)

    val temp = equipmentItem

    val equipmentTypeMatches =
      inventoryItem.nonEmpty && inventoryItem.get.template
        .parameters("equipableType")
        .stringValue
        .get == equipmentTypes(equipmentIndex)

    if (inventoryItem.isEmpty || equipmentTypeMatches) {
      if (temp.nonEmpty) player.inventoryItems(inventoryIndex) = temp.get
      else player.inventoryItems.remove(inventoryIndex)
      if (inventoryItem.nonEmpty) player.equipmentItems(equipmentIndex) = inventoryItem.get
      else player.equipmentItems.remove(equipmentIndex)
    }

    promoteSecondaryToPrimaryWeapon()

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }

  def tryDropSelectedItem(): Unit = {
    val x: Float = mousePositionWindowScaled.x
    val y: Float = mousePositionWindowScaled.y

    var inventorySlotHovered: Option[Int] = None
    var equipmentSlotHovered: Option[Int] = None

    inventoryRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => inventorySlotHovered = Some(k) }

    equipmentRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => equipmentSlotHovered = Some(k) }

    if (inventorySlotHovered.nonEmpty && player.inventoryItems.contains(inventorySlotHovered.get)) {
      currentArea.get.spawnLootPile(player.pos.x, player.pos.y, player.inventoryItems(inventorySlotHovered.get))
      player.inventoryItems.remove(inventorySlotHovered.get)

      sound(Assets.coinBagSound).play(0.3f)

      promoteSecondaryToPrimaryWeapon()

    }

    if (equipmentSlotHovered.nonEmpty && player.equipmentItems.contains(inventorySlotHovered.get)) {
      currentArea.get.spawnLootPile(player.pos.x, player.pos.y, player.equipmentItems(equipmentSlotHovered.get))
      player.equipmentItems.remove(equipmentSlotHovered.get)

      sound(Assets.coinBagSound).play(0.3f)
    }
  }

  def useItemClick(): Unit = {

    val x: Float = mousePositionWindowScaled.x
    val y: Float = mousePositionWindowScaled.y

    var inventorySlotHovered: Option[Int] = None
    var equipmentSlotHovered: Option[Int] = None

    inventoryRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => inventorySlotHovered = Some(k) }

    equipmentRectangles
      .filter { case (_, v) => v.contains(x, y) }
      .foreach { case (k, _) => equipmentSlotHovered = Some(k) }

    if (inventorySlotHovered.nonEmpty && player.inventoryItems.contains(inventorySlotHovered.get)) {
      val item = player.inventoryItems.get(inventorySlotHovered.get)
      if (item.nonEmpty && item.get.template.consumable.get) {
        player.useItem(item.get)
        if (item.get.quantity <= 1) player.inventoryItems.remove(inventorySlotHovered.get)
        else item.get.quantity = item.get.quantity - 1
      }
    }

    if (equipmentSlotHovered.nonEmpty && player.equipmentItems.contains(equipmentSlotHovered.get)) {
      val item = player.equipmentItems.get(equipmentSlotHovered.get)
      if (item.nonEmpty && item.get.template.consumable.get) {
        player.useItem(item.get)
        if (item.get.quantity <= 1) player.equipmentItems.remove(equipmentSlotHovered.get)
        else item.get.quantity = item.get.quantity - 1
      }
    }

  }

  def swapPrimaryAndSecondaryWeapons(): Unit = {
    if (player.equipmentItems.contains(secondaryWeaponIndex)) {
      val primaryWeapon = player.equipmentItems(primaryWeaponIndex)
      val secondaryWeapon = player.equipmentItems(secondaryWeaponIndex)

      player.equipmentItems(secondaryWeaponIndex) = primaryWeapon
      player.equipmentItems(primaryWeaponIndex) = secondaryWeapon

    }
  }
}
