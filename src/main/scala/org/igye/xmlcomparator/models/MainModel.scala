package org.igye.xmlcomparator.models

import java.io.File
import java.net.{URL, URLClassLoader}
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZonedDateTime}
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.collections.FXCollections
import javafx.scene.control.TextField

import org.igye.commonutils.JaxbSupport
import org.igye.jfxutils.properties.SyncronizableList
import org.igye.xmlcomparator.report.{ConnectionPojo, PerRowModifiers, ReportPojo}
import org.reflections.Reflections
import org.reflections.scanners.{ResourcesScanner, SubTypesScanner}
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder, FilterBuilder}

import scala.collection.JavaConversions._
import scala.io.Source

class MainModel {
  val mainframeRows = FXCollections.observableArrayList[FileRow]()
  val javaRows = FXCollections.observableArrayList[FileRow]()
  val connections = FXCollections.observableArrayList[Connection]()
  val possibleTransformations = FXCollections.observableArrayList[Transformation]()
  val genTransformations = new SyncronizableList[Transformation]

  val selectedMainframeRow: ObjectProperty[FileRow] = new SimpleObjectProperty(null)
  val selectedJavaRow: ObjectProperty[FileRow] = new SimpleObjectProperty(null)

  var report: ReportPojo = _

  def connect(mainframeRow: FileRow, javaRow: FileRow): Unit = {
    connections.add(Connection(mainframeRow, javaRow))
  }

  def disconnect(mainframeRow: FileRow, javaRow: FileRow): Unit = {
    connections.remove(Connection(mainframeRow, javaRow))
  }

  def isNotConnected(fileRow: FileRow) = {
    connections.toList.find(c => c.mainframeRow == fileRow || c.javaRow == fileRow).isEmpty
  }

  def areConnected(mainframeRow: FileRow, javaRow: FileRow) = {
    connections.toList.find(c => c.mainframeRow == mainframeRow && c.javaRow == javaRow).isDefined
  }

  def load(mainframeFile: TextField, javaFile: TextField, jarFile: TextField, resultFile: String): Unit = {
    selectedMainframeRow.setValue(null)
    selectedJavaRow.setValue(null)
    mainframeRows.clear()
    javaRows.clear()
    possibleTransformations.clear()
    genTransformations.clear()
    connections.clear()

    if (resultFile.trim != "") {
      report = readReport(resultFile)
      mainframeFile.setText(report.mainframeFile)
      javaFile.setText(report.javaFile)
      jarFile.setText(report.jarFile)
    }

    readModifiers(jarFile.getText).foreach(possibleTransformations.add(_))

    loadFromFile(new File(mainframeFile.getText)).foreach{mainframeRows.add}
    loadFromFile(new File(javaFile.getText)).foreach{javaRows.add}

    if (report != null) {
      mainframeFile.setText("")
      javaFile.setText("")
      jarFile.setText("")

      report.generalModifiers.foreach{gmName =>
        genTransformations.add(
          possibleTransformations.find(_.name == gmName).getOrElse(
            throw new Exception(s"Can't find modifier with name $gmName")
          )
        )
      }

      val allRows = (mainframeRows.toList:::javaRows.toList)
      def findRow(id: String) = {
        allRows.find(_.id == id).getOrElse(
          throw new Exception(s"Can't find row with id ${id}")
        )
      }

      report.rowModifiersList.foreach{perRowModifs=>
        val row = findRow(perRowModifs.lineId)
        perRowModifs.modifiers.foreach{trName=>
          row.appliedTransformations.add(
            possibleTransformations.find(_.name == trName).getOrElse(
              throw new Exception(s"Can't find modifier with name $trName")
            )
          )
        }
      }

      report.connections.foreach{con=>
        connections.add(Connection(findRow(con.mainframeId), findRow(con.javaId)))
      }
    }
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
          xmlStr = xmlStr,
          mainModel = this
        )
    }.toList.sortWith((r1, r2) => r1.timestamp.isBefore(r2.timestamp))
  }

  private def readModifiers(jarPath: String): List[Transformation] = {
    val child = new URLClassLoader(Array[URL](new File(jarPath).toURI.toURL()), this.getClass().getClassLoader())

    val classLoadersList = new java.util.LinkedList[ClassLoader]()
    classLoadersList.add(child)

    val reflections = new Reflections(new ConfigurationBuilder()
      .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
      .setUrls(ClasspathHelper.forClassLoader(child))
      .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("org.igye.comparex.modifiers"))))

    reflections.getAllTypes().toList.map{className=>
      Transformation(className.split('.').last, Class.forName(className, true, child).newInstance())
    }
  }

  def save(mainframeFile: TextField, javaFile: TextField, jarFile: TextField, resultFile: String): Unit = {
    report = createReport(mainframeFile.getText, javaFile.getText, jarFile.getText)
    JaxbSupport.marshal(report, new File(resultFile))
    mainframeFile.setText("")
    javaFile.setText("")
    jarFile.setText("")
  }

  private def createReport(mainframeFile: String, javaFile: String, jarFile: String): ReportPojo = {
    val res = new ReportPojo

    res.lastModifiedOn = ZonedDateTime.now().toString
    res.mainframeFile = if (report != null) report.mainframeFile else mainframeFile
    res.javaFile = if (report != null) report.javaFile else javaFile
    res.jarFile = if (report != null) report.jarFile else jarFile

    genTransformations.foreach(tr => res.generalModifiers.add(tr.name))

    (mainframeRows.toList:::javaRows.toList).filter(_.appliedTransformations.size() > 0).foreach{fileRow=>
      val perRowModifiers = new PerRowModifiers
      res.rowModifiersList.add(perRowModifiers)
      perRowModifiers.lineId = fileRow.id
      fileRow.appliedTransformations.toList.foreach(tr => perRowModifiers.modifiers.add(tr.name))
    }
    connections.toList.foreach{con=>
      val conPojo = new ConnectionPojo
      conPojo.mainframeId = con.mainframeRow.id
      conPojo.javaId = con.javaRow.id
      res.connections.add(conPojo)
    }
    res
  }

  private def readReport(fileStr: String): ReportPojo = {
    JaxbSupport.unmarshal[ReportPojo](new File(fileStr))
  }
}
