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
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class Rism
{
	public static void main(String[] args) throws JDOMException, IOException
	{
		String xmlSource = "input/opac2.xml";
		if(args.length > 0)
		{
			xmlSource = args[0];
		}
		new Log();
		try
		{
			Log.init(xmlSource + ".log");
			Log.info("Elaborazione file_:" + xmlSource);
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
				String cf001 = record.getChild("controlfield", null).getText();
				Log.info("Elaborazione record " + cf001);
				Filtro f = new Filtro();
// f.leader(record);
				f.controlFields(record);
				f.dataFields(record);
			}
			ArrayList<Namespace> nss = new ArrayList<Namespace>();
			nss.add(zs);
			XPathExpression<Element> expr = xFactory.compile("//record", Filters.element(), null, nss);
			List<Element> links = expr.evaluate(jdomDocument);
			int count = 0;
			for(Element linkElement : links)
			{
				Log.info(++count + ": " + linkElement.getValue());
				linkElement.setText("Questo era un link: " + linkElement.getValue());
			}
			XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			String xmlTarget = xmlSource.substring(0, xmlSource.indexOf(".xml"));
			xmlTarget += ".out.xml";
			xo.output(jdomDocument, new PrintWriter(xmlTarget));
		}
		catch(IOException e)
		{
			System.err.println("Problemi con il file di configurazione: " + e.getMessage());
		}
	}
}
