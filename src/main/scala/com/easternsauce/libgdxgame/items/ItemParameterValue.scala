package com.easternsauce.libgdxgame.items

class ItemParameterValue private (val stringValue: Option[String], val boolValue: Option[Boolean], val intValue: Option[Int], val floatValue: Option[Float]) {
  def isUndefined: Boolean = {
    stringValue.isEmpty && boolValue.isEmpty && intValue.isEmpty && floatValue.isEmpty
  }
}

object ItemParameterValue {
  def apply(boolValue: Boolean) = new ItemParameterValue(None, Some(boolValue), None, None)
  def apply(stringValue: String) = new ItemParameterValue(Some(stringValue), None, None, None)
  def apply(intValue: Int) = new ItemParameterValue(None, None, Some(intValue), None)
  def apply(floatValue: Float) = new ItemParameterValue(None, None, None, Some(floatValue))
  def apply() = new ItemParameterValue(None, None, None, None)
}
