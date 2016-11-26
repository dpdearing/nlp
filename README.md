# NLP Sandbox

A sandbox for trying out NLP tools.
This project is an OpenNLP (Apache) implementation

In order to run this project you have to have all of the dependencies in the right spot (as described in the following paragraphs), as well as having the VM configured with extra memory and the WordNet dictionary directory (as a VM argument).
So please define the following (when running on your favorite IDE or as MAVEN_OPTS when using pure maven):  
* `-Xmx1024M`
* `-DWNSEARCHDIR=lib/wordnet-3.0/dict`

To run the tests with maven:

**`MAVEN_OPTS="-Xmx1024M -DWNSEARCHDIR=lib/wordnet-3.0/dict" mvn test`**

**Windows users:**

Use the WordNet dictionary files at `-DWNSEARCHDIR=lib\wordnet-3.0\dict-win`
```
set MAVEN_OPTS=-Xmx1024M -DWNSEARCHDIR=lib\wordnet-3.0\dict-win
mvn test
```

## OpenNLP

I am currently using OpenNLP 1.5.x.  [See OpenNLP 1.5 tutorials at http://blog.dpdearing.com](http://blog.dpdearing.com/2011/05/opennlp-1-5-0-basics-sentence-detection-and-tokenizing).

* The repository includes the English model files compatible with OpenNLP 1.5
* Model file locations can be overridden with a different properties file resource (that exists on the classpath) by specifying the resource name with the `opennlp.properties` system property when running OpenNlpToolkit.  If not specified it will load the default property file at `src/main/resources/com/dpdearing/nlp/opennlp/opennlp-1.5-en.properties`.
* Alternate pre-trained language-appropriate [OpenNLP binary (`.bin`) model files](http://opennlp.sourceforge.net/models-1.5/) can be downloaded and placed on the classpath (e.g., in a new subdirectory of `src/main/resources`)
* Coreference Resolution [(tutorial)](http://blog.dpdearing.com/2012/11/making-coreference-resolution-with-opennlp-1-5-0-your-bitch) depends upon:
  * The [OpenNLP 1.4 coreference model files](http://opennlp.sourceforge.net/models-1.4/english/coref/).  The English files are included in the repository at `lib/opennlp-1.5-en/coref`
  * The WordNet 3.0 dictionary files from the "source code and binaries" links of [WordNet 3.0 for UNIX-like systems](http://wordnet.princeton.edu/wordnet/download/current-version).  Only the `dict` subdirectory is necessary.  These files are in the repository at `lib/wordnet-3.0/dict`.
    * *Note: Do not download any other files (e.g., v2.1, v3.1 or "just database files").  This project's code is verified to work files for the v3.0 of WordNet only.*
    * **Windows users:** Rename the WordNet `data.xxx` and `index.xxx` dictionary files:
      * Remove the `data.` prefix and add the `.dat` extension (i.e., `data.noun` becomes `noun.dat`)
      * Remove the `index.` prefix and add the `.idx` extension (i.e., `index.noun` becomes `noun.idx`)
      * The WordNet files named for the Windows platform are in the repository at `lib\wordnet-3.0\dict-win`

