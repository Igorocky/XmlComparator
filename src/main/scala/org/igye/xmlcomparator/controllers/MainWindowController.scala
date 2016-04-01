package org.igye.xmlcomparator.controllers

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{HBox, Pane, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.shape.Line

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.{listToListOperators, nodeToNodeOps, observableValueToObservableValueOperators, propertyToPropertyOperators}
import org.igye.jfxutils.action.Action
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.{FxmlSupport, Initable}
import org.igye.jfxutils.properties.{ChgListener, Expr}
import org.igye.jfxutils.{JfxUtils, Window}
import org.igye.xmlcomparator.models.{Connection, FileRow, MainModel, Transformation}

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
  protected var resultFld: TextField = _
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
  protected var saveBtn: Button = _
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
  @FXML
  protected var javaSelector: HBox = _
  protected val javaSelectorController = FxmlSupport.load[SelectorController]
  @FXML
  protected var comparingVbox: VBox = _
  @FXML
  protected var comparingScrollPane: ScrollPane = _

  private val loadAction = new Action {
    override val description: String = "Load"
    override protected def onAction(): Unit = {
      model.genTransformations.unbind()
      genTransSelectorController.target.clear()

      model.load(mainframeFld, javaFld, jarFld, resultFld.getText)

      genTransSelectorController.target.addAll(model.genTransformations.toList.map(_.name))
      model.genTransformations <== (genTransSelectorController.target, (tn: String) => {
        model.possibleTransformations.toList.find(_.name == tn).get
      })
    }
  }

  private val saveAction = new Action {
    override val description: String = "Save"
    override protected def onAction(): Unit = {
      model.save(mainframeFld, javaFld, jarFld, resultFld.getText)
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
    ,saveAction
    ,connectAction
    ,disconnectAction
  )

  override def init(): Unit = {
    require(rootNode != null)
    require(mainframeFld != null)
    require(javaFld != null)
    require(jarFld != null)
    require(resultFld != null)
    require(scrollPane != null)
    require(hboxInScrollPane != null)
    require(mainframeElemsVbox != null)
    require(javaElemsVbox != null)
    require(arrowsPane != null)
    require(detailedMfView != null)
    require(detailedJavaView != null)
    require(loadBtn != null)
    require(saveBtn != null)
    require(connectBtn != null)
    require(disconnectBtn != null)
    require(mfTimestampLabel != null)
    require(initialMfLabel != null)
    require(resultMfLabel != null)
    require(javaTimestampLabel != null)
    require(initialJavaLabel != null)
    require(resultJavaLabel != null)
    require(mfSelector != null)
    require(javaSelector != null)
    require(genTransVbox != null)
    require(comparingVbox != null)
    require(comparingScrollPane != null)

    initWindow(rootNode)
    bindModel()

    Action.bind(loadAction, loadBtn)
    Action.bind(saveAction, saveBtn)
    Action.bind(connectAction, connectBtn)
    Action.bind(disconnectAction, disconnectBtn)
    JfxUtils.bindActionsToSceneProp(rootNode.sceneProperty(), actions)
  }

  private def bindModel(): Unit = {
    mfSelector.getChildren.add(mfSelectorController.rootPane)
    javaSelector.getChildren.add(javaSelectorController.rootPane)
    genTransVbox.getChildren.add(genTransSelectorController.rootPane)
    hboxInScrollPane.setBorder(JfxUtils.createBorder(Color.BLUE))
    hboxInScrollPane.prefWidthProperty() <== scrollPane.widthProperty()

    mainframeElemsVbox.setBorder(JfxUtils.createBorder(Color.GRAY))
    javaElemsVbox.setBorder(JfxUtils.createBorder(Color.GRAY))

    arrowsPane.prefWidthProperty() <== Expr(
      mainframeElemsVbox.widthProperty(),
      javaElemsVbox.widthProperty(),
      hboxInScrollPane.widthProperty()
    ) {
      hboxInScrollPane.getWidth() - mainframeElemsVbox.getWidth() - javaElemsVbox.getWidth()
    }

    mainframeElemsVbox.getChildren <== (model.mainframeRows, createNodeFromFileRow(
      _: FileRow,
      model.selectedMainframeRow.setValue(_),
      selectedMfRow => {
        model.selectedMainframeRow.setValue(selectedMfRow)
        model.connections.find(_.mainframeRow == selectedMfRow).foreach(con => model.selectedJavaRow.setValue(con.javaRow))
      }
    ))
    javaElemsVbox.getChildren <== (model.javaRows, createNodeFromFileRow(
      _: FileRow,
      model.selectedJavaRow.setValue(_),
      selectedJavaRow => {
        model.selectedJavaRow.setValue(selectedJavaRow)
        model.connections.find(_.javaRow == selectedJavaRow).foreach(con => model.selectedMainframeRow.setValue(con.mainframeRow))
      }
    ))
    arrowsPane.getChildren <==(model.connections, createLineFromConnection)

    model.selectedMainframeRow ==> createSelectionListener(mainframeElemsVbox, detailedMfView, mfTimestampLabel, initialMfLabel, resultMfLabel, mfSelectorController)
    model.selectedJavaRow ==> createSelectionListener(javaElemsVbox, detailedJavaView, javaTimestampLabel, initialJavaLabel, resultJavaLabel, javaSelectorController)

    detailedMfView.visibleProperty() <== Expr(model.selectedMainframeRow) {
      model.selectedMainframeRow.get() != null
    }
    resultMfLabel.visibleProperty() <== detailedMfView.visibleProperty()
    detailedJavaView.visibleProperty() <== Expr(model.selectedJavaRow) {
      model.selectedJavaRow.get() != null
    }
    resultJavaLabel.visibleProperty() <== detailedJavaView.visibleProperty()

    mfSelectorController.source <== (model.possibleTransformations, (t: Transformation) => t.name)
    javaSelectorController.source <== (model.possibleTransformations, (t: Transformation) => t.name)
    genTransSelectorController.source <== (model.possibleTransformations, (t: Transformation) => t.name)

    comparingVbox.backgroundProperty() <== Expr(resultMfLabel.textProperty(), resultJavaLabel.textProperty()) {
      if (resultMfLabel.getText == resultJavaLabel.getText) {
        JfxUtils.createBackground(Color.GREEN)
      } else {
        JfxUtils.createBackground(Color.RED)
      }
    }

    comparingScrollPane.hnd(MouseEvent.MOUSE_CLICKED){event=>
      if (event.getClickCount == 2 && (model.selectedMainframeRow.get != null || model.selectedJavaRow.get != null)) {
        val mfText = if (model.selectedMainframeRow.get != null) {
          model.selectedMainframeRow.get.id + " " + model.selectedMainframeRow.get.transformedXml.get()
        } else {
          ""
        }
        val javaText = if (model.selectedJavaRow.get != null) {
          model.selectedJavaRow.get.id + " " + model.selectedJavaRow.get.transformedXml.get()
        } else {
          ""
        }
        val selection = new StringSelection(mfText + "\n" + javaText)
        val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
        clipboard.setContents(selection, selection)
      }
    }
  }

  private def createLineFromConnection(connection: Connection) = {
    val mfLabel = mainframeElemsVbox.getChildren.toList.find(_.asInstanceOf[HasAssignedRow].getRow == connection.mainframeRow).get
    val javaLabel = javaElemsVbox.getChildren.toList.find(_.asInstanceOf[HasAssignedRow].getRow == connection.javaRow).get
    val line = new Line()
    line.setStartX(0);
    line.startYProperty <== Expr(mfLabel.layoutYProperty(), mfLabel.layoutBoundsProperty()) {
      mfLabel.getLayoutY() + mfLabel.getLayoutBounds.getHeight / 2
    }
    line.endXProperty() <== Expr(arrowsPane.widthProperty()) {
      arrowsPane.getWidth()
    }
    line.endYProperty() <== Expr(javaLabel.layoutYProperty(), javaLabel.layoutBoundsProperty()) {
      javaLabel.getLayoutY() + javaLabel.getLayoutBounds.getHeight / 2
    }
    line.strokeProperty <== Expr(connection.mainframeRow.transformedXml, connection.javaRow.transformedXml) {
      if (connection.mainframeRow.transformedXml.getValue == connection.javaRow.transformedXml.getValue) {
        Color.GREEN
      } else {
        Color.RED
      }
    }
    line
  }

  private def createSelectionListener(elemsVbox: VBox, detailedView: ScrollPane,
                                      timestampLabel: Label, initialValueLabel: Label,
                                      resultValueLabel: Label, selectorController: SelectorController) = {
    ChgListener[FileRow] {chg=>
      if (chg.oldValue != null) {
        chg.oldValue.appliedTransformations.unbind()
      }
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
        timestampLabel.setText(selected.id + " " + selected.timestamp.toString)
        initialValueLabel.setText(selected.xmlStr)
        resultValueLabel.textProperty() <== selected.transformedXml
        selectorController.target.clear()
        selectorController.target.addAll(selected.appliedTransformations.toList.map(_.name))
        selected.appliedTransformations <== (selectorController.target, (tn: String) => {
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

  private def createNodeFromFileRow(fileRow: FileRow, mouseClickedHnd: FileRow => Unit, mouseDblClickedHnd: FileRow => Unit) = {
    val res = new Label(fileRow.id) with HasAssignedRow with Selectable {
      private val initialBackground = getBackground

      override def getRow: FileRow = fileRow

      override def select1: Unit = setBackground(JfxUtils.createBackground(Color.BLUE))
      override def select2: Unit = setBackground(JfxUtils.createBackground(Color.GREEN))

      override def unselect: Unit = setBackground(initialBackground)
    }
    res.hnd(MouseEvent.MOUSE_CLICKED){ event =>
      if (event.getClickCount == 1) {
        mouseClickedHnd(fileRow)
      } else if (event.getClickCount == 2) {
        mouseDblClickedHnd(fileRow)
      }
    }
    res
  }
}
