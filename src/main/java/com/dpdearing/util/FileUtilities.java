package com.dpdearing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * File utilities helper methods
 */
public class FileUtilities {

   /**
    * Private constructor to prevent instantiation.
    */
   private FileUtilities() { // this class is not to be instantiated
   }

   /**
    * Loads a file, returning the contents as a Collection of lines.
    * 
    * @param file
    *           The file to load.
    * @param cs
    *           A charset.
    * @return The lines in the specified file, as a Set.
    * @throws {@link IOException}
    *            If the file can't be opened or read.
    * @throws {@link java.io.FileNotFoundException}
    *            If the file can't be found.
    * @throws {@link NullPointerException}
    *            If file is null.
    */
   public static List<String> loadLines(final File file, final Charset cs)
         throws IOException {
      return loadLines(file, new LinkedList<String>(), cs);
   }

   /**
    * Loads a file, storing the contents in the supplied collection,
    * line-by-line. This allows the calling code to perform sorting or
    * deduplication as desired at the time the files are loaded by providing a
    * Set or Sorted Collection, depending on the calling code's needs.
    * 
    * @param file
    *           The file to load.
    * @param lines
    *           The collection to store files in.
    * @param cs
    *           A charset.
    * @return The lines in the specified file, as a Collection.
    * @throws {@link IOException}
    *            If the file can't be opened or read.
    * @throws {@link java.io.FileNotFoundException}
    *            If the file can't be found.
    * @throws {@link NullPointerException}
    *            If any argument is null.
    */
   public static <T extends Collection<String>> T loadLines(final File file,
         final T lines, final Charset cs) throws IOException {
      checkNotNull(file, "file");
      checkNotNull(lines, "lines");
      checkNotNull(cs, "cs");
      
      BufferedReader bread = null;
      try {
         final InputStreamReader isReader =
            new InputStreamReader(new FileInputStream(file), cs);
         bread = new BufferedReader(isReader);

         // read the file into the words list, one word per line:
         String line = null;
         while (null != (line = bread.readLine())) {
            lines.add(line);
         }
      } finally {
         if (null != bread) {
            bread.close();
         }
      }

      return lines;
   }

   private static <T> T checkNotNull(final T reference, final String fieldName) {
      if (reference == null) {
         throw new IllegalArgumentException(fieldName);
      }
      return reference;
   }

}
