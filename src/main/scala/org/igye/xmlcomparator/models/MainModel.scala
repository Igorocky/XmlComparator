package org.igye.xmlcomparator.models

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javafx.collections.FXCollections

import scala.io.Source

class MainModel {
  val mainframeRows = FXCollections.observableArrayList[FileRow]()
  val javaRows = FXCollections.observableArrayList[FileRow]()
  val connections = FXCollections.observableArrayList[Connection]()

  def load(mainframeFile: String, javaFile: String, jarFile: String, resultFile: String): Unit = {
    val mainframePattern = """^(\d+)\s+(\S+)\s+(<.+)$""".r
    mainframeRows.clear()
    val file = new File(mainframeFile)
    Source.fromFile(file).getLines().zipWithIndex.map{
      case (str, lineNum) =>
        val mainframePattern(timeStr, source, xmlStr) = str.trim
        FileRow(
          id = file.getName + ":" + (lineNum + 1),
          timestamp = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
          source = source,
          xmlStr = xmlStr
        )
    }.toList.sortWith((r1, r2) => r1.timestamp.isBefore(r2.timestamp)).foreach{mainframeRows.add(_)}
  }
}
