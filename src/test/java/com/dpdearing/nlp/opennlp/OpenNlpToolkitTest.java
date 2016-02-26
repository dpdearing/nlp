package com.dpdearing.nlp.opennlp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

import org.junit.Test;

/**
 * Test fixture for {@link OpenNlpToolkit}
 */
public class OpenNlpToolkitTest {

   /**
    * Test method for both sentence detection and tokenization
    */
   @Test
   public void testSentenceDetectionAndTokenization() throws IOException {
      final OpenNlpToolkit toolkit = new OpenNlpToolkit();
      
      // Example taken from:
      // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Sentence_Detector
      // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Tokenizer
      final String content =
            "Pierre Vinken, 61 years old, will join the board as a nonexecutive"
            + " director Nov. 29. Mr. Vinken is chairman of Elsevier N.V., the"
            + " Dutch publishing group. Rudolph Agnew, 55 years old and former"
            + " chairman of Consolidated Gold Fields PLC, was named a director"
            + " of this British industrial conglomerate."
            // added this for more boundary cases
            + " Those contraction-less sentences don't have boundary/odd"
            + " cases...this one does.";
      
      final String[][] expected = new String[][] {
            new String[] {"Pierre", "Vinken", ",", "61", "years", "old", ",",
                  "will", "join", "the", "board", "as", "a", "nonexecutive",
                  "director", "Nov.", "29", "."},
            new String[] {"Mr.", "Vinken", "is", "chairman", "of", "Elsevier",
                  "N.V.", ",", "the", "Dutch", "publishing", "group", "."},
            new String[] {"Rudolph", "Agnew", ",", "55", "years", "old", "and",
                  "former", "chairman", "of", "Consolidated", "Gold", "Fields",
                  "PLC", ",", "was", "named", "a", "director", "of", "this",
                  "British", "industrial", "conglomerate", "."},
            new String[] {"Those", "contraction-less", "sentences", "do",
                  "n't", "have", "boundary/odd", "cases", "...this", "one",
                  "does", "."}
      };
  
      final String[] sentences = toolkit.detectSentences(content);
      assertEquals("Incorrect number of sentences detected.",
            expected.length, sentences.length);

      for (int i=0; i < sentences.length; i++) {

         final String[] tokens = toolkit.tokenize(sentences[i]);
         
         assertEquals(
               "Incorrect number of tokens detected for sentence at index " + i,
               expected[i].length, tokens.length);
         // compare each token against expectations
         for (int j=0; j < expected[i].length; j++) {
            assertEquals(
                  "Unexpected token at sentence index " + i
                  + ", token index " + j,
                  expected[i][j], tokens[j]);
         }
      }
   }

   /**
    * Test method for {@link OpenNlpToolkit#detectSentences(String)}.
    */
   @Test
   public void testDetectSentences() throws IOException {
      final OpenNlpToolkit toolkit = new OpenNlpToolkit();
      
      // Example taken from:
      // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Sentence_Detector
      final String content =
            "Pierre Vinken, 61 years old, will join the board as a nonexecutive"
            + " director Nov. 29. Mr. Vinken is chairman of Elsevier N.V., the"
            + " Dutch publishing group. Rudolph Agnew, 55 years old and former"
            + " chairman of Consolidated Gold Fields PLC, was named a director"
            + " of this British industrial conglomerate."
            // added this for more boundary cases
            + " Those contraction-less sentences don't have boundary/odd"
            + " cases...this one does.";
      
      final String[] expected = new String[] {
            "Pierre Vinken, 61 years old, will join the board as a nonexecutive"
               + " director Nov. 29.",
            "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing"
               + " group.",
            "Rudolph Agnew, 55 years old and former chairman of Consolidated"
               + " Gold Fields PLC, was named a director of this British"
               + " industrial conglomerate.",
            "Those contraction-less sentences don't have boundary/odd"
               + " cases...this one does."
      };
      
      final String[] sentences = toolkit.detectSentences(content);
      // compare each sentence against expectations
      assertEquals("Incorrect number of sentences detected.",
            expected.length, sentences.length);
      for (int i=0; i < expected.length; i++) {
         assertEquals("Unexpected sentence content",
               expected[i], sentences[i]);
      }
   }

   /**
    * Test method for {@link OpenNlpToolkit#detectSentences(File, Charset)}.
    */
   @Test
   public void testDetectSentencesWithEndOfLineBoundaries() throws IOException {
      final OpenNlpToolkit toolkit = new OpenNlpToolkit();

      final String[] expected = new String[] {
            // expect to add period at end-of-line boundaries
            "OpenNLP doesn't naturally treat end-of-lines as sentence boundaries.",
            "OpenNLP is poorly documented."
      };
      
      // reading content from file
      final File testFile = new File(
            getClass().getResource("/text_with_title.txt").getFile());
      final String[] sentences = toolkit.detectSentences(
            testFile, Charset.forName("UTF-8"));
      
      // compare each sentence against expectations
      assertEquals("Incorrect number of sentences detected.",
            expected.length, sentences.length);
      for (int i=0; i < expected.length; i++) {
         assertEquals("Unexpected sentence content",
               expected[i], sentences[i]);
      }
   }

   /**
    * Test method for {@link OpenNlpToolkit#tokenize(String)}.
    */
   @Test
   public void testTokenize() throws IOException {
      final OpenNlpToolkit toolkit = new OpenNlpToolkit();

      // Example taken (and corrected) from:
      // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Tokenizer
      final String[] sentences = new String[] {
            "Pierre Vinken, 61 years old, will join the board as a nonexecutive"
            + " director Nov. 29.",
         "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing"
            + " group.",
         "Rudolph Agnew, 55 years old and former chairman of Consolidated"
            + " Gold Fields PLC, was named a director of this British"
            + " industrial conglomerate."
      };
      
      final String[][] expected = new String[][] {
            new String[] {"Pierre", "Vinken", ",", "61", "years", "old", ",",
                  "will", "join", "the", "board", "as", "a", "nonexecutive",
                  "director", "Nov.", "29", "."},
            new String[] {"Mr.", "Vinken", "is", "chairman", "of", "Elsevier",
                  "N.V.", ",", "the", "Dutch", "publishing", "group", "."},
            new String[] {"Rudolph", "Agnew", ",", "55", "years", "old", "and",
                  "former", "chairman", "of", "Consolidated", "Gold", "Fields",
                  "PLC", ",", "was", "named", "a", "director", "of", "this",
                  "British", "industrial", "conglomerate", "."}
      };
  
      for (int i=0; i < sentences.length; i++) {
         final String[] tokens = toolkit.tokenize(sentences[i]);
         
         assertEquals(
               "Incorrect number of tokens detected for sentence at index " + i,
               expected[i].length, tokens.length);
         // compare each token against expectations
         for (int j=0; j < expected[i].length; j++) {
            assertEquals(
                  "Unexpected token at sentence index " + i
                  + ", token index " + j,
                  expected[i][j], tokens[j]);
         }
      }
   }

   /**
    * Test method for {@link OpenNlpToolkit#tagPartOfSpeech(String[])}.
    */
   @Test
   public void testPartOfSpeechTagger() throws IOException {
      final OpenNlpToolkit toolkit = new OpenNlpToolkit();

      // Example taken (and corrected) from:
      // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=POS_Tagger
      final String[][] tokens = new String[][] {
            new String[] {"Pierre", "Vinken", ",", "61", "years", "old", ",",
                  "will", "join", "the", "board", "as", "a", "nonexecutive",
                  "director", "Nov.", "29", "."},
            new String[] {"Mr.", "Vinken", "is", "chairman", "of", "Elsevier",
                  "N.V.", ",", "the", "Dutch", "publishing", "group", "."},
      };
      
      final String[][] expected = new String[][] {
            new String[] {"NNP", "NNP", ",", "CD", "NNS", "JJ", ",", "MD",
                  "VB", "DT", "NN", "IN", "DT", "JJ", "NN", "NNP", "CD", "."},
            new String[] {"NNP", "NNP", "VBZ", "NN", "IN", "NNP", "NNP", ",",
                  "DT", "JJ", "NN", "NN", "."},
      };
  
      for (int i=0; i < tokens.length; i++) {
         final String[] tags = toolkit.tagPartOfSpeech(tokens[i]);
         
         assertEquals(
               "Incorrect number of tags detected for sentence at index " + i,
               expected[i].length, tags.length);
         // compare each tag against expectations
         for (int j=0; j < expected[i].length; j++) {
            assertEquals(
                  "Unexpected tag at sentence index " + i
                  + ", token index " + j,
                  expected[i][j], tags[j]);
         }
      }
   }
   
   /**
    * Test method for {@link OpenNlpToolkit#}.
    */
   @Test
   public void testParser() throws IOException {
      final OpenNlpToolkit toolkit = new OpenNlpToolkit();

      // Example taken (and corrected) from:
      // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Parser
      final String sentence = "The quick brown fox jumps over the lazy dog.";
      
      // Parse
      final Parse actual = toolkit.parseSentence(sentence);
      
      final StringBuffer buf = new StringBuffer();
      actual.show(buf);
      
      // expected
      //(TOP (NP (NP (DT The) (JJ quick) (JJ brown) (NN fox) (NNS jumps)) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))(. .)))
      assertEquals("The created parse tree does not match the expected string.",
            "(TOP " +
                  "(NP " +
                     "(NP (DT The) (JJ quick) (JJ brown) (NN fox) (NNS jumps)) " +
                     "(PP " +
                        "(IN over) " +
                        "(NP (DT the) (JJ lazy) (NN dog))" +
                     ")" +
                     "(. .)" +
                  ")" +
            ")",
            buf.toString());
   }

   
   /**
    * Test method for {@link OpenNlpToolkit#findNamedEntities(String, String[])}.
    */
   @Test
   public void testFindNamedEntities() throws IOException {
      final OpenNlpToolkit toolkit = new OpenNlpToolkit();

      // Example taken (and corrected) from:
      // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Name_Finder
      final String text = 
            "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29.  " +
      		"Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group.  " +
      		"Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a director of this British industrial conglomerate.";
      
      final String[] tokens = toolkit.tokenize(text);
      
      final List<Span> spans = toolkit.findNamedEntities(text, tokens);
      
      // expected
      final String[][] expected = {
            new String[] { "Pierre", "Vinken" },
            new String[] { "Rudolph", "Agnew" },
            new String[] { "Consolidated", "Gold", "Fields", "PLC" }
      };
      
      assertEquals("Unexpected number of spans", expected.length, spans.size());
      
      for (int i=0; i < spans.size(); i++) {
         final Span s = spans.get(i);
         int j = 0;
         for (int tok = s.getStart(); tok < s.getEnd(); tok++) {
            assertEquals("Unexpected Named Entity token found", expected[i][j], tokens[tok]);
            j++;
         }
      }
   }

   /**
    * Test method for {@link OpenNlpToolkit#findEntityMentions(String[])}.
    * <p>
    * <strong>NOTE:</strong> Requires Java VM param <code>-DWNSEARCHDIR=lib/wordnet/dict</code>
    * </p>
    */
   @Test
   public void testFindEntityMentions() throws IOException {
       final OpenNlpToolkit toolkit = new OpenNlpToolkit();

       // Example taken (and corrected) from:
       // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Name_Finder
       final String[] sentences = {
               "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29.",
               "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group.",
               "Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a director of this British industrial conglomerate."
       };

       final DiscourseEntity[] entities = toolkit.findEntityMentions(sentences);

       // expected
       final String[][] expected = {
               new String[]{"this British industrial conglomerate"},
               new String[]{"a nonexecutive director", "chairman", "former chairman", "a director"},
               new String[]{"Consolidated Gold Fields PLC"},
               new String[]{"55 years"},
               new String[]{"Rudolph Agnew"},
               new String[]{"Elsevier N.V.", "the Dutch publishing group"},
               new String[]{"Pierre Vinken", "Mr. Vinken"},
               new String[]{"Nov. 29"},
               new String[]{"the board"},
               new String[]{"61 years"}
       };

       assertEquals("Unexpected number of entities", expected.length, entities.length);

       for (int i = 0; i < entities.length; i++) {
           final DiscourseEntity ent = entities[i];

           assertEquals("Unexpected number of mentions at index " + i,
                   expected[i].length, ent.getNumMentions());
           final Iterator<MentionContext> mentions = ent.getMentions();
           int j = 0;
           while (mentions.hasNext()) {
               final MentionContext mc = mentions.next();
               System.out.println("[" + mc.toString() + "]");
               assertEquals("Unexpected Entity Mention found", expected[i][j], mc.toString().trim());
               j++;
           }
       }
   }
}
