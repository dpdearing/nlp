# NLP Sandbox

A sandbox for trying out NLP tools.
This project is an OpenNLP (Apache) implementation

In order to run this project you have to have all of the dependencies in the right spot (as described in the following paragraphs), as well as having the VM configured with extra memory and the WordNet directory (as a VM argument).
So please define the following (when running on your favorite IDE or as MAVEN_OPTS when using pure maven):  
`-Xmx1024M`  
`-DWNSEARCHDIR=lib/wordnet/dict`  
(for maven) `MAVEN_OPTS="-Xmx1024M -DWNSEARCHDIR=lib/wordnet/dict"` **`mvn test`**



* **OpenNLP:** I am currently using OpenNLP 1.5.x,  [See tutorials](http://blog.dpdearing.com/2011/05/opennlp-1-5-0-basics-sentence-detection-and-tokenizing).

* The [OpenNLP binary (`.bin`) model files](http://opennlp.sourceforge.net/models-1.5/) should be downloaded, then copied to `src/main/resources`
* Download the [Coreference files](http://opennlp.sourceforge.net/models-1.4/english/coref/) then copy them to `lib/opennlp/coref`
* Download [Wordnet v3.0 dictionary files](http://wordnet.princeton.edu/wordnet/download/current-version) from the "Source code and binaries" under the Unix section and copy them to `lib/wordnet/dict`.  *Note Do not download any other files not v2.1 nor v3.1 for example as my project's code works 100% with the v3.0 of wordnet only.*
* (Windows users) Go to the wordnet dictionary files and rename the data file to have .dat extension (remove the "data" prefix from the filenames), and the index files to have a .idx extension (remove the "index" prefix from the filename)
* Directory locations are configurable using the `opennlp.properties` file (in the `resources` directory)
