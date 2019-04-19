package com.bsgenerator.utils

import java.util.UUID

object Helpers {
  def randomId(): String = UUID.randomUUID.toString
}
