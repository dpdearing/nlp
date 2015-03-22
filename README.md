# NLP Sandbox

An sandbox for trying out NLP tools.

If you have all of the dependencies in the right spot, you can verify that everything compiles and runs correctly using Maven with extra memory and the WordNet directory. This is on OSX.  Setting MAVEN_OPTS might be different on Windows.

`MAVEN_OPTS="-Xmx1024M -DWNSEARCHDIR=lib/wordnet/dict" `**`mvn test`** 

* OpenNLP: I am using OpenNLP 1.5.x.  See tutorials at  http://blog.dpdearing.com/2011/05/opennlp-1-5-0-basics-sentence-detection-and-tokenizing
 * The OpenNLP binary (`.bin`) model files can be found at http://opennlp.sourceforge.net/models-1.5/  I put the .bin model files in `src/main/resources`
 * For coreference resolution:
  * Put the coref models at `lib/opennlp/coref`
  * You also need the database files for the WordNet 3.0 dictionary from http://wordnet.princeton.edu/wordnet/download/current-version which I put at `lib/wordnet/dict`.  *Note that the WordNet 3.0 "just database files" download seems to be incomplete.  Instead, get the `dict` folder from either the full source code and binaries download or try the "WordNet 3.1 DATABASE FILES ONLY" which can replace the 3.0 files.*
  * You need to specify the WordNet dictionary folder location with a java VM argument: ```-DWNSEARCHDIR=lib/wordnet/dict```
  

