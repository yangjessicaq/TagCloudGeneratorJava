import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This Java components program generates a tag cloud from the most frequent
 * words in a given input text to a HTML file.
 *
 * @author Jessica Yang
 * @author Benjamin Nataniel Escobar Alfaro
 *
 */
public final class TagCloudGenerator {
    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Definition of whitespace separators.
     */
    private static final String SEPARATORS = " \t\n\r,-.!?[]';:/()*`1234567890\"{}~<>";

    /**
     * Sorts the keys of Map.Entry<String, Integer> in alphabetical order.
     */
    private static class AlphabetSort
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {

            int comparator = o1.getKey().compareToIgnoreCase(o2.getKey());

            // for computer to make sure consistent with equals
            if (comparator == 0) {
                comparator = o1.getValue().compareTo(o2.getValue());
            }

            return comparator;
        }
    }

    /**
     * Sorts the values of Map.Entry<String, Integer> in numeric decreasing
     * order.
     */
    private static class NumberSort
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {

            // first compare the values
            int comparator = o2.getValue().compareTo(o1.getValue());

            // ensure consistency with equals; don't return 0 if keys are not also equal
            if (comparator == 0) {
                comparator = o1.getKey().compareToIgnoreCase(o2.getKey());
            }

            return comparator;
        }
    }

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
    }

    // METHOD FROM SOFTWARE I
    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        charSet.clear();
        for (int i = 0; i < str.length(); i++) {
            if (!charSet.contains(str.charAt(i))) {
                charSet.add(str.charAt(i));
            }
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code SEPARATORS}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(SEPARATORS) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(SEPARATORS))
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        String word = "";
        int i = position + 1;

        // as long as i is less than length of the string and
        // the current character is not a separator, add the character
        while (i < text.length()
                && SEPARATORS.indexOf(text.charAt(position)) == SEPARATORS
                        .indexOf(text.charAt(i))) {
            i++;
        }

        // take substring from first char to last char of string
        word = text.substring(position, i);

        return word;

    }

    /**
     * Finds the correct font size for each word respective to the number of
     * occurrences.
     *
     * @param max
     *            maximum count in tag cloud words
     * @param min
     *            minimum count in tag cloud words
     * @param count
     *            count of the word
     * @requires max > 0 && min > 0
     * @return the font size for a specific word in the appropriate range
     */
    private static String getFontSize(int max, int min, int count) {
        final int maxFont = 48;
        final int minFont = 11;

        int size = maxFont - minFont;
        if (max > min) {
            // derived from the Tag Cloud formula
            // have to subtract minimum as formula in Wiki doesn't have a min
            size = size * (count - min);
            size = size / (max - min);
            // make sure minimum is above 11
            size = size + minFont;
        } else {
            size = maxFont;
        }

        return "f" + size;
    }

    /**
     * Prints the header tags as well as the title of the HTML page. Also
     * declares the CSS that will be used to format the page.
     *
     * @param printHTML
     *            output stream
     * @param numWords
     *            number of words to print
     * @param inputFile
     *            name of the input file that was read from
     * @ensures printHTML prints a header
     * @requires printHTML.is_open
     */
    public static void header(PrintWriter printHTML, int numWords,
            String inputFile) {
        // print out header tags for tag cloud HTML
        printHTML.println("<html><head><title>Top " + numWords + " words in "
                + inputFile + "</title>");
        printHTML.println(
                "<link href=\"doc/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        printHTML.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        printHTML.println("</head><body class=\"vsc-initialized\">");
        printHTML.println(
                "<h2>Top " + numWords + " words in " + inputFile + "</h2>");
        printHTML.println("<hr>");
        printHTML.println("<div class = \"cdiv\">");
        printHTML.println("<p class = \"cbox\">");
    }

    /**
     * Prints a HTML page with a tag cloud of numWords words. First prints
     * header, then calculates font for each word and prints word, finally
     * prints footer.
     *
     * @param printHTML
     *            output stream
     * @param wordCounter
     *            map containing the words and word counts
     * @param numWords
     *            number of words to print
     * @param outputFile
     *            name of the file to print to
     * @param inputFile
     *            name of the input file that was read from
     * @ensures printHTML prints numWords of words from a sorted wordCounter and
     *          a footer
     * @requires printHTML.is_open && wordCounter.length() != 0
     */
    public static void printTagCloudHTML(PrintWriter printHTML,
            Map<String, Integer> wordCounter, int numWords, String outputFile,
            String inputFile) {

        header(printHTML, numWords, inputFile);

        // create comparator and map for sorting numerically
        Comparator<Map.Entry<String, Integer>> byNumber = new NumberSort();
        List<Map.Entry<String, Integer>> sortNum = new ArrayList<Map.Entry<String, Integer>>();
        Set<Map.Entry<String, Integer>> keys = wordCounter.entrySet();

        // add all pairs from wordCounter to sorting machine
        // use an iterator and a set in place of removeAny()
        Iterator<Entry<String, Integer>> it = keys.iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            sortNum.add(entry);
        }
        sortNum.sort(byNumber);

        // organize list with number of words user provided
        List<Map.Entry<String, Integer>> finalSort = new ArrayList<Map.Entry<String, Integer>>();
        while (sortNum.size() > 0 && finalSort.size() < numWords) {
            // get the first entry, add it to the limited list, and remove from original
            Map.Entry<String, Integer> entry = sortNum.get(0);
            sortNum.remove(0);
            finalSort.add(entry);
        }

        // take max and min (first and last of the list)
        int max = 0;
        int min = 0;
        if (finalSort.size() > 0) {
            max = finalSort.get(0).getValue();
            min = finalSort.get(finalSort.size() - 1).getValue();
        }

        // create comparator for sorting alphabetically
        Comparator<Map.Entry<String, Integer>> byAlph = new AlphabetSort();
        finalSort.sort(byAlph);

        // print HTML to print each word
        for (Map.Entry<String, Integer> word : finalSort) {
            String size = getFontSize(max, min, word.getValue());
            String tag = "<span style=\"cursor:default\" class=\"" + size
                    + "\" title=\"count: " + word.getValue() + "\">"
                    + word.getKey() + "</span>";
            printHTML.println(tag);
        }

        // print footer tags for tag cloud HTML
        printHTML.println("</p></div></body></html>");
    }

    // METHOD FROM SOFTWARE I; added toLowerCase
    /**
     * Gets input from txt file and stores data in map with words and word
     * counts as keys and values respectively.
     *
     * @param input
     *            input stream file
     * @return map with words and word counts
     * @ensures getMap = [a map where (key, value) is (word, word count)]
     */
    public static Map<String, Integer> getMap(BufferedReader input) {
        Map<String, Integer> wordCounter = new HashMap<>();
        Set<Character> separators = new HashSet<>();
        generateElements(SEPARATORS, separators);

        try {
            String line = input.readLine();
            while (line != null) {
                line = line.toLowerCase();
                int position = 0;
                // position runs through the line
                while (position < line.length()) {
                    // get individual words
                    String part = nextWordOrSeparator(line, position);
                    // if in map, increment word count, else add
                    if (!separators.contains(part.charAt(0))) {
                        if (wordCounter.containsKey(part)) {
                            int newCount = wordCounter.get(part) + 1;
                            wordCounter.replace(part, newCount);
                        } else {
                            wordCounter.put(part, 1);
                        }
                    }
                    position += part.length();
                }
                line = input.readLine();
            }
        } catch (IOException e) {
            System.err.println("Input cannot be read.");
        }

        return wordCounter;
    }

    /*
     * Main test method -------------------------------------------------------
     */
    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        // wrap input stream
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));

        // initialize the variables used in the try catch
        String inputFile = "";
        String outputFile = "";
        int num = 0;
        try {
            // ask for input and output files and check if valid
            System.out.print("Enter input file name: ");
            inputFile = in.readLine();

            // ask for an output file name
            System.out.print("Enter output file name: ");
            outputFile = in.readLine();

            // get num of words and check for valid input
            System.out.print("Enter how many words to be in tag cloud: ");
            num = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Input cannot be read.");
        }
        if (num < 0) {
            System.err.println("Must have positive number of words.");
        }

        // create output file and read input file
        PrintWriter printHTML;
        BufferedReader readFile;
        try {
            printHTML = new PrintWriter(outputFile);
            readFile = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            // program should not continue to run if files do not work
            return;
        }

        // create the default map with the unordered words and counts
        Map<String, Integer> wordCounter = getMap(readFile);

        // check if num is bigger than number of words in file
        if (num > wordCounter.size()) {
            System.err.println(
                    "Desired words in tag cloud must not exceed total words.");
            num = wordCounter.size();
        }

        // generate tag cloud
        printTagCloudHTML(printHTML, wordCounter, num, outputFile, inputFile);

        // close input and output streams
        try {
            printHTML.close();
            readFile.close();
            in.close();
        } catch (IOException e) {
            System.err.println("Cannot close streams.");
        }
    }

}
