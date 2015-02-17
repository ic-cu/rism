import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class Filtro
{
	private static String xmlSource = "input/opac2.xml";
	private static Matcher m = null;
	private static Pattern p = null;

	private static void organico(Element s)
	{
		String temp = s.getText();
		p = Pattern.compile("([a-z]+) ([0-9]),");
		m = p.matcher(temp);
		String stru = null;
		int struCount = 1;
		if(m.find())
		{
			stru = m.group(1);
			p = Pattern.compile(stru);
			m = p.matcher(temp);
			while(m.find())
			{
				struCount++;
			}
			temp.replaceAll(stru + " [0-9],", struCount + stru);
		}

// intervento preliminare, [stru 1, 2, 3,..., n] => [stru (n)]"

// temp = temp.replaceAll("([a-z]+)", "\\1 ");
// temp = temp.replaceAll("([0-9], )+([0-9], )", "(\\2),");

// altro intervento preliminare, [stru 1, stru 2,..., stru n] => [stru n]"

// temp = temp.replaceAll("([a-zA-Z]+) [0-9], vl ([0-9])", "zzzz\\1 (\\2)");

// si divide la stringa alle virgole

		String[] eList = temp.split(",");
		String r = new String();

// i cori richiedono una gestione delicata

		boolean coro = false;

		for(String e : eList)
		{
			e = e.trim();
			if(e.startsWith("Coro "))
			{
				if(!coro)
				{
					coro = true;
					r += "Coro(";
				}
				e = e.replace("Coro ", "");
			}
			else if(coro)
			{
				coro = false;
				r = r.substring(0, r.length() - 1) + "),";
			}
			if(e.contains("("))
			{
				int lp = e.indexOf("(");
				int rp = e.indexOf(")");
				String count = e.substring(lp + 1, rp);
				String ex = e.substring(0, lp).trim();
				e = count + ex;
			}
			r += e + ",";
		}
// r = r.replaceAll("([a-zA-Z]+) [0-9],\\1 ([0-9])", "\1 (\2)");
// r = r.replaceAll(",tamb", ",\\1 uro");

		s.setText(r.substring(0, r.length() - 1));
	}

	private static void nonSort(Element s)
	{
		String r = s.getText();
		if(r.startsWith("La "))
		{
			r = r.replaceFirst("La", 0xc288 + "La" + 0xc289);
			System.err.println(r);
			s.setText(r);
		}
	}

	private static void leader(Element r)
	{
		System.err.println(r.getChildText("leader", null));
	}

	private static void controlFields(Element r)
	{
		String tag = null;
		for(Element d : r.getChildren("controlfield", null))
		{
			tag = d.getAttributeValue("tag");
			tag = new DecimalFormat("000").format(Integer.parseInt(tag));
			System.err.println(tag + "  " + d.getText());
		}
	}

	private static void dataFields(Element r)
	{
		String tag = null;
		String ind = null;
		for(Element d : r.getChildren("datafield", null))
		{
			tag = d.getAttributeValue("tag");
			tag = new DecimalFormat("000").format(Integer.parseInt(tag));
			ind = d.getAttributeValue("ind1");
			ind += d.getAttributeValue("ind2");
			System.err.print(tag + ind);
			subFields(d);
		}
	}

	private static void subFields(Element d)
	{
		String tag = d.getAttributeValue("tag");
		String field = null;
		for(Element s : d.getChildren())
		{
			System.err.print("$" + s.getAttributeValue("code") + s.getText());
			field = tag + "$" + s.getAttributeValue("code");
			switch(field)
			{
				case "100$a":
					if(!s.getText().contains(","))
					{
						d.setAttribute("ind1", "0");
					}
					break;

				case "700$a":
					if(!s.getText().contains(","))
					{
						d.setAttribute("ind1", "0");
					}
					break;

				case "031$r":
					s.setText(s.getText().replace("|", ""));
					break;

				case "240$r":
					s.setText(s.getText().replace("|", ""));
					break;

				case "240$m":
					organico(s);
					break;

				case "594$a":
					organico(s);
					break;

				case "240$a":
					nonSort(s);
					break;

				default:
					break;
			}
		}
		System.err.println();
	}

	public static void main(String[] args) throws JDOMException, IOException
	{

		// read the XML into a JDOM2 document.
		SAXBuilder jdomBuilder = new SAXBuilder();
		Document jdomDocument = jdomBuilder.build(new FileReader(xmlSource));

		// use the default implementation
		XPathFactory xFactory = XPathFactory.instance();
		Element root = jdomDocument.getRootElement();
		Namespace zs = root.getNamespace("zs");

		for(Element zsRecord : root.getChild("records", zs).getChildren("record",
				zs))
		{
			Element zsRecordData = zsRecord.getChild("recordData", zs);
			Element record = zsRecordData.getChild("record", null);
			leader(record);
			controlFields(record);
			dataFields(record);
		}
// System.out.println(def.getPrefix());
		ArrayList<Namespace> nss = new ArrayList<Namespace>();
		nss.add(zs);
// nss.add(null);
// nss.add(boh);
		XPathExpression<Element> expr = xFactory.compile("//record",
				Filters.element(), null, nss);
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
