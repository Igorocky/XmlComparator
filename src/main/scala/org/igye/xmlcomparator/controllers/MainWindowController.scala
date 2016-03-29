package org.igye.xmlcomparator.controllers

import javafx.fxml.FXML
import javafx.scene.layout.StackPane

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Window
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.Initable

@FxmlFile("fxml/MainWindow.fxml")
class MainWindowController extends Window with Initable {
  implicit val log: Logger = LogManager.getLogger()

  @FXML
  protected var rootNode: StackPane = _

  override def init(): Unit = {
    initWindow(rootNode)
  }
}
