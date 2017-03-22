package model.NamedEntityClassifiers;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import model.NamedEntityEvaluater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Joep on 18-Mar-17.
 */
public class NamedEntityClassifier{

    public class NamedEntityType{
        public String TAG;
        public String START;
        public String STOP;

        public NamedEntityType(String tag, String start, String stop){
            this.TAG = tag;
            this.START = start;
            this.STOP = stop;
        }
    }
    private AbstractSequenceClassifier<CoreLabel> classifier;
    public List<NamedEntityType> entityTypes;
    public String language;

    public NamedEntityClassifier(){
        entityTypes = new ArrayList<>();
    }


    NamedEntityClassifier(String classifierFolder,String classifierName){
        setClassifier(classifierFolder,classifierName);

    }

    public void setClassifier(String classifierFolder,String classifierName) {
        try {
            classifier = CRFClassifier.getClassifier(classifierFolder + classifierName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    public static NamedEntityClassifier getClassifierForTypeClass(String typeClass){
        NamedEntityClassifier classifier = new NamedEntityClassifier();
        switch (typeClass) {
            case NamedEntityClassifier_3class.typeClass:
                classifier = new NamedEntityClassifier_3class();

                break;
            case NamedEntityClassifier_4class.typeClass:
                classifier = new NamedEntityClassifier_4class();

                break;
            case NamedEntityClassifier_7class.typeClass:
                classifier = new NamedEntityClassifier_7class();

                break;
            default:
                classifier = new NamedEntityClassifier_3class();

                break;
        }

        return classifier;
    }


    public static void main(String[] args) throws Exception {

        System.out.println(args[0]);
        NamedEntityClassifier namedEntityClassifier = new NamedEntityClassifier(args[0],args[1]);

        namedEntityClassifier.classifyString("This is an test Joep, Henk wrote for Peter Wessels in the University of Twente");

    }

    public HashMap<String,List<String>> classifyString(String string){

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

//        NamedEntityEvaluater.printEntities(result);

        return result;

    }
}
