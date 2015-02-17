import java.io.IOException;
import java.util.List;
 
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
  
public class XPathTest {
    private static String xmlSource = "http://feeds.bbci.co.uk/news/technology/rss.xml?edition=int";
  
    public static void main(String[] args) throws JDOMException, IOException {
  
        // read the XML into a JDOM2 document.
        SAXBuilder jdomBuilder = new SAXBuilder();
        Document jdomDocument = jdomBuilder.build(xmlSource);
  
        // use the default implementation
        XPathFactory xFactory = XPathFactory.instance();
        // System.out.println(xFactory.getClass());
  
        // select all links
        XPathExpression<Element> expr = xFactory.compile("//link", Filters.element());
        List<Element> links = expr.evaluate(jdomDocument);
        for (Element linkElement : links) {
            System.out.println(linkElement.getValue());
        }
  
        // select all links in image element
        expr = xFactory.compile("//image/link", Filters.element());
        List<Element> links2 = expr.evaluate(jdomDocument);
        for (Element linkElement : links2) {
            System.out.println(linkElement.getValue());
        }
  
        // get the media namespace
        Namespace media = jdomDocument.getRootElement().getNamespace("media");
        // find all thumbnail elements from the media namespace where the
        // attribute widht has a value > 60
        expr = xFactory.compile("//media:thumbnail[@width>60.00]", Filters.element(), null, media);
        // find the first element in the document and get its attribute named 'url'
        System.out.println(expr.evaluateFirst(jdomDocument).getAttributeValue("url"));
                 
  
        // find the child element of channel whose name is title. find the
        // descendant of item with name title.
        Element firstTitle = xFactory.compile("//channel/child::item/descendant::title", Filters.element()).evaluateFirst(jdomDocument);
        System.out.println(firstTitle.getValue());
  
    }
}
  