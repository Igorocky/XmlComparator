package org.igye.xmlcomparator.models

import java.time.LocalDateTime
import javafx.beans.property.SimpleStringProperty

import org.igye.jfxutils.Implicits.propertyToPropertyOperators
import org.igye.jfxutils.properties.{Expr, SyncronizableList}

import scala.collection.JavaConversions._
import scala.xml.{Elem, XML}

case class FileRow(id: String, timestamp: LocalDateTime, source: String, xmlStr: String, mainModel: MainModel) {
  val xml: Elem = XML.loadString(xmlStr)
  val accId = extractAccId(xml)

  val appliedTransformations = new SyncronizableList[Transformation]

  val transformedXml: SimpleStringProperty = new SimpleStringProperty("")

  transformedXml <== Expr(mainModel.genTransformations, appliedTransformations){
    (mainModel.genTransformations.toList:::appliedTransformations.toList).foldLeft(xmlStr){(result, transformation) =>
      transformation.inst.getClass.getMethod("modify", classOf[String]).invoke(transformation.inst, result).asInstanceOf[String]
    }
  }

  private def extractAccId(doc: Elem): String = {
    val text = (doc \ "@accountid").text
    text.substring(0, text.length - 1)
  }
}