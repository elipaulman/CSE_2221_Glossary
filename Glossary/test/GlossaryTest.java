import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

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
 * @author Elijah Paulman
 *
 */

public class GlossaryTest {

    // Test cases for nextWordOrSeparator

    /**
     * Test case starting with separators.
     */
    @Test
    public void testNextWordOrSeparator() {
        String text = ",/example";
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
        String expected = ",/";
        int position = 0;
        String actual = Glossary.nextWordOrSeparator(text, position,
                separators);
        assertEquals(expected, actual);
    }

    /**
     * Test that starts with word and contains separator.
     */
    @Test
    public void testNextWordOrSeparator2() {
        String text = "example/123";
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
        String expected = "example";
        int position = 0;
        String actual = Glossary.nextWordOrSeparator(text, position,
                separators);
        assertEquals(expected, actual);
    }

    /**
     * Test with a string that ends with a word.
     */
    @Test
    public void testNextWordOrSeparator3() {
        String text = "example-123";
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
        String expected = "-123";
        final int position = 7;
        String actual = Glossary.nextWordOrSeparator(text, position,
                separators);
        assertEquals(expected, actual);
    }

    // Tests of getTermsAndDefs

    /**
     * Checks if length of terms matches number of terms.
     */
    @Test
    public void testGetTermsAndDefs() {
        Map<String, String> definitionMap = new Map1L<>();
        SimpleReader in = new SimpleReader1L("data/terms.txt");
        Queue<String> terms = Glossary.getTermsAndDefs(definitionMap, in);
        final int seven = 7;
        assertEquals(seven, terms.length());
    }

    /**
     * Tests with empty input file.
     */
    @Test
    public void testGetTermsAndDefs2() {
        Map<String, String> definitionMap = new Map1L<>();
        SimpleReader in = new SimpleReader1L("test/emptyTest.txt");
        Queue<String> terms = Glossary.getTermsAndDefs(definitionMap, in);
        assertEquals(0, terms.length());
    }

    /**
     * Checks if terms and corresponding definitions are correctly mapped.
     */
    @Test
    public void testGetTermsAndDefs3() {
        Map<String, String> definitionMap = new Map1L<>();
        SimpleReader in = new SimpleReader1L("data/terms.txt");
        Queue<String> terms = Glossary.getTermsAndDefs(definitionMap, in);
        final int seven = 7;
        assertEquals(seven, terms.length());
        assertEquals("meaning", terms.dequeue());
        assertEquals("term", terms.dequeue());
        assertEquals(true, definitionMap.hasKey("meaning"));
        assertEquals(true, definitionMap.hasKey("term"));
        assertEquals(
                "something that one wishes to convey, especially by language",
                definitionMap.value("meaning"));
        assertEquals("a word whose definition is in a glossary",
                definitionMap.value("term"));
    }

    // Tests of updateDefinitions

    /**
     * Test with single term.
     */
    @Test
    public void testUpdateDefinitionsSingleTerm() {
        Map<String, String> definitionMap = new Map1L<>();
        Queue<String> terms = new Queue1L<>();
        terms.enqueue("apple");
        definitionMap.add("apple", "a fruit");
        Glossary.updateDefinitions(definitionMap, terms, "out.txt");
        assertEquals("a fruit", definitionMap.value("apple"));
    }

    /**
     * Test with multiple terms.
     */
    @Test
    public void testUpdateDefinitionsMultipleTerms() {
        Map<String, String> definitionMap = new Map1L<>();
        Queue<String> terms = new Queue1L<>();
        terms.enqueue("apple");
        terms.enqueue("banana");
        definitionMap.add("apple", "a fruit");
        definitionMap.add("banana", "a yellow fruit");
        Glossary.updateDefinitions(definitionMap, terms, "out.txt");
        assertEquals("a fruit", definitionMap.value("apple"));
        assertEquals("a yellow fruit", definitionMap.value("banana"));
    }

    /**
     * Test that the method correctly adds links to other terms in the
     * definitions, when applicable.
     */
    @Test
    public void testUpdateDefinitionsLinks() {
        Map<String, String> definitionMap = new Map1L<>();
        Queue<String> terms = new Queue1L<>();
        terms.enqueue("apple");
        terms.enqueue("banana");
        definitionMap.add("apple", "a fruit");
        definitionMap.add("banana",
                "a yellow fruit that is related to <a href=\"apple.html\">apples</a>");
        Glossary.updateDefinitions(definitionMap, terms, "out.txt");
        assertEquals("a fruit", definitionMap.value("apple"));
        assertEquals(
                "a yellow fruit that is related to <a href=\"apple.html\">apples</a>",
                definitionMap.value("banana"));
    }

    // Tests of addLinks

    /**
     * Tests that no new html files are created with empty map input.
     */
    @Test
    public void testAddLinksEmptyInput() {
        Map<String, String> definitionMap = new Map1L<>();
        Queue<String> terms = new Queue1L<>();
        String out = "test";
        Glossary.addLinks(definitionMap, terms, out);
        File directory = new File(out);
        assertTrue(directory.isDirectory());
        final int four = 4;
        assertEquals(four, directory.list().length);
    }

    /**
     * Tests that an html file is created for each term in the output directory.
     */
    @Test
    public void testAddLinksNonEmptyInput() {
        Map<String, String> definitionMap = new Map1L<>();
        definitionMap.add("apple", "a type of fruit");
        definitionMap.add("banana",
                "a long curved fruit that grows in clusters and has soft "
                        + "pulpy flesh and yellow skin when ripe");
        Queue<String> terms = new Queue1L<>();
        terms.enqueue("apple");
        terms.enqueue("banana");
        String out = "test";
        Glossary.addLinks(definitionMap, terms, out);
        File directory = new File(out);
        assertTrue(directory.isDirectory());
        final int four = 4;
        assertEquals(four, directory.list().length);
        assertTrue(new File(out + "/apple.html").isFile());
        assertTrue(new File(out + "/banana.html").isFile());
    }

    /**
     * Tests with duplicate terms.
     */
    @Test
    public void testAddLinksDuplicateTerms() {
        Map<String, String> definitionMap = new Map1L<>();
        definitionMap.add("apple", "a type of fruit");
        Queue<String> terms = new Queue1L<>();
        terms.enqueue("apple");
        terms.enqueue("apple");
        String out = "test";
        Glossary.addLinks(definitionMap, terms, out);
        File directory = new File(out);
        assertTrue(directory.isDirectory());
        final int four = 4;
        assertEquals(four, directory.list().length);
        assertTrue(new File(out + "/apple.html").isFile());
    }

    // Tests of createIndex

    /**
     * Tests with empty queue.
     */
    @Test
    public void testCreateIndexWithEmptyQueue() {
        Queue<String> emptyQueue = new Queue1L<>();
        SimpleWriter out = new SimpleWriter1L("test-output.html");
        Glossary.createIndex(emptyQueue, "test-output.html", out);
        out.close();

        File outputFile = new File("test-output.html");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.isFile());

        String expected = "<html>\n<head>\n<title>Glossary</title>\n</head>\n"
                + "<body>\n<h2>Glossary</h2>\n<hr />\n<h3>Index</h3>\n<ul>\n"
                + "</ul>\n</body>\n</html>\n";
        String actual = HtmlToStringForTests.htmlConvert(outputFile);
        assertEquals(expected, actual);
    }

    /**
     * Tests with single term.
     *
     */
    @Test
    public void testCreateIndexWithSingleTerm() {
        Queue<String> singleTermQueue = new Queue1L<>();
        singleTermQueue.enqueue("term1");
        SimpleWriter out = new SimpleWriter1L("test-output.html");
        Glossary.createIndex(singleTermQueue, "test-output.html", out);
        out.close();

        File outputFile = new File("test-output.html");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.isFile());

        String expected = "<html>\n<head>\n<title>Glossary</title>\n</head>\n"
                + "<body>\n<h2>Glossary</h2>\n<hr />\n<h3>Index</h3>\n<ul>\n"
                + "<li><a href=\"term1.html\">term1</a></li>\n</ul>\n</body>\n</html>\n";
        String actual = HtmlToStringForTests.htmlConvert(outputFile);
        assertEquals(expected, actual);
    }

    /**
     * Tests with multiple terms.
     */
    @Test
    public void testCreateIndexWithMultipleTerms() {
        Queue<String> multipleTermQueue = new Queue1L<>();
        multipleTermQueue.enqueue("term1");
        multipleTermQueue.enqueue("term2");
        multipleTermQueue.enqueue("term3");
        SimpleWriter out = new SimpleWriter1L("test-output.html");
        Glossary.createIndex(multipleTermQueue, "test-output.html", out);
        out.close();

        File outputFile = new File("test-output.html");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.isFile());

        String expected = "<html>\n<head>\n<title>Glossary</title>\n"
                + "</head>\n<body>\n<h2>Glossary</h2>\n<hr />\n<h3>Index</h3>\n<ul>\n"
                + "<li><a href=\"term1.html\">term1</a></li>\n<li>"
                + "<a href=\"term2.html\">term2</a></li>\n<li>"
                + "<a href=\"term3.html\">term3</a></li>\n</ul>\n</body>\n</html>\n";
        String actual = HtmlToStringForTests.htmlConvert(outputFile);
        assertEquals(expected, actual);
    }
}
