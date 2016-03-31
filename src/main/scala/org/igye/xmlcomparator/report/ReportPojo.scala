package org.igye.xmlcomparator.report

import java.util
import javax.xml.bind.annotation.{XmlElementWrapper, XmlValue, XmlElement, XmlRootElement}

@XmlRootElement
class ReportPojo {
  @XmlElement
  var lastModifiedOn: String = _

  @XmlElement
  var mainframeFile: String = _

  @XmlElement
  var javaFile: String = _

  @XmlElement
  var jarFile: String = _

  @XmlElementWrapper(name = "generalModifiers")
  @XmlElement(name = "generalModifier")
  var generalModifiers: java.util.List[String] = new util.ArrayList()

  @XmlElementWrapper(name = "rowModifiersList")
  @XmlElement(name = "rowModifiers")
  var rowModifiersList: java.util.List[PerRowModifiers] = new util.ArrayList()

  @XmlElementWrapper(name = "connections")
  @XmlElement(name = "connection")
  var connections: java.util.List[ConnectionPojo] = new util.ArrayList()

}
