package org.igye.xmlcomparator.controllers

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TextField, ScrollPane}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.{VBox, HBox, StackPane}
import javafx.scene.paint.Color

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.{listToListOperators, propertyToPropertyOperators}
import org.igye.jfxutils.action.{Shortcut, Action}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.{JfxUtils, Window}
import org.igye.xmlcomparator.models.{FileRow, MainModel}

@FxmlFile("fxml/MainWindow.fxml")
class MainWindowController extends Window with Initable {
  implicit val log: Logger = LogManager.getLogger()

  private val model = new MainModel

  @FXML
  protected var rootNode: StackPane = _
  @FXML
  protected var mainframeFld: TextField = _
  @FXML
  protected var scrollPane: ScrollPane = _
  @FXML
  protected var hboxInScrollPane: HBox = _
  @FXML
  protected var mainframeElemsVbox: VBox = _
  @FXML
  protected var loadBtn: Button = _

  private val loadAction = new Action {
    override val description: String = "Load"
    setShortcut(Shortcut(CONTROL, L))
    override protected def onAction(): Unit = {
      model.load(mainframeFld.getText, null, null, null)
    }
  }

  private val actions = List(
    loadAction
  )

  override def init(): Unit = {
    require(rootNode != null)
    require(mainframeFld != null)
    require(scrollPane != null)
    require(hboxInScrollPane != null)
    require(mainframeElemsVbox != null)

    initWindow(rootNode)
    hboxInScrollPane.setBorder(JfxUtils.createBorder(Color.BLUE))
    hboxInScrollPane.prefWidthProperty() <== scrollPane.widthProperty()

    mainframeElemsVbox.getChildren <== (model.mainframeRows, createNodeFromFileRow)

    Action.bind(loadAction, loadBtn)
    JfxUtils.bindActionsToSceneProp(rootNode.sceneProperty(), actions)
  }

  private def createNodeFromFileRow(fileRow: FileRow) = {
    new Label(fileRow.id) with HasAssignedRow {
      override def getRow: FileRow = fileRow
    }
  }
}
