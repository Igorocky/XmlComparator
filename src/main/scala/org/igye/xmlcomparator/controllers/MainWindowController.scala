package org.igye.xmlcomparator.controllers

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TextField, ScrollPane}
import javafx.scene.input.KeyCode._
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{VBox, HBox, StackPane}
import javafx.scene.paint.Color

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.{nodeToNodeOps, observableValueToObservableValueOperators, listToListOperators, propertyToPropertyOperators}
import org.igye.jfxutils.action.{Shortcut, Action}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.properties.ChgListener
import org.igye.jfxutils.{JfxUtils, Window}
import org.igye.xmlcomparator.models.{FileRow, MainModel}

import scala.collection.JavaConversions._

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
    bindModel()

    Action.bind(loadAction, loadBtn)
    JfxUtils.bindActionsToSceneProp(rootNode.sceneProperty(), actions)
  }

  private def bindModel(): Unit = {
    hboxInScrollPane.setBorder(JfxUtils.createBorder(Color.BLUE))
    hboxInScrollPane.prefWidthProperty() <== scrollPane.widthProperty()

    mainframeElemsVbox.getChildren <== (model.mainframeRows, createNodeFromFileRow)

    model.selectedMainframeRow ==> ChgListener {chg=>
      mainframeElemsVbox.getChildren.toList.map(_.asInstanceOf[HasAssignedRow with Selectable])
      .foreach{label =>
        val selected = model.selectedMainframeRow.getValue
        if (selected == label.getRow) {
          label.select1
        } else if (selected.accId == label.getRow.accId) {
          label.select2
        } else {
          label.unselect
        }
      }
    }
  }

  private def createNodeFromFileRow(fileRow: FileRow) = {
    val res = new Label(fileRow.id) with HasAssignedRow with Selectable {
      private val initialBackground = getBackground

      override def getRow: FileRow = fileRow

      override def select1: Unit = setBackground(JfxUtils.createBackground(Color.BLUE))
      override def select2: Unit = setBackground(JfxUtils.createBackground(Color.GREEN))

      override def unselect: Unit = setBackground(initialBackground)
    }
    res.hnd(MouseEvent.MOUSE_CLICKED){event=>
      model.selectedMainframeRow.setValue(fileRow)
    }
    res
  }
}
