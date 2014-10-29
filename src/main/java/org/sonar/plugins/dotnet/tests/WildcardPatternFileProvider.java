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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.sonar.api.utils.WildcardPattern;

import java.io.File;
import java.util.List;
import java.util.Set;

public class WildcardPatternFileProvider {

  public static final String CURRENT_FOLDER = ".";
  public static final String PARENT_FOLDER = "..";

  public static final String RECURSIVE_PATTERN = "**";
  public static final String ZERO_OR_MORE_PATTERN = "*";
  public static final String ANY_PATTERN = "?";

  private final File baseDir;
  private final String directorySeparator;

  public WildcardPatternFileProvider(File baseDir, String directorySeparator) {
    this.baseDir = baseDir;
    this.directorySeparator = directorySeparator;
  }

  public Set<File> listFiles(String pattern) {
    List<String> elements = ImmutableList.copyOf(Splitter.on(directorySeparator).split(pattern));

    int lastElementWithoutWildcard = 0;
    for (String element : elements) {
      if (containsWildcard(element)) {
        break;
      }
      lastElementWithoutWildcard++;
    }

    String pathTillFirstWildcardElement = Joiner.on(directorySeparator).join(elements.subList(0, lastElementWithoutWildcard));
    File fileTillFirstWildcardElement = new File(pathTillFirstWildcardElement);

    File startDir = fileTillFirstWildcardElement.isAbsolute() ? fileTillFirstWildcardElement : new File(baseDir, pathTillFirstWildcardElement);

    List<String> wildcardElements = elements.subList(lastElementWithoutWildcard, elements.size());
    if (wildcardElements.isEmpty()) {
      return startDir.exists() ? ImmutableSet.of(startDir) : ImmutableSet.<File>of();
    }

    for (String wildcardElement : wildcardElements) {
      if (isCurrentOrParentFolder(wildcardElement)) {
        throw new IllegalArgumentException("Cannot contain '" + CURRENT_FOLDER + "' or '" + PARENT_FOLDER + "' after the first wildcard.");
      }
    }

    WildcardPattern wildcardPattern = WildcardPattern.create(Joiner.on(directorySeparator).join(wildcardElements), directorySeparator);

    ImmutableSet.Builder<File> builder = ImmutableSet.builder();
    for (File file : listFiles(startDir)) {
      String relativePath = relativize(startDir, file);

      if (wildcardPattern.match(relativePath)) {
        builder.add(file);
      }
    }

    return builder.build();
  }

  private static boolean containsWildcard(String element) {
    return RECURSIVE_PATTERN.equals(element) ||
      element.contains(ZERO_OR_MORE_PATTERN) ||
      element.contains(ANY_PATTERN);
  }

  private static boolean isCurrentOrParentFolder(String element) {
    return CURRENT_FOLDER.equals(element) ||
      PARENT_FOLDER.equals(element);
  }

  private static Set<File> listFiles(File dir) {
    ImmutableSet.Builder<File> builder = ImmutableSet.builder();
    listFiles(builder, dir);
    return builder.build();
  }

  private static void listFiles(ImmutableSet.Builder<File> builder, File dir) {
    File[] files = dir.listFiles();
    if (files != null) {
      builder.add(files);

      for (File file : files) {
        if (file.isDirectory()) {
          listFiles(builder, file);
        }
      }
    }
  }

  private static String relativize(File parent, File file) {
    return file.getAbsolutePath().substring(parent.getAbsolutePath().length() + 1);
  }

}
