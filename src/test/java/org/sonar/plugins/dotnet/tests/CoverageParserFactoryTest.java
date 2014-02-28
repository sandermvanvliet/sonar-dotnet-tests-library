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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.SonarException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoverageParserFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void hasCoverageProperty() {
    Settings settings = mock(Settings.class);

    CoverageConfiguration coverageConf = new CoverageConfiguration("", "ncover", "opencover");

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(false);
    assertThat(new CoverageParserFactory(coverageConf, settings).hasCoverageProperty()).isFalse();

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(true);
    assertThat(new CoverageParserFactory(coverageConf, settings).hasCoverageProperty()).isTrue();

    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.hasKey("opencover")).thenReturn(false);
    assertThat(new CoverageParserFactory(coverageConf, settings).hasCoverageProperty()).isTrue();

    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.hasKey("opencover")).thenReturn(true);
    assertThat(new CoverageParserFactory(coverageConf, settings).hasCoverageProperty()).isTrue();

    coverageConf = new CoverageConfiguration("", "ncover2", "opencover2");
    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.hasKey("opencover")).thenReturn(true);
    assertThat(new CoverageParserFactory(coverageConf, settings).hasCoverageProperty()).isFalse();
  }

  @Test
  public void coverageProvider() {
    CoverageConfiguration coverageConf = new CoverageConfiguration("", "ncover", "opencover");
    Settings settings = mock(Settings.class);

    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.getString("ncover")).thenReturn("");
    when(settings.hasKey("opencover")).thenReturn(false);
    assertThat(new CoverageParserFactory(coverageConf, settings).coverageProvider()).isInstanceOf(NCover3ReportParser.class);

    when(settings.hasKey("opencover")).thenReturn(true);
    when(settings.getString("opencover")).thenReturn("");
    when(settings.hasKey("ncover")).thenReturn(false);
    assertThat(new CoverageParserFactory(coverageConf, settings).coverageProvider()).isInstanceOf(OpenCoverReportParser.class);
  }

  @Test
  public void coverageProvider_should_fail_when_several_reports_are_provided() {
    thrown.expect(SonarException.class);
    thrown.expectMessage("The properties \"ncover\" and " +
      "\"opencover\" are mutually exclusive, specify either none or just one of them, but not both.");

    CoverageConfiguration coverageConf = new CoverageConfiguration("", "ncover", "opencover");
    Settings settings = mock(Settings.class);

    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.hasKey("opencover")).thenReturn(true);
    new CoverageParserFactory(coverageConf, settings).coverageProvider();
  }

}
