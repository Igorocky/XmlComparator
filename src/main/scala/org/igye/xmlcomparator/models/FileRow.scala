package org.igye.xmlcomparator.models

import java.time.LocalDateTime

case class FileRow(id: String, timestamp: LocalDateTime, source: String, xmlStr: String)