/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.dotnet.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NUnitTestResultsFileParser implements UnitTestResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(NUnitTestResultsFileParser.class);

  @Override
  public void parse(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the NUnit Test Results file " + file.getAbsolutePath());
    new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final UnitTestResults unitTestResults;

    public Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    public void parse() {
      try {
        xmlParserHelper = new XmlParserHelper(file);
        checkRootTag();
        handleTestResultsTag();
        dispatchTags();
      } finally {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }
      }
    }

    private void dispatchTags() {
      String tagName;
      while ((tagName = xmlParserHelper.nextTag()) != null) {
        if ("test-suite".equals(tagName)) {
          handleTestSuiteTag();
        }
      }
    }

    private void checkRootTag() {
      xmlParserHelper.checkRootTag("test-results");
    }

    private void handleTestResultsTag() {
      int total = xmlParserHelper.getRequiredIntAttribute("total");
      int errors = xmlParserHelper.getRequiredIntAttribute("errors");
      int failures = xmlParserHelper.getRequiredIntAttribute("failures");
      int inconclusive = xmlParserHelper.getRequiredIntAttribute("inconclusive");
      int ignored = xmlParserHelper.getRequiredIntAttribute("ignored");

      int tests = total - inconclusive;
      int passed = total - errors - failures - inconclusive;
      int skipped = inconclusive + ignored;

      unitTestResults.add(tests, passed, skipped, failures, errors, 0);
    }

    private void handleTestSuiteTag() {
      String timeAttribute = xmlParserHelper.getAttribute("time");

      long executionTime = 0;

      try {
         NumberFormat usFormat = NumberFormat.getInstance(Locale.US);
         Number number = usFormat.parse(timeAttribute);
         double time = number.doubleValue();
         executionTime = (long)(time * 1000);
      } catch(ParseException px) {
        // Use the default value when we can't parse the input
      }

      unitTestResults.add(0, 0, 0, 0, 0, executionTime);
    }
  }

}
