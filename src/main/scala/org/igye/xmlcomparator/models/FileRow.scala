package org.igye.xmlcomparator.models

import java.time.LocalDateTime

import scala.xml.{Elem, XML}

case class FileRow(id: String, timestamp: LocalDateTime, source: String, xmlStr: String) {
  val xml: Elem = XML.loadString(xmlStr)
  val accId = extractAccId(xml)


  private def extractAccId(doc: Elem): String = {
    val text = (doc \ "@accountid").text
    text.substring(0, text.length - 1)
  }
}