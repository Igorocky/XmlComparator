package org.igye.xmlcomparator.controllers

import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.input.KeyCode._
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{HBox, Pane, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.shape.Line

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.{listToListOperators, nodeToNodeOps, observableValueToObservableValueOperators, propertyToPropertyOperators}
import org.igye.jfxutils.action.{Action, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.{FxmlSupport, Initable}
import org.igye.jfxutils.properties.{ChgListener, Expr}
import org.igye.jfxutils.{JfxUtils, Window}
import org.igye.xmlcomparator.models.{Transformation, Connection, FileRow, MainModel}

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
  protected var jarFld: TextField = _
  @FXML
  protected var scrollPane: ScrollPane = _
  @FXML
  protected var hboxInScrollPane: HBox = _
  @FXML
  protected var mainframeElemsVbox: VBox = _
  @FXML
  protected var javaElemsVbox: VBox = _
  @FXML
  protected var arrowsPane: Pane = _
  @FXML
  protected var loadBtn: Button = _
  @FXML
  protected var connectBtn: Button = _
  @FXML
  protected var disconnectBtn: Button = _
  @FXML
  protected var detailedMfView: ScrollPane = _
  @FXML
  protected var detailedJavaView: ScrollPane = _
  @FXML
  protected var mfTimestampLabel: Label = _
  @FXML
  protected var initialMfLabel: Label = _
  @FXML
  protected var resultMfLabel: Label = _
  @FXML
  protected var javaTimestampLabel: Label = _
  @FXML
  protected var initialJavaLabel: Label = _
  @FXML
  protected var resultJavaLabel: Label = _
  @FXML
  protected var genTransVbox: VBox = _
  protected val genTransSelectorController = FxmlSupport.load[SelectorController]
  @FXML
  protected var mfSelector: HBox = _
  protected val mfSelectorController = FxmlSupport.load[SelectorController]

  private val loadAction = new Action {
    override val description: String = "Load"
    setShortcut(Shortcut(CONTROL, L))
    override protected def onAction(): Unit = {
      model.load(mainframeFld.getText, javaFld.getText, jarFld.getText, null)
    }
  }

  private val connectAction = new Action {
    override val description: String = "Connect"
    enabled <== Expr(model.selectedMainframeRow, model.selectedJavaRow, model.connections){
      model.selectedMainframeRow.get() != null && model.isNotConnected(model.selectedMainframeRow.get()) &&
        model.selectedJavaRow.get() != null && model.isNotConnected(model.selectedJavaRow.get())
    }
    override protected def onAction(): Unit = {
      model.connect(model.selectedMainframeRow.get(), model.selectedJavaRow.get())
    }
  }

  private val disconnectAction = new Action {
    override val description: String = "Disconnect"
    enabled <== Expr(model.selectedMainframeRow, model.selectedJavaRow, model.connections){
      model.selectedMainframeRow.get() != null && model.selectedJavaRow.get() != null &&
        model.areConnected(model.selectedMainframeRow.get(), model.selectedJavaRow.get())
    }
    override protected def onAction(): Unit = {
      model.disconnect(model.selectedMainframeRow.get(), model.selectedJavaRow.get())
    }
  }

  private val actions = List(
    loadAction
    ,connectAction
    ,disconnectAction
  )

  override def init(): Unit = {
    require(rootNode != null)
    require(mainframeFld != null)
    require(javaFld != null)
    require(jarFld != null)
    require(scrollPane != null)
    require(hboxInScrollPane != null)
    require(mainframeElemsVbox != null)
    require(javaElemsVbox != null)
    require(arrowsPane != null)
    require(detailedMfView != null)
    require(detailedJavaView != null)
    require(loadBtn != null)
    require(connectBtn != null)
    require(disconnectBtn != null)
    require(mfTimestampLabel != null)
    require(initialMfLabel != null)
    require(resultMfLabel != null)
    require(javaTimestampLabel != null)
    require(initialJavaLabel != null)
    require(resultJavaLabel != null)
    require(mfSelector != null)
    require(genTransVbox != null)

    initWindow(rootNode)
    bindModel()

    Action.bind(loadAction, loadBtn)
    Action.bind(connectAction, connectBtn)
    Action.bind(disconnectAction, disconnectBtn)
    JfxUtils.bindActionsToSceneProp(rootNode.sceneProperty(), actions)
  }

  private def bindModel(): Unit = {
    mfSelector.getChildren.add(mfSelectorController.rootPane)
    genTransVbox.getChildren.add(genTransSelectorController.rootPane)
    hboxInScrollPane.setBorder(JfxUtils.createBorder(Color.BLUE))
    hboxInScrollPane.prefWidthProperty() <== scrollPane.widthProperty()

    mainframeElemsVbox.setBorder(JfxUtils.createBorder(Color.RED))
    javaElemsVbox.setBorder(JfxUtils.createBorder(Color.RED))

    arrowsPane.prefWidthProperty() <== Expr(
      mainframeElemsVbox.widthProperty(),
      javaElemsVbox.widthProperty(),
      hboxInScrollPane.widthProperty()
    ) {
      hboxInScrollPane.getWidth() - mainframeElemsVbox.getWidth() - javaElemsVbox.getWidth()
    }

    mainframeElemsVbox.getChildren <== (model.mainframeRows, createNodeFromFileRow(_: FileRow, model.selectedMainframeRow.setValue(_)))
    javaElemsVbox.getChildren <== (model.javaRows, createNodeFromFileRow(_: FileRow, model.selectedJavaRow.setValue(_)))
    arrowsPane.getChildren <==(model.connections, createLineFromConnection)

    model.selectedMainframeRow ==> createSelectionListener(mainframeElemsVbox, detailedMfView, mfTimestampLabel, initialMfLabel, resultMfLabel)
    model.selectedJavaRow ==> createSelectionListener(javaElemsVbox, detailedJavaView, javaTimestampLabel, initialJavaLabel, resultJavaLabel)

    detailedMfView.visibleProperty() <== Expr(model.selectedMainframeRow) {
      model.selectedMainframeRow.get() != null
    }
    resultMfLabel.visibleProperty() <== detailedMfView.visibleProperty()
    detailedJavaView.visibleProperty() <== Expr(model.selectedJavaRow) {
      model.selectedJavaRow.get() != null
    }
    resultJavaLabel.visibleProperty() <== detailedJavaView.visibleProperty()

    mfSelectorController.source <== (model.possibleTransformations, (t: Transformation) => t.name)
    genTransSelectorController.source <== (model.possibleTransformations, (t: Transformation) => t.name)
    genTransSelectorController.target.addAll(model.genTransformations.toList.map(_.name))
    model.genTransformations <== (genTransSelectorController.target, (tn: String) => {
      model.possibleTransformations.toList.find(_.name == tn).get
    })
  }

  private def createLineFromConnection(connection: Connection) = {
    val mfLabel = mainframeElemsVbox.getChildren.toList.find(_.asInstanceOf[HasAssignedRow].getRow == connection.mainframeRow).get
    val javaLabel = javaElemsVbox.getChildren.toList.find(_.asInstanceOf[HasAssignedRow].getRow == connection.javaRow).get
    val res = new Line()
    res.setStartX(0);
    res.startYProperty <== Expr(mfLabel.layoutYProperty(), mfLabel.layoutBoundsProperty()) {
      mfLabel.getLayoutY() + mfLabel.getLayoutBounds.getHeight / 2
    }
    res.endXProperty() <== Expr(arrowsPane.widthProperty()) {
      arrowsPane.getWidth()
    }
    res.endYProperty() <== Expr(javaLabel.layoutYProperty(), javaLabel.layoutBoundsProperty()) {
      javaLabel.getLayoutY() + javaLabel.getLayoutBounds.getHeight / 2
    }
    res.setStroke(Color.RED)
    res
  }

  private def createSelectionListener(elemsVbox: VBox, detailedView: ScrollPane,
                                      timestampLabel: Label, initialValueLabel: Label,
                                      resultValueLabel: Label) = {
    ChgListener[FileRow] {chg=>
      val selected = chg.newValue
      elemsVbox.getChildren.toList.map(_.asInstanceOf[HasAssignedRow with Selectable])
        .foreach{label =>
          if (selected == label.getRow) {
            label.select1
          } else {
            label.unselect
          }
        }
      updateSelection2()
      if (selected != null) {
        timestampLabel.setText(selected.timestamp.toString)
        initialValueLabel.setText(selected.xmlStr)
        resultValueLabel.textProperty() <== selected.transformedXml
        mfSelectorController.target.clear()
        mfSelectorController.target.addAll(selected.appliedTransformations.toList.map(_.name))
        selected.appliedTransformations <== (mfSelectorController.target, (tn: String) => {
          model.possibleTransformations.toList.find(_.name == tn).get
        })
      }
    }
  }

  private def updateSelection2(): Unit = {
    val selectedMfRow = model.selectedMainframeRow.get()
    val selectedJavaRow = model.selectedJavaRow.get()
    if (selectedMfRow != null && selectedJavaRow != null && selectedMfRow.accId == selectedJavaRow.accId) {
      (mainframeElemsVbox.getChildren.toList ::: javaElemsVbox.getChildren.toList)
        .map(_.asInstanceOf[HasAssignedRow with Selectable])
        .foreach{label =>
          if (label.getRow != selectedMfRow && label.getRow != selectedJavaRow &&
            label.getRow.accId == selectedJavaRow.accId) {
            label.select2
          }
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
