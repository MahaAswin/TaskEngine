package com.taskengine.backend.util;

import java.util.Locale;

public final class Slugify {

  private Slugify() {}

  public static String baseSlug(String name) {
    if (name == null) {
      return "org";
    }
    String s =
        name.toLowerCase(Locale.ROOT)
            .trim()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    return s.isEmpty() ? "org" : s;
  }
}
