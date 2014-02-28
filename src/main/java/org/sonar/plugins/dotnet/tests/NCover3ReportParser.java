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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class NCover3ReportParser implements CoverageParser {

  private static final Logger LOG = LoggerFactory.getLogger(NCover3ReportParser.class);

  private final File file;

  public NCover3ReportParser(File file) {
    this.file = file;
  }

  @Override
  public Coverage parse() {
    return new Parser(file).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final Map<String, String> documents = Maps.newHashMap();
    private final Coverage coverage = new Coverage();

    public Parser(File file) {
      this.file = file;
    }

    public Coverage parse() {
      LOG.info("Parsing the NCover3 report " + file.getAbsolutePath());

      InputStreamReader reader = null;
      XMLStreamReader stream = null;

      try {
        reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        stream = xmlFactory.createXMLStreamReader(reader);
        xmlParserHelper = new XmlParserHelper(file, stream);

        checkRootTag();

        while (stream.hasNext()) {
          if (stream.next() == XMLStreamConstants.START_ELEMENT) {
            String tagName = stream.getLocalName();

            if ("doc".equals(tagName)) {
              handleDocTag();
            } else if ("seqpnt".equals(tagName)) {
              handleSegmentPointTag();
            }
          }
        }
      } catch (IOException e) {
        throw Throwables.propagate(e);
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      } finally {
        XmlParserHelper.closeXmlStream(stream);
        Closeables.closeQuietly(reader);
      }

      return coverage;
    }

    private void handleDocTag() throws XMLStreamException {
      String id = xmlParserHelper.getRequiredAttribute("id");
      String url = xmlParserHelper.getRequiredAttribute("url");

      if (!isExcludedId(id)) {
        documents.put(id, url);
      }
    }

    private static boolean isExcludedId(String id) {
      return "0".equals(id);
    }

    private void handleSegmentPointTag() throws XMLStreamException {
      String doc = xmlParserHelper.getRequiredAttribute("doc");
      int line = xmlParserHelper.getRequiredIntAttribute("l");
      int vc = xmlParserHelper.getRequiredIntAttribute("vc");

      if (documents.containsKey(doc) && !isExcludedLine(line)) {
        coverage.addHits(documents.get(doc), line, vc);
      }
    }

    private static boolean isExcludedLine(Integer line) {
      return 0 == line;
    }

    private void checkRootTag() throws XMLStreamException {
      xmlParserHelper.checkRootTag("coverage");
      xmlParserHelper.checkRequiredAttribute("exportversion", 3);
    }

  }

}
