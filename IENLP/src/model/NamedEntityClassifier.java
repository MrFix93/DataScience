package model;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.util.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Joep on 22-Mar-17.
 */
public class NamedEntityClassifier {

    private AbstractSequenceClassifier<CoreLabel> classifier;

    NamedEntityClassifier(String classifierFolder,String classifierName) throws Exception{

        classifier = CRFClassifier.getClassifier(classifierFolder + classifierName);


    }

    public static void main(String[] args) throws Exception {

        System.out.println(args[0]);
        NamedEntityClassifier namedEntityClassifier = new NamedEntityClassifier(args[0],args[1]);

        namedEntityClassifier.classifyString("This is an test Joep, Henk wrote for Peter Wessels in the University of Twente");

    }

    public String classifyString(String string){

        HashMap<String,List<String>> result = new HashMap<>();

        List<List<CoreLabel>> out = classifier.classify(string);
        for (List<CoreLabel> sentence : out) {

            String classifiedString = "";
            String prefClassifierClass = null;
            for (CoreLabel word : sentence) {

                String classifierClass = word.get(CoreAnnotations.AnswerAnnotation.class);

                //Add different words that are labled after each other to one string
                if(prefClassifierClass == null || classifierClass.equals(prefClassifierClass)){
                    if(!classifiedString.isEmpty()){
                        classifiedString += " ";
                    }
                    classifiedString += word.word();

                }else{
                    //add previous string to the result
                    if(result.containsKey(prefClassifierClass)){ //if already in result
                        result.get(prefClassifierClass).add(classifiedString);
                    }else{
                        List<String> list = new ArrayList<String>(Arrays.asList(classifiedString));
                        result.put(prefClassifierClass,list);
                    }

                    //begin new string
                    classifiedString = word.word();
                }

                prefClassifierClass = classifierClass;

//
//              System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
            }
        }

        NamedEntityEvaluater.printEntities(result);

        return null;

    }
}
