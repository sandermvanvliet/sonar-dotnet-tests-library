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
import org.mockito.Mockito;
import org.sonar.api.config.Settings;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnitTestResultsAggregatorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void hasUnitTestResultsProperty() {
    Settings settings = mock(Settings.class);

    UnitTestConfiguration unitTestConf = new UnitTestConfiguration("visualStudioTestResultsFile");

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(false);
    assertThat(new UnitTestResultsAggregator(unitTestConf, settings).hasUnitTestResultsProperty()).isFalse();

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    assertThat(new UnitTestResultsAggregator(unitTestConf, settings).hasUnitTestResultsProperty()).isTrue();

    unitTestConf = new UnitTestConfiguration("visualStudioTestResultsFile2");
    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    assertThat(new UnitTestResultsAggregator(unitTestConf, settings).hasUnitTestResultsProperty()).isFalse();
  }

  @Test
  public void aggregate() {
    UnitTestConfiguration unitTestConf = new UnitTestConfiguration("visualStudioTestResultsFile");
    Settings settings = mock(Settings.class);

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    when(settings.getString("visualStudioTestResultsFile")).thenReturn("foo.trx");
    VisualStudioTestResultsFileParser visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    UnitTestResults results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser).parse(new File("foo.trx"), results);

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(false);
    when(settings.getString("visualStudioTestResultsFile")).thenReturn("foo.trx");
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(UnitTestResults.class));

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    when(settings.getString("visualStudioTestResultsFile")).thenReturn(",foo.trx  ,bar.trx");
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser).parse(new File("foo.trx"), results);
    verify(visualStudioTestResultsFileParser).parse(new File("bar.trx"), results);
  }

}
