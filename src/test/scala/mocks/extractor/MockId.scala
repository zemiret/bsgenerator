package mocks.extractor

import com.bsgenerator.utils.IId

object MockId extends IId {
  val id = "id"

  override def randomId(): String = id
}
