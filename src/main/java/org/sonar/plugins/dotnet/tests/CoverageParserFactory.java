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

import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.SonarException;

import java.io.File;

public class CoverageParserFactory implements BatchExtension {

  private final CoverageConfiguration coverageConf;
  private final Settings settings;

  public CoverageParserFactory(CoverageConfiguration coverageConf, Settings settings) {
    this.coverageConf = coverageConf;
    this.settings = settings;
  }

  public boolean hasCoverageProperty() {
    return settings.hasKey(coverageConf.ncover3PropertyKey()) ||
      settings.hasKey(coverageConf.openCoverPropertyKey());
  }

  public CoverageParser coverageProvider() {
    CoverageParser coverageProvider;

    if (settings.hasKey(coverageConf.ncover3PropertyKey()) && !settings.hasKey(coverageConf.openCoverPropertyKey())) {
      coverageProvider = new NCover3ReportParser(new File(settings.getString(coverageConf.ncover3PropertyKey())));
    } else if (settings.hasKey(coverageConf.openCoverPropertyKey()) && !settings.hasKey(coverageConf.ncover3PropertyKey())) {
      coverageProvider = new OpenCoverReportParser(new File(settings.getString(coverageConf.openCoverPropertyKey())));
    } else {
      // In case both are not set, this method is not supposed to be called
      throw new SonarException("The properties \"" + coverageConf.ncover3PropertyKey() +
        "\" and \"" + coverageConf.openCoverPropertyKey() +
        "\" are mutually exclusive, specify either none or just one of them, but not both.");
    }

    return coverageProvider;
  }

}
