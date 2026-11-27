package com.library;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformancePlanTest {
    private static final List<String> SEARCH_PLANS = List.of(
        "performance/catalog-search.jmx",
        "performance/catalog-search-50-users.jmx",
        "performance/catalog-search-100-users.jmx",
        "performance/catalog-search-500-users-stress.jmx"
    );

    @Test void searchPlansUseLoginCookiesSearchEndpointAndSuccessAssertions() throws Exception {
        for (String plan : SEARCH_PLANS) {
            Document document = parse(plan);
            assertTrue(hasNamedElement(document, "HTTPSamplerProxy", "Login"));
            assertTrue(hasNamedElement(document, "HTTPSamplerProxy", "Catalog search"));
            assertTrue(hasElement(document, "CookieManager"));
            assertEquals(1, document.getElementsByTagName("CSVDataSet").getLength());
            Element csv = (Element) document.getElementsByTagName("CSVDataSet").item(0);
            assertEquals("false", property(csv, "recycle"));
            assertEquals("true", property(csv, "stopThread"));
            Element once = (Element) document.getElementsByTagName("OnceOnlyController").item(0);
            assertNotNull(once);
            Element onceTree = nextElementSibling(once);
            assertNotNull(onceTree);
            assertEquals(1, onceTree.getElementsByTagName("CSVDataSet").getLength());
            assertTrue(hasNamedElement(onceTree, "HTTPSamplerProxy", "Login"));
            assertTrue(hasProperty(document, "Argument.name", "q"));
            assertTrue(hasText(document, "${studentEmail}"));
            assertTrue(hasText(document, "${studentPassword}"));
            assertTrue(hasProperty(document, "Assertion.test_field", "Assertion.response_data"));
            assertTrue(hasText(document, "THE COLLECTION"));
            assertTrue(hasText(document, "Good to see you,"));
            assertEquals(2, document.getElementsByTagName("ResponseAssertion").getLength());
            assertTrue(hasElement(document, "ResultCollector"));
        }
    }

    @Test void circulationPlanChecksLoginCsrfBorrowAndReturnResponses() throws Exception {
        Document document = parse("performance/circulation-borrow-return.jmx");
        for (String sampler : List.of("Student login", "Student catalog / CSRF token", "Borrow assigned book",
                                      "Librarian login", "Librarian issues / CSRF token", "Return assigned overdue loan")) {
            assertTrue(hasNamedElement(document, "HTTPSamplerProxy", sampler), sampler);
        }
        assertTrue(hasElement(document, "CookieManager"));
        assertEquals(2, document.getElementsByTagName("RegexExtractor").getLength());
        assertEquals(6, document.getElementsByTagName("ResponseAssertion").getLength());
        Element csv = (Element) document.getElementsByTagName("CSVDataSet").item(0);
        assertEquals("false", property(csv, "recycle"));
        assertEquals("true", property(csv, "stopThread"));
        assertTrue(hasText(document, "/app/issues/borrow"));
        assertTrue(hasText(document, "/librarian/issues/return"));
    }

    private Document parse(String file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(Path.of(file).toFile());
    }

    private boolean hasElement(Document document, String tag) { return document.getElementsByTagName(tag).getLength() > 0; }
    private boolean hasNamedElement(Document document, String tag, String name) {
        NodeList nodes = document.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) if (name.equals(((Element) nodes.item(i)).getAttribute("testname"))) return true;
        return false;
    }
    private boolean hasNamedElement(Element element, String tag, String name) {
        NodeList nodes = element.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) if (name.equals(((Element) nodes.item(i)).getAttribute("testname"))) return true;
        return false;
    }
    private boolean hasNamedElement(Document document, String tag, String attribute, String value) {
        NodeList nodes = document.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) if (value.equals(((Element) nodes.item(i)).getAttribute(attribute))) return true;
        return false;
    }
    private boolean hasProperty(Document document, String propertyName, String value) {
        NodeList nodes = document.getElementsByTagName("stringProp");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element property = (Element) nodes.item(i);
            if (propertyName.equals(property.getAttribute("name")) && value.equals(property.getTextContent())) return true;
        }
        return false;
    }
    private Element nextElementSibling(Node node) {
        Node sibling = node.getNextSibling();
        while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) sibling = sibling.getNextSibling();
        return sibling instanceof Element element ? element : null;
    }
    private boolean hasText(Document document, String expected) { return document.getDocumentElement().getTextContent().contains(expected); }
    private String property(Element element, String name) {
        NodeList nodes = element.getElementsByTagName("boolProp");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (name.equals(((Element) node).getAttribute("name"))) return node.getTextContent();
        }
        return null;
    }
}
