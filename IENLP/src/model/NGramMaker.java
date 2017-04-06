package model;
/*
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by peterwessels on 28/02/2017.
 */
/*
public class NGramMaker {
    private TokenStream tokens;
    private BufferedReader in;

    public static void main(String[] args) {
        NGramMaker analyzer = new NGramMaker();
        analyzer.in = new BufferedReader(new InputStreamReader(System.in));
        analyzer.start();

    }

    public void start() {
        boolean running = true;

        String input;
        while(running) {
            try {
                input = this.in.readLine();

                switch (input) {
                    case "tokenize":
                        System.out.print(this.getTokens());
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getTokens() throws IOException {
        System.out.println("Give your input");
        String input = this.in.readLine();

        TokenStream tk = tokenizeString(input);
        return createNGramList(tk);
    }

    /**
     * tokenizeString
     * @param text
     * @return
     */
/*
    public static TokenStream tokenizeString(String text) {
        return new WhitespaceTokenizer(Version.LUCENE_36,  new StringReader(text));
    }

    /**
     *
     * @param tk
     * @return
     */
/*
    public static List<String> createNGramList(TokenStream tk) {
        List<String> result = new ArrayList<>();

        tk = new ShingleFilter(tk);
        tk = new EdgeNGramTokenFilter(tk, EdgeNGramTokenFilter.Side.BACK , 2, 4);
        CharTermAttribute termAtt = tk.addAttribute(CharTermAttribute.class);

        try {
            while (tk.incrementToken()) {
                result.add(termAtt.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
**/
