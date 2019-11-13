# autoprox
For a process `p` in a database with a set of background processes `Q`,
`autoprox` generates a set of bridge processes `B` as described in
[Ingwersen et al. 2018](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6463304/)
that connect the product inputs and waste outputs of `p` with corresponding
product outputs and waste inputs provided by the processes in `Q`.

## Implemented matchers

### The `BigramsDiceMatcher`

### The `InfoContentMatcher`

### The `WordNetPathMatcher`

The `WordNetPathMatcher` uses the [WS4j](https://code.google.com/archive/p/ws4j)
API to calculate semantic similarities between words using
[WordNet](https://wordnet.princeton.edu).

WS4j is an archived Google Code project and a bit complicated to set up (see
below) and is compatible with a relative old version of WordNet. An alternative
could be [JWI](http://projects.csail.mit.edu/jwi/) which supports to load
a current WordNet database from a folder (just download and extract the
[WordNet database files](http://wordnetcode.princeton.edu/3.0/WNdb-3.0.tar.gz)
to that folder):

```kotlin
val wordNetPath = "C:/Users/ms/Downloads/WNdb-3.0/dict"
val dict = RAMDictionary(File(wordNetPath), ILoadPolicy.NO_LOAD)
dict.open()
val idxWord = dict.getIndexWord("asphalt", POS.NOUN)
if (idxWord != null) {
    val word = dict.getWord(idxWord.wordIDs[0])
    word.synset.relatedSynsets.
}
```

However, WS4j provides a lot of features and
[algorithms](http://ws4jdemo.appspot.com) that can be used easily while JWI
provides a more low level API (but with a nice
[tutorial](http://projects.csail.mit.edu/jwi/download.php?f=edu.mit.jwi_2.4.0_manual.pdf)).

## Building from source

### WS4j
For calculating semantic similarities this project uses
[WS4j](https://code.google.com/archive/p/ws4j). WS4j is an archived project
on Google Code bit there is also a [Github clone available](https://github.com/Sciss/ws4j)
which seems to be the version that is published in the Maven central repository.
In order to run WS4j, you need to put the configuration files
[jawjaw.conf and similarity.conf](https://github.com/Sciss/ws4j/tree/master/config)
and the database file `wnjpn.db` into the class-path. The `wnjpn.db` file can
be extracted from the distribution packages from the
[WS4j Google Code download pages](https://code.google.com/archive/p/ws4j/downloads).
