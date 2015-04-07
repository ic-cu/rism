import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;


public class Rism
{
	public static void main(String[] args) throws JDOMException, IOException
	{

		String xmlSource = "input/opac2.xml";

		// read the XML into a JDOM2 document.
		SAXBuilder jdomBuilder = new SAXBuilder();
		Document jdomDocument = jdomBuilder.build(new FileReader(xmlSource));

		// use the default implementation
		XPathFactory xFactory = XPathFactory.instance();
		Element root = jdomDocument.getRootElement();
		Namespace zs = root.getNamespace("zs");

		for(Element zsRecord : root.getChild("records", zs).getChildren("record", zs))
		{
			Element zsRecordData = zsRecord.getChild("recordData", zs);
			Element record = zsRecordData.getChild("record", null);
			Filtro f = new Filtro();
			f.leader(record);
			f.controlFields(record);
			f.dataFields(record);
		}
// System.out.println(def.getPrefix());
		ArrayList<Namespace> nss = new ArrayList<Namespace>();
		nss.add(zs);
// nss.add(null);
// nss.add(boh);
		XPathExpression<Element> expr = xFactory.compile("//record", Filters.element(), null, nss);
		List<Element> links = expr.evaluate(jdomDocument);
		int count = 0;
		for(Element linkElement : links)
		{
			System.out.println(++count + ": " + linkElement.getValue());
			linkElement.setText("Questo era un link: " + linkElement.getValue());
		}

		XMLOutputter xo = new XMLOutputter();
		xo.output(jdomDocument, new PrintWriter("opac2.out.xml"));
	}

}
