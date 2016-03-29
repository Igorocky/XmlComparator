package org.igye.xmlcomparator.models

import javafx.collections.FXCollections

class Connection(val mainframeRow: FileRow, javaRow: FileRow) {
  val mainframeModifications = FXCollections.observableArrayList[String]()
  val javaModifications = FXCollections.observableArrayList[String]()
}
