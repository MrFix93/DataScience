package model;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import java.util.Collection;
import java.util.List;

/**
 * Created by Joep on 23-Mar-17.
 */
public class TextParser {

    LexicalizedParser parser;

    TextParser(String parserFile){
        parser = LexicalizedParser.loadModel(parserFile);
    }

    public Tree parseSentece(List<CoreLabel> rawWords){

        Tree parsed = parser.apply(rawWords);

//        parsed.pennPrint();
//        System.out.println();

        return parsed;
    }

    public Collection gramStructure(List<CoreLabel> rawWords){
        Tree parse = parseSentece(rawWords);
        TreebankLanguagePack tlp = parser.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection tdl = gs.typedDependenciesCollapsed();
        //System.out.println(tdl);
        return tdl;
    }
}


