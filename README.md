# NLP Sandbox

A sandbox for trying out NLP tools.
This project is an OpenNLP (Apache) implementation

In order to run this project you have to have all of the dependencies in the right spot (as described in the following paragraphs), as well as having the VM configured with extra memory and the WordNet directory (as a VM argument).
So please define the following (when running on your favorite IDE or as MAVEN_OPTS when using pure maven):  
* `-Xmx1024M`
* `-DWNSEARCHDIR=lib/wordnet/dict`

(for maven) **`MAVEN_OPTS="-Xmx1024M -DWNSEARCHDIR=lib/wordnet/dict" mvn test`**

## OpenNLP

I am currently using OpenNLP 1.5.x.  [See OpenNLP 1.5 tutorials at http://blog.dpdearing.com](http://blog.dpdearing.com/2011/05/opennlp-1-5-0-basics-sentence-detection-and-tokenizing).

* The pre-trained language-appropriate [OpenNLP binary (`.bin`) model files](http://opennlp.sourceforge.net/models-1.5/) should be downloaded, then copied to `src/main/resources`
* For Coreference Resolution [(tutorial)](http://blog.dpdearing.com/2012/11/making-coreference-resolution-with-opennlp-1-5-0-your-bitch):
  * Download the [Coreference files](http://opennlp.sourceforge.net/models-1.4/english/coref/) then copy them to `lib/opennlp/coref`
  * Download the WordNet 3.0 dictionary files from the "source code and binaries" links of [WordNet 3.0 for UNIX-like systems](http://wordnet.princeton.edu/wordnet/download/current-version) and copy the `dict` subdirectory to `lib/wordnet/dict`.
    * *Note: Do not download any other files (e.g., v2.1, v3.1 or "just database files").  This project's code is verified to work files for the v3.0 of WordNet only.*
    * **(Windows users)** Rename the WordNet `data.xxx` and `index.xxx` dictionary files:
      * Remove the `data.` prefix and add the `.dat` extension (i.e., `data.noun` becomes `noun.dat`)
      * Remove the `index.` prefix and add the `.idx` extension (i.e., `index.noun` becomes `noun.idx`)
* Resource locations are configurable using the `opennlp.properties` file (in the `src/main/resources/com/dpdearing/nlp/opennlp` directory)
