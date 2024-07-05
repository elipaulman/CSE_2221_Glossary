import java.io.Serializable;
import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Extracts terms and definitions from .txt file and outputs as .html file in
 * the form of a glossary.
 *
 * @author Elijah Paulman
 *
 */
public final class Glossary {
    /**
     * Private constructor to prevent instantiation.
     */
    private Glossary() {

    }

    /**
     * Comparator implementation that sorts strings in alphabetical order.
     */
    public static class AlphabeticalSort
            implements Comparator<String>, Serializable {

        public static final long serialVersionUID = 1L;

        /**
         * Compare two strings and return a negative integer, zero, or a
         * positive integer depending on whether the first string is less than,
         * equal to, or greater than the second string.
         *
         * @param str1
         *            The first string to be compared.
         * @param str2
         *            The second string to be compared.
         * @return A negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second
         */
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        // Initializes count, returnedCh and returnedStr (empty string for now)
        int count = 0;
        char returnedCh = 'a';
        String returnedStr = "";

        // If the character at the given position is a separator
        if (separators.contains(text.charAt(position))) {
            /*
             * Iterates through the characters in the text string, starting from
             * the given position, until a non-separator character is found
             */
            while (position + count < text.length()
                    && separators.contains(text.charAt(position + count))) {
                // Sets returnedCh to the current character and appends it to returnedStr
                returnedCh = text.charAt(position + count);
                returnedStr += returnedCh;
                count++;
            }
        } else {
            /*
             * If the character at the given position is not a separator,
             * iterates through the characters in the text string starting from
             * the given position, until a separator character is found
             */
            while (position + count < text.length()
                    && !separators.contains(text.charAt(position + count))) {
                // Sets returnedCh to the current character and appends it to returnedStr
                returnedCh = text.charAt(position + count);
                returnedStr += returnedCh;
                count++;
            }
        }
        // Returns accumulated characters as a string
        return returnedStr;
    }

    /**
     * Gets terms and definitions from input file and maps definitions to
     * corresponding term. Returns queue containing all terms
     *
     * @param definitionMap
     *            map of <term, definition> as strings
     * @param in
     *            input file as .txt file
     * @return a queue queue of all terms
     * @requires empty map and the input file contains pairs of lines in the
     *           format "term\n definition\n"
     * @ensures Map has term as key and corresponding definition as value
     */
    public static Queue<String> getTermsAndDefs(
            Map<String, String> definitionMap, SimpleReader in) {

        // Makes new queue for terms
        Queue<String> terms = new Queue1L<>();

        while (!in.atEOS()) {
            // Gets term from input file
            String term = in.nextLine();
            // Gets one line of definition from input file
            String definition = in.nextLine();

            // Accounts for possible extra line of definition
            String extraLineOfDef = " ";
            // If extra line is not 0 and end of input file has not been reached
            while (extraLineOfDef.length() > 0 && !in.atEOS()) {
                // Extra line of definition is added from next line of input
                extraLineOfDef = in.nextLine();
                // If the length of extra line is greater than 0, add to definition
                if (extraLineOfDef.length() > 0) {
                    definition += extraLineOfDef;
                }
            }
            // Add terms and corresponding definition to map
            definitionMap.add(term, definition);
            // Add term to queue
            terms.enqueue(term);
        }
        // Returns queue of terms
        return terms;
    }

    /**
     * Updates the definitions of terms in a given map with links to other
     * terms, if applicable.
     *
     * @param definitionMap
     *            the map containing the terms and their definitions
     * @param terms
     *            the queue of terms to be checked and updated
     * @param out
     *            the output file to write to (not used in this method)
     */
    public static void updateDefinitions(Map<String, String> definitionMap,
            Queue<String> terms, String out) {

        // Makes temporary queue
        Queue<String> temp = new Queue1L<>();

        // Creates set of all possible separators
        Set<Character> separators = new Set1L<>();
        separators.add(' ');
        separators.add('!');
        separators.add('\'');
        separators.add('.');
        separators.add(',');
        separators.add('/');
        separators.add(':');
        separators.add(';');
        separators.add('?');

        // For all terms starting at position 0
        int position = 0;
        while (terms.length() > 0) {
            // Gets first term from terms queue
            String term = terms.dequeue();
            // Adds removed term to temporary queue
            temp.enqueue(term);
            // Gets definition from map
            String definition = definitionMap.value(term);
            // Initializes final definition as blank string
            String fullDefinition = " ";
            //the position can not be larger than the string
            while (position < definition.length()) {
                // Gets current word
                String word = nextWordOrSeparator(definition, position,
                        separators);
                // If the word is another term
                if (definitionMap.hasKey(word)) {
                    // Word is linked in definition
                    fullDefinition += "<a href=\"" + word + ".html\">" + word
                            + "</a>";
                    // If word is not another term, add word to definition as regular text
                } else {
                    fullDefinition += word;
                }
                // Moves to position after word
                position = position + word.length();
            }
            // Updates definition with link to another term if applicable
            definitionMap.replaceValue(term, fullDefinition.substring(1));
            // Reset position
            position = 0;
        }
        // Restores terms from temp queue
        terms.transferFrom(temp);
    }

    /**
     * Adds linkOuts to each term in definitionMap to an html file with the term
     * as its name and writes each file to the specified directory. Each file
     * contains a title with the corresponding term, the definition of the term,
     * and a linkOut to the index file.
     *
     * @param definitionMap
     *            map of <term, definition> as strings
     * @param terms
     *            queue of all terms
     * @param out
     *            output directory where html files will be saved
     * @requires definitionMap and terms are not empty, out is a valid directory
     * @ensures an html file is created for each term in definitionMap with
     *          linkOuts to the index file
     */
    public static void addLinks(Map<String, String> definitionMap,
            Queue<String> terms, String out) {
        // Makes temporary queue
        Queue<String> temp = new Queue1L<>();

        // For each item in the queue
        while (terms.length() > 0) {
            // Gets term from terms queue
            String term = terms.dequeue();
            // Gets corresponding definition from map
            String definition = definitionMap.value(term);
            // Adds term to temp queue
            temp.enqueue(term);

            // New writer to output html elements
            SimpleWriter linkOut = new SimpleWriter1L(
                    out + "/" + term + ".html");
            // Creates html page for each term
            linkOut.println("<html>");
            linkOut.println("<head>");
            // Term is title
            linkOut.println("<title>" + term + "</title>");
            linkOut.println("</head>");
            linkOut.println("<body>");
            // Adds formatting to term
            linkOut.print("<h2><b><i><font color = \"red\">");
            linkOut.print(term);
            linkOut.println("</font></i></b></h2>");
            linkOut.print("<blockquote>");
            // Prints definition to corresponding term
            linkOut.print(definition);
            linkOut.println("</blockquote>");
            // Adds horizontal line
            linkOut.println("<hr />");
            // Adds option to return to index home page
            linkOut.println(
                    "<p>Return to <a href=\"index.html\">index</a></p>");
            linkOut.println("</body>");
            linkOut.println("</html>");

            // Closes SimpleWriter
            linkOut.close();
        }
        // Restores terms from temp
        terms.transferFrom(temp);
    }

    /**
     * Creates an index for the glossary by generating an HTML file. The index
     * contains a list of terms with linkOuts to individual HTML pages for each
     * term.
     *
     * @param terms
     *            the queue of terms to be included in the index
     * @param output
     *            the file name of the output HTML file
     * @param out
     *            a SimpleWriter object used to write the HTML file
     * @requires terms is not null and out is not null
     * @modifies out
     * @effects generates an HTML file containing an index of all terms with
     *          linkOuts to individual pages for each term
     */
    public static void createIndex(Queue<String> terms, String output,
            SimpleWriter out) {
        // Prints html elements for index page
        out.println("<html>");
        out.println("<head>");
        // Adds glossary title
        out.println("<title>Glossary</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Glossary</h2>");
        out.println("<hr />");
        out.println("<h3>Index</h3>");
        out.println("<ul>");
        // For all terms, creates a clickable linkOut with term as text
        for (String term : terms) {
            out.println(
                    "<li><a href=\"" + term + ".html\">" + term + "</a></li>");
        }
        // Closes html elements
        out.println("</ul>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     *
     * Prompts the user for an input file and an output folder, reads the input
     * file, extracts the terms and their definitions, and creates an HTML file
     * with an index of all terms and definitions sorted alphabetically. It also
     * adds links to each term that point to their individual definition pages.
     *
     * @param args
     *            the command line arguments
     *
     * @requires args is not null
     *
     * @modifies creates an HTML file with the name "index.html" in the
     *           specified output folder with an index of all terms and
     *           definitions sorted alphabetically
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        // Prompts user for input file
        out.print("Enter input file (.txt): ");
        String input = in.nextLine();
        // Prompts user for save file location
        out.print("Enter folder name for save: ");
        String output = in.nextLine();

        // Makes new reader and writer for input and output files
        SimpleReader fileIn = new SimpleReader1L(input);
        SimpleWriter fileOut = new SimpleWriter1L(output + "/index.html");

        // Empty map for words and definitions both as strings
        Map<String, String> definitionMap = new Map1L<>();
        // Empty queue for terms
        Queue<String> terms = new Queue1L<>();

        // Adds terms to terms queue made above and adds term and definitions to map
        terms.append(getTermsAndDefs(definitionMap, fileIn));

        // New comparator to sort alphabetically
        Comparator<String> alphabetical = new AlphabeticalSort();
        // Sorts terms queue alphabetically
        terms.sort(alphabetical);

        // Checks for definitions within a definition
        updateDefinitions(definitionMap, terms, output);

        // Adds linkOuts to all terms
        addLinks(definitionMap, terms, output);

        // Creates index
        createIndex(terms, output, fileOut);

        // Close all open readers and writers
        in.close();
        out.close();
        fileIn.close();
        fileOut.close();
    }
}
