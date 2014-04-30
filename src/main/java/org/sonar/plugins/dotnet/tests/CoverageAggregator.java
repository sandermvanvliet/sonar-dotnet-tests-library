/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.dotnet.tests;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;

import java.io.File;

public class CoverageAggregator implements BatchExtension {

  private final CoverageConfiguration coverageConf;
  private final Settings settings;
  private final NCover3ReportParser ncover3ReportParser;
  private final OpenCoverReportParser openCoverReportParser;

  public CoverageAggregator(CoverageConfiguration coverageConf, Settings settings) {
    this(coverageConf, settings, new NCover3ReportParser(), new OpenCoverReportParser());
  }

  @VisibleForTesting
  public CoverageAggregator(CoverageConfiguration coverageConf, Settings settings, NCover3ReportParser ncover3ReportParser, OpenCoverReportParser openCoverReportParser) {
    this.coverageConf = coverageConf;
    this.settings = settings;
    this.ncover3ReportParser = ncover3ReportParser;
    this.openCoverReportParser = openCoverReportParser;
  }

  public boolean hasCoverageProperty() {
    return hasNCover3ReportPaths() || hasOpenCoverReportPaths();
  }

  private boolean hasNCover3ReportPaths() {
    return settings.hasKey(coverageConf.ncover3PropertyKey());
  }

  private boolean hasOpenCoverReportPaths() {
    return settings.hasKey(coverageConf.openCoverPropertyKey());
  }

  public Coverage aggregate(Coverage coverage) {
    if (hasNCover3ReportPaths()) {
      aggregate(settings.getString(coverageConf.ncover3PropertyKey()), ncover3ReportParser, coverage);
    }

    if (hasOpenCoverReportPaths()) {
      aggregate(settings.getString(coverageConf.openCoverPropertyKey()), openCoverReportParser, coverage);
    }

    return coverage;
  }

  private static void aggregate(String reportPaths, CoverageParser parser, Coverage coverage) {
    for (String reportPath : Splitter.on(',').trimResults().omitEmptyStrings().split(reportPaths)) {
      parser.parse(new File(reportPath), coverage);
    }
  }

}
