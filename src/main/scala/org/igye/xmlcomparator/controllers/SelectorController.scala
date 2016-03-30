package org.igye.xmlcomparator.controllers

import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.layout.VBox

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.action.Action
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.Initable

@FxmlFile("fxml/Selector.fxml")
class SelectorController extends Initable {
  implicit val log: Logger = LogManager.getLogger()

  @FXML
  protected var rootPaneVar: VBox = _
  lazy val rootPane = rootPaneVar
  @FXML
  protected var choiceBox: ChoiceBox[String] = _
  @FXML
  protected var addBtn: Button = _
  @FXML
  protected var listView: ListView[String] = _
  @FXML
  protected var removeBtn: Button = _

  lazy val source: ObservableList[String] = choiceBox.getItems
  lazy val target: ObservableList[String] = listView.getItems

  private val addAction = new Action {
    override val description: String = "Add selected item"
    override protected def onAction(): Unit = {
      val selectedItem = choiceBox.getSelectionModel.getSelectedItem
      if (selectedItem.trim != "") {
        listView.getItems.add(selectedItem)
      }
    }
  }

  private val removeAction = new Action {
    override val description: String = "Remove selected item"
    override protected def onAction(): Unit = {
      listView.getItems.remove(listView.getSelectionModel.getSelectedItem)
    }
  }

  private val actions = List(
    addAction
    ,removeAction
  )

  override def init(): Unit = {
    require(rootPaneVar != null)
    require(choiceBox != null)
    require(addBtn != null)
    require(listView != null)
    require(removeBtn != null)

    Action.bind(addAction, addBtn)
    Action.bind(removeAction, removeBtn)
  }
}
