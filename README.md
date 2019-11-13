# autoprox
For a process `p` in a database with a set of background processes `Q`,
`autoprox` generates a set of bridge processes `B` as described in
[Ingwersen et al. 2018](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6463304/)
that connect the product inputs and waste outputs of `p` with corresponding
product outputs and waste inputs provided by the processes in `Q`.

## Building from source


WS4j
https://github.com/Sciss/ws4j
http://ws4jdemo.appspot.com/?mode=s&s1=Coal+conveyor+system%3B+Manufacture%3B+For+underground+mine%2C+at+point-of-sale%3B+1.22+m+belt+width&s2=Picking+line+conveyor+with+apron%3B+30+hp%3B+electric
https://code.google.com/archive/p/ws4j/
println(WS4J.runPATH("hog", "fuel"))
    println(WS4J.runPATH("diesel", "fuel"))

JWI
http://projects.csail.mit.edu/jwi/
projects.csail.mit.edu/jwi/download.php?f=edu.mit.jwi_2.4.0_manual.pdf

http://wordnetcode.princeton.edu/3.0/WNdb-3.0.tar.gz

val wordNetPath = "C:/Users/ms/Downloads/WNdb-3.0/dict"
    val dict = RAMDictionary(File(wordNetPath), ILoadPolicy.NO_LOAD)
    dict.open()
    val idxWord = dict.getIndexWord("asphalt", POS.NOUN)
    if (idxWord != null) {
        val word = dict.getWord(idxWord.wordIDs[0])
        word.synset.relatedSynsets.
    }