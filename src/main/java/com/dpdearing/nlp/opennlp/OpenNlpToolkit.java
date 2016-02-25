package com.dpdearing.nlp.opennlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dpdearing.util.FileUtilities;

public class OpenNlpToolkit {
   static private final Logger logger = LoggerFactory.getLogger(OpenNlpToolkit.class);

   /**
    * Open NLP properties
    */
   static final private String PROPERTIES_FILENAME = "opennlp.properties";
   static final private String COREF_DIR = "opennlp.coref.dir";
   
   /**
    * The Named Entity Types of interest. Each requires a corresponding Open NLP
    * model file.
    */
   private static final String[] NAME_TYPES =
         { "person", "organization", "location" };

   /**
    * Properties containing the Open NLP binary paths
    */
   private Properties _properties = null;
   
   /**
    * OpenNLP components, each is lazily initialized.  Don't directly access, but use:
    * <ul>
    * <li>{@link #detectSentences(String)}</li>
    * <li>{@link #tokenize(String)}</li>
    * <li>{@link #nameFinders()}</li>
    * <li>{@link #parse(Parse)}</li>
    * <li>{@link #linker()}</li>
    * </ul>
    */
   private SentenceDetector _sentenceDetector = null;
   private Tokenizer _tokenizer = null;
   private POSTagger _posTagger = null;
   final private Map<String, TokenNameFinder> _nameFinderMap =
         new HashMap<String, TokenNameFinder>();
   private Parser _parser = null;
   private Linker _linker = null;

   /**
    * Read the content from the specified file and return a list of detected
    * sentences.
    * 
    * @param file
    *           the file to read
    * @param cs
    *           the file charset
    * @return the detected sentences
    * 
    * @throws IOException
    *            If an error occurs while loading the file
    */
   public String[] detectSentences(final File file, final Charset cs)
     throws Exception {
      // reading individual lines instead of raw content because with news stories,
      // some sentence lines don't end in punctuation (especially headings, etc.)
      final List<String> lines = FileUtilities.loadLines(file, cs);
      final ArrayList<String> sentences = new ArrayList<String>();
      for (final String content : lines) {
         final String[] detected = detectSentences(content);
         for (int idx=0; idx < detected.length; idx++) {
            final String sentence = detected[idx].trim();
            // check for ending with punctuation
            if (sentence.matches(".*\\p{P}$")) {
               sentences.add(sentence);
            } else {
               logger.warn("Sentence #" + idx
                     + " does not end with punctuation: [" + sentence + "]");
               logger.warn("Appending a . (period)");
               sentences.add(sentence + ".");
            }
         }
      }
      return sentences.toArray(new String[0]);
   }
   
   /**
    * Break the given content into sentences.
    * <p>
    * The sentence detector is lazily initialized on first use.
    * </p>
    * 
    * @param content the content to break into sentences
    * @return the detected sentences
    */
   public String[] detectSentences(final String content) throws Exception {
      if (_sentenceDetector == null) {
         // lazy initialize
         InputStream modelIn = null;
         String modelFilename = getProperty("opennlp.sentence");
         try {
            // sentence detector
            logger.info("Loading sentence detection model: " + modelFilename);
            modelIn = getClass().getResourceAsStream(modelFilename);
            final SentenceModel sentenceModel = new SentenceModel(modelIn);
            modelIn.close();
            _sentenceDetector = new SentenceDetectorME(sentenceModel);
            logger.info("done.");
         } catch (NullPointerException npe) { // happens when modelFilename is null
            throw new Exception("No key/value pair found for the Sentence detection model in the 'opennlp.properties' file, the missing key/value pair is 'opennlp.sentence=...'");
         } catch (final Exception e) {
            throw new IOException("Error loading sentence detector, does: " + modelFilename + " exist in your 'resources' directory?");
         } finally {
            if (modelIn != null) {
               try {
                  modelIn.close();
               } catch (final IOException e) {}
            }
         }
      }
      
      // detect sentences
      return _sentenceDetector.sentDetect(content);
   }

   /**
    * Tokenize the given sentence.
    * <p>
    * The tokenizer is lazily initialized on first use.
    * </p>
    * 
    * @param sentence
    *           a sentence to tokenize
    * @return the individual tokens
    */
   public String[] tokenize(final String sentence) throws Exception {
      // tokenize
      return tokenizer().tokenize(sentence);
   }
   
   /**
    * @return the lazily-initialized tokenizer
    */
   private Tokenizer tokenizer() throws Exception {
      if (_tokenizer == null) {
         // lazy initialize
         InputStream modelIn = null;
         String modelFilename = getProperty("opennlp.tokenizer");
         try {
            // tokenizer
            logger.info("Loading tokenizer model: " + modelFilename);
            modelIn = getClass().getResourceAsStream(modelFilename);
            final TokenizerModel tokenModel = new TokenizerModel(modelIn);
            modelIn.close();
            _tokenizer = new TokenizerME(tokenModel);
            logger.info("done.");
         } catch (NullPointerException npe) { // happens when modelFilename == null
            throw new Exception("No key/value pair found for the tokenizer model in the 'opennlp.properties' file, the missing key/value pair is 'opennlp.tokenizer=...'");
         } catch (final Exception e) {
            throw new IOException("Error loading tokenizer, does: " + modelFilename + " exist in your 'resources' directory?");
         } finally {
            if (modelIn != null) {
               try {
                  modelIn.close();
               } catch (final IOException e) {}
            }
         }
      }
      return _tokenizer;
   }

   /**
    * Detect the part of speech tags for the given tokens in a sentence.
    * <p>
    * The tagger is lazily initialized on first use.
    * </p>
    * 
    * @param tokens
    *           an array of sentence tokens to tag
    * @return the individual part-of-speech tags
    */
   public String[] tagPartOfSpeech(final String[] tokens) throws Exception {
      if (_posTagger == null) {
         // lazy initialize
         InputStream modelIn = null;
         String modelFilename = getProperty("opennlp.pos");
         try {
            // tagger
            logger.info("Loading part-of-speech model: " + modelFilename);
            modelIn = getClass().getResourceAsStream(modelFilename);
            final POSModel posModel = new POSModel(modelIn);
            modelIn.close();
            _posTagger = new POSTaggerME(posModel);
            logger.info("done.");
         } catch (NullPointerException npe) { // happens when modelFilename == null
            throw new Exception("No key/value pair found for the tag part of speech model in the 'opennlp.properties' file, the missing key/value pair is 'opennlp.pos=...'");
         } catch (final Exception e) {
            throw new IOException("Error loading part-of-speech tagger, does: " + modelFilename + " exist in your 'resources' directory?");
         } finally {
            if (modelIn != null) {
               try {
                  modelIn.close();
               } catch (final IOException e) {}
            }
         }
      }
      return _posTagger.tag(tokens);
   }
   
   
   /**
    * Find named entities in a tokenized sentence.
    * <p>
    * Must call {@link #clearNamedEntityAdaptiveData()} after finding all named
    * entities in a single document.
    * </p>
    *
    *
    * @param sentence
    *           the sentence text
    * @param tokens
    *           the sentence tokens
    * @return a collection of named entity references
    */
   public List<Span> findNamedEntities(final String sentence, final String[] tokens) throws Exception {
      final List<Span> entities = new LinkedList<Span>();
      
      // use each type of finder to identify named entities 
      for (final TokenNameFinder finder : nameFinders()) {
         entities.addAll(Arrays.asList(finder.find(tokens)));
      }
      
      return entities;
   }
   
   /**
    * Must be called between documents or can negatively impact detection rate.
    */
   public void clearNamedEntityAdaptiveData() throws Exception {
      for (final TokenNameFinder finder : nameFinders()) {
         finder.clearAdaptiveData();
      }
   }

   /**
    * @return the lazily-initialized token name finders
    */
   private TokenNameFinder[] nameFinders() throws Exception {
      final TokenNameFinder[] finders = new TokenNameFinder[NAME_TYPES.length];
      // one for each name type
      for (int i = 0; i < NAME_TYPES.length; i++) {
         finders[i] = nameFinder(NAME_TYPES[i]);
      }
      return finders;
   }
   
   /**
    * @param type the name type recognizer to load
    * @return the lazily-initialized name token finder
    */
   private TokenNameFinder nameFinder(final String type) throws Exception {
      if (!_nameFinderMap.containsKey(type)) {
         final TokenNameFinder finder = createNameFinder(type);
         _nameFinderMap.put(type, finder);
      }
      return _nameFinderMap.get(type);
   }

   /**
    * @param type the name type recognizer to load
    * @return the lazily-initialized name token finder
    */
   private TokenNameFinder createNameFinder(final String type) throws Exception {
      InputStream modelIn = null;
      String modelFilename = getProperty("opennlp.namefinder.format");
      try {
         logger.info("Loading " + type + " named entity model");
         modelIn = getClass().getResourceAsStream(String.format(modelFilename, type));
         final TokenNameFinderModel nameFinderModel = new TokenNameFinderModel(modelIn);
         modelIn.close();
         return new NameFinderME(nameFinderModel);
      } catch (NullPointerException npe) { // happens when modelFilename == null
         throw new Exception("No key/value pair found for the " + type + " named entity model in the 'opennlp.properties' file, the missing key/value pair is 'opennlp.namefinder.format=...'");
      } catch (final Exception e) {
         throw new IOException("Error loading " + type + " token name finder, does: " + String.format(modelFilename, type) + " exist in your 'resources' directory?");
      } finally {
         if (modelIn != null) {
            try {
               modelIn.close();
            } catch (final IOException e) {}
         }
      }
   }

   /**
    * Find Discourse entities (entity mentions) in a document.
    * 
    * @param sentences the document sentences
    * @return the recognized discourse entities.
    */
   public DiscourseEntity[] findEntityMentions(final String[] sentences) throws Exception {
      
      // list of document mentions
      final List<Mention> document = new ArrayList<Mention>();

      for (int i=0; i < sentences.length; i++) {
         // generate the sentence parse tree
         final Parse parse = parseSentence(sentences[i]);
         
         final DefaultParse parseWrapper = new DefaultParse(parse, i);
         final Mention[] extents = linker().getMentionFinder().getMentions(parseWrapper);
         
         //Note: taken from TreebankParser source...
         for (int ei=0, en=extents.length; ei<en; ei++) {
            // construct new parses for mentions which don't have constituents.
            if (extents[ei].getParse() == null) {
               // not sure how to get head index, but its not used at this point
               final Parse snp = new Parse(parse.getText(), extents[ei].getSpan(), "NML", 1.0, 0);
               parse.insert(snp);
               logger.debug("Setting new parse for " + extents[ei] + " to " + snp);
               extents[ei].setParse(new DefaultParse(snp, i));
            }
         }
         document.addAll(Arrays.asList(extents));
      }
      
      if (!document.isEmpty()) {
         try {
            return linker().getEntities(document.toArray(new Mention[0]));
         } catch (NullPointerException npe) {
            throw new Exception("This exception is usually thrown when you don't define '-DWNSEARCHDIR=...' in your VM args, or when a wordnet dictionary file is missing, from that directory", npe);
         }
      }

      return new DiscourseEntity[0];
   }
   
   /**
    * @return the lazily-initialized linker
    */
   private Linker linker() throws Exception {
      if (_linker == null) {
         String corefDir = getProperty(COREF_DIR);
         try {
            // linker
            logger.info("Loading the linker from: " + corefDir);
            _linker = new DefaultLinker(
              // LinkerMode should be TEST
              //Note: I tried EVAL for a long time before realizing that was the problem
              corefDir, LinkerMode.TEST);
         } catch (NullPointerException npe) {
            throw new Exception("No key/value pair found for the linker in the 'opennlp.properties' file, the missing key/value pair is 'opennlp.coref.dir=...'");
         } catch (final Exception e) {
            throw new IOException("Error loading linker from: " + corefDir + " do the coref data files exist in that directory?");
         }
      }

      return _linker;
   }
   
   /**
    * Convert the provided sentence and corresponding tokens into a parse tree.
    * 
    * @param text the sentence text
    * @return the parse tree
    */
   public Parse parseSentence(final String text) throws Exception {
      
      final Parse p = new Parse(text,
            // a new span covering the entire text
            new Span(0, text.length()),
            // the label for the top if an incomplete node
            AbstractBottomUpParser.INC_NODE,
            // the probability of this parse...uhhh...? 
            1,
            // the token index of the head of this parse
            0);

      final Span[] spans = tokenizer().tokenizePos(text);

      for (int idx=0; idx < spans.length; idx++) {
         final Span span = spans[idx];
         // flesh out the parse with token sub-parses
         p.insert(new Parse(text, span,
               AbstractBottomUpParser.TOK_NODE,
               0,
               idx));
      }

      return parse(p);
   }
   
   /**
    * Parse the given parse object.
    * <p>
    * The parser is lazily initialized on first use.
    * </p>
    * 
    * @param p the parse object
    * @return the parsed parse
    */
   private Parse parse(final Parse p) throws Exception {
      return parser().parse(p);
   }
   
   private Parser parser() throws Exception {
      if (_parser == null) {
         // lazily initialize the parser
         InputStream modelIn = null;
         String modelFilename = getProperty("opennlp.parser");
         try {
            // parser
            logger.info("Loading the parser model: " + modelFilename);
            modelIn = getClass().getResourceAsStream(modelFilename);
            final ParserModel parseModel = new ParserModel(modelIn);
            modelIn.close();
            _parser = ParserFactory.create(parseModel);
         } catch(NullPointerException npe) { // happens when modelFilename == null
            throw new Exception("No key/value pair found for the parser model in the 'opennlp.properties' file, the missing key/value pair is 'opennlp.parser=...'");
         } catch (final Exception e) {
            throw new IOException("Error loading parser, does: " + modelFilename + " exist in your 'resources' directory?");
         } finally {
            if (modelIn != null) {
               try {
                  modelIn.close();
               } catch (final IOException e) {}
            }
         }
      }

      return _parser;
   }   
   
   /**
    * Gets the specified application property.
    * 
    * @param property
    *           property of interest
    * @return value of the specified property
    */
   private String getProperty(final String property) throws Exception {
      if (_properties == null) {
         _properties = new Properties();

         InputStream input = null;
         try {
            input = getClass().getResourceAsStream(PROPERTIES_FILENAME);
            _properties.load(input);
         }
         catch (final Exception ioe) { // includes also NPE not only IOException
            throw new Exception("Error reading properties file: " + PROPERTIES_FILENAME);
         }
         finally {
            if (input != null) {
               try {
                  input.close();
               }
               catch (final IOException ioe) {
                  logger.warn("Unable to close OpenNLP properties file", ioe);
               }
            }
         }
      }

      return _properties.getProperty(property);
   }
}