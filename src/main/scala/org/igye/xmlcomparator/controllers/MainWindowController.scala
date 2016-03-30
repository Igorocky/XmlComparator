package org.igye.xmlcomparator.controllers

import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.input.KeyCode._
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{Pane, VBox, HBox, StackPane}
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
  protected var javaFld: TextField = _
  @FXML
  protected var scrollPane: ScrollPane = _
  @FXML
  protected var hboxInScrollPane: HBox = _
  @FXML
  protected var mainframeElemsVbox: VBox = _
  @FXML
  protected var javaElemsVbox: VBox = _
  @FXML
  protected var loadBtn: Button = _
  @FXML
  protected var rightPane: Pane = _
  @FXML
  protected var secondSplitPane: SplitPane = _
  @FXML
  protected var mainframeDetailedViewVbox: VBox = _
  @FXML
  protected var javaDetailedViewVbox: VBox = _

  private val loadAction = new Action {
    override val description: String = "Load"
    setShortcut(Shortcut(CONTROL, L))
    override protected def onAction(): Unit = {
      model.load(mainframeFld.getText, javaFld.getText, null, null)
    }
  }

  private val actions = List(
    loadAction
  )

  override def init(): Unit = {
    require(rootNode != null)
    require(mainframeFld != null)
    require(javaFld != null)
    require(scrollPane != null)
    require(hboxInScrollPane != null)
    require(mainframeElemsVbox != null)
    require(javaElemsVbox != null)
    require(rightPane != null)
    require(secondSplitPane != null)
    require(mainframeDetailedViewVbox != null)
    require(javaDetailedViewVbox != null)

    initWindow(rootNode)
    bindModel()

    Action.bind(loadAction, loadBtn)
    JfxUtils.bindActionsToSceneProp(rootNode.sceneProperty(), actions)
  }

  private def bindModel(): Unit = {
    hboxInScrollPane.setBorder(JfxUtils.createBorder(Color.BLUE))
    hboxInScrollPane.prefWidthProperty() <== scrollPane.widthProperty()
    mainframeElemsVbox.setBorder(JfxUtils.createBorder(Color.RED))
    javaElemsVbox.setBorder(JfxUtils.createBorder(Color.RED))

    rightPane.setBorder(JfxUtils.createBorder(Color.YELLOW))

    secondSplitPane.setLayoutX(0)
    secondSplitPane.setLayoutY(0)
    secondSplitPane.prefWidthProperty() <== rightPane.widthProperty()
    secondSplitPane.prefHeightProperty() <== rightPane.heightProperty()

    mainframeElemsVbox.getChildren <== (model.mainframeRows, createNodeFromFileRow(_: FileRow, model.selectedMainframeRow.setValue(_)))
    javaElemsVbox.getChildren <== (model.javaRows, createNodeFromFileRow(_: FileRow, model.selectedJavaRow.setValue(_)))

    model.selectedMainframeRow ==> createSelectionListener(mainframeElemsVbox, mainframeDetailedViewVbox)
    model.selectedJavaRow ==> createSelectionListener(javaElemsVbox, javaDetailedViewVbox)
  }

  private def createSelectionListener(elemsVbox: VBox, detailedViewVbox: VBox) = {
    ChgListener[FileRow] {chg=>
      val selected = chg.newValue
      elemsVbox.getChildren.toList.map(_.asInstanceOf[HasAssignedRow with Selectable])
        .foreach{label =>
          if (selected == label.getRow) {
            label.select1
          } else if (selected.accId == label.getRow.accId) {
            label.select2
          } else {
            label.unselect
          }
        }
      detailedViewVbox.getChildren.clear()
      if (selected != null) {
        detailedViewVbox.getChildren.add(new Label(selected.xmlStr))
      }
    }
  }

  private def createNodeFromFileRow(fileRow: FileRow, mouseClickedHnd: FileRow => Unit) = {
    val res = new Label(fileRow.id) with HasAssignedRow with Selectable {
      private val initialBackground = getBackground

      override def getRow: FileRow = fileRow

      override def select1: Unit = setBackground(JfxUtils.createBackground(Color.BLUE))
      override def select2: Unit = setBackground(JfxUtils.createBackground(Color.GREEN))

      override def unselect: Unit = setBackground(initialBackground)
    }
    res.hnd(MouseEvent.MOUSE_CLICKED){ event =>
      mouseClickedHnd(fileRow)
    }
    res
  }
}
