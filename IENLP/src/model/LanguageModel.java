package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static model.Util.cleanTitle;

/**
 * Created by peterwessels on 06/03/2017.
 */
public class LanguageModel {
    Map<String, Double> NGrams = new HashMap<>();
    BufferedReader in;


    public static void main(String[] args) {
        LanguageModel lm = new LanguageModel();
        lm.in = new BufferedReader(new InputStreamReader(System.in));
        lm.start();
    }

    public void start() {
        try {
            String input = this.in.readLine();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
