package org.igye.xmlcomparator.report

import java.util
import javax.xml.bind.annotation.{XmlElementWrapper, XmlElement}

class PerRowModifiers {
  @XmlElement
  var lineId: String = _
  @XmlElementWrapper(name = "modifiers")
  @XmlElement(name = "modifier")
  var modifiers: java.util.List[String] = new util.ArrayList()
}
