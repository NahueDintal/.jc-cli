package com.javacli.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectConfig {
  private String name;
  private String version;
  private String mainClass;
  private List<String> sourceDirectories;
  private List<String> testDirectories;
  private List<String> dependencies;
  private String testFramework;
  private int javaVersion = 25; // es el default
  private String outputDirectory = "out/production/default"; // valor por defecto

  public static ProjectConfig load(Path baseDir) throws IOException {
    Path configPath = baseDir.resolve("jc.json");
    if (!Files.exists(configPath)) {
      throw new IOException("No se encuentra jc.json en " + baseDir);
    }
    String content = Files.readString(configPath);
    ProjectConfig config = new ProjectConfig();

    config.name = extractString(content, "name");
    config.version = extractString(content, "version");
    config.mainClass = extractString(content, "mainClass");
    config.sourceDirectories = extractList(content, "sourceDirectories");
    config.testDirectories = extractList(content, "testDirectories");
    config.dependencies = extractList(content, "dependencies");
    config.testFramework = extractString(content, "testFramework");
    config.javaVersion = extractInt(content, "javaVersion", 25);
    config.outputDirectory = extractString(content, "outputDirectory");

    return config;
  }

  private static int extractInt(String json, String key, int defaultValue) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    return defaultValue;
  }

  private static String extractString(String json, String key) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "";
  }

  private static List<String> extractList(String json, String key) {
    List<String> list = new ArrayList<>();
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[(.*?)\\]");
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      String arrayContent = matcher.group(1).trim();
      if (!arrayContent.isEmpty()) {
        String[] items = arrayContent.split(",");
        for (String item : items) {
          item = item.trim();
          if (item.startsWith("\"") && item.endsWith("\"")) {
            item = item.substring(1, item.length() - 1);
          }
          if (!item.isEmpty()) {
            list.add(item);
          }
        }
      }
    }
    return list;
  }

  // Getters
  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getMainClass() {
    return mainClass;
  }

  public List<String> getSourceDirectories() {
    return sourceDirectories;
  }

  public List<String> getTestDirectories() {
    return testDirectories;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public String getTestFramework() {
    return testFramework;
  }

  public int getJavaVersion() {
    return javaVersion;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void generateClasspath(Path baseDir) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<classpath>\n");

    for (String src : sourceDirectories) {
      sb.append("    <classpathentry kind=\"src\" path=\"").append(src).append("\"/>\n");
    }
    for (String testSrc : testDirectories) {
      sb.append("    <classpathentry kind=\"src\" path=\"").append(testSrc).append("\"/>\n");
    }

    sb.append("    <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");

    for (String dep : dependencies) {
      sb.append("    <classpathentry kind=\"lib\" path=\"").append(dep).append("\"/>\n");
    }

    sb.append("    <classpathentry kind=\"output\" path=\"bin\"/>\n");
    sb.append("</classpath>\n");

    Files.writeString(baseDir.resolve(".classpath"), sb.toString());
  }
}
