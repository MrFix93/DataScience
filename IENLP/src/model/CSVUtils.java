package model;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static model.Util.sortByComparator;

public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ',';

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    public static void writeLine(Writer w, List<String> values, char separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());


    }
    /*
    public static void toCsv(String fileName, List<String,HashMap<String, Integer>> frequencyTweets) throws IOException {
        FileWriter writer = new FileWriter(fileName);

        for(Map.Entry<String, HashMap<String, Integer>> frequencies : frequencyTweets.entrySet()){
            System.out.println(frequencies.getKey());
            CSVUtils.writeLine(writer, Arrays.asList(frequencies.getKey()));
            int i=0;
            for(Map.Entry<String, Integer> map : sortByComparator(frequencies.getValue(),false).entrySet()){
                if(i++ > LanguageModel.LIMIT)
                    break;

                System.out.println(map.getKey() + " : " + map.getValue() + " " + i);
                CSVUtils.writeLine(writer, Arrays.asList(map.getKey(), map.getValue().toString()));
            }
            CSVUtils.writeLine(writer, Arrays.asList());
        }
        writer.flush();
        writer.close();
    }
    */

}