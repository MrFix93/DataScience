package model;

/**
 * Created by Joep on 18-Mar-17.
 */
public class ConfusionMatrix {
    public int true_positives = 0;
    public int false_positives = 0;
    public int false_negatives = 0;
    public int true_negatives = 0;

    public void combine(ConfusionMatrix confusionMatrix){
        true_positives += confusionMatrix.true_positives;
        false_positives += confusionMatrix.false_positives;
        true_negatives += confusionMatrix.true_negatives;
        false_negatives += confusionMatrix.false_negatives;

    }
}
