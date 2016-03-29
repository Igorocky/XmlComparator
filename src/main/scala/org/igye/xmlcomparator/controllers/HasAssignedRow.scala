package org.igye.xmlcomparator.controllers

import org.igye.xmlcomparator.models.FileRow

trait HasAssignedRow {
  def getRow: FileRow
}
