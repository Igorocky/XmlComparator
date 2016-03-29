package org.igye.xmlcomparator

import java.lang.Thread.UncaughtExceptionHandler
import javafx.application.Application
import javafx.stage.Stage

import org.apache.logging.log4j.LogManager
import org.igye.jfxutils.fxml.FxmlSupport
import org.igye.xmlcomparator.controllers.MainWindowController

object XmlComparatorMain {
  def main(args: Array[String]) {
    Application.launch(classOf[XmlComparatorApp], args: _*)
  }
}

class XmlComparatorApp extends Application {
  implicit val log = LogManager.getLogger()

  override def start(primaryStage: Stage): Unit = {
    val prevUncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler
    Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler {
      override def uncaughtException(t: Thread, e: Throwable): Unit = {
        log.error(e.getMessage, e)
        if (prevUncaughtExceptionHandler != null) {
          prevUncaughtExceptionHandler.uncaughtException(t, e)
        } else {
          throw e
        }
      }
    })
    val mainWindow = FxmlSupport.load[MainWindowController](primaryStage)
    mainWindow.stage.setTitle("XmlComparator")
    mainWindow.stage.setMaximized(true)
    mainWindow.open()
  }
}