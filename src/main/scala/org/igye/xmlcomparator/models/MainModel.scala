package org.igye.xmlcomparator.models

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.collections.FXCollections

import scala.io.Source

class MainModel {
  val mainframeRows = FXCollections.observableArrayList[FileRow]()
  val javaRows = FXCollections.observableArrayList[FileRow]()
  val connections = FXCollections.observableArrayList[Connection]()

  val selectedMainframeRow: ObjectProperty[FileRow] = new SimpleObjectProperty(null)
  val selectedJavaRow: ObjectProperty[FileRow] = new SimpleObjectProperty(null)

  def load(mainframeFile: String, javaFile: String, jarFile: String, resultFile: String): Unit = {
    mainframeRows.clear()
    loadFromFile(new File(mainframeFile)).foreach{mainframeRows.add}
    loadFromFile(new File(javaFile)).foreach{javaRows.add}
  }

  private def loadFromFile(file: File) = {
    val rowPattern = """^(\d+)\s+(\S+)\s+(<.+)$""".r
    Source.fromFile(file).getLines().zipWithIndex.map{
      case (str, lineNum) =>
        val rowPattern(timeStr, source, xmlStr) = str.trim
        FileRow(
          id = file.getName + ":" + (lineNum + 1),
          timestamp = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
          source = source,
          xmlStr = xmlStr
        )
    }.toList.sortWith((r1, r2) => r1.timestamp.isBefore(r2.timestamp))
  }
}
