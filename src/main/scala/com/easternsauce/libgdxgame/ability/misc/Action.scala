package com.easternsauce.libgdxgame.ability.misc

trait Action {
  def perform(): Action
  def update(): Action
  def forceStop(): Action
}
