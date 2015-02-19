import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
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

	private static void err(String msg)
	{
		System.err.println(msg);
	}

	private static void organico(Element s)
	{
		String temp = s.getText();
		Hashtable<String, Integer> coroHt, vociHt, struHt;
		coroHt = new Hashtable<String, Integer>();
		vociHt = new Hashtable<String, Integer>();
		struHt = new Hashtable<String, Integer>();
		p = Pattern.compile("([a-z]+) ([0-9]),");
		m = p.matcher(temp);
		String stru = null, lastStru = null;
		String count = null;
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
		boolean voce = false;

		err("");
		for(String e : eList)
		{
			e = e.trim();
// err(e);
			if(e.startsWith("Coro "))
			{
				coro = true;
				e = e.replace("Coro ", "");
			}
			if(e.matches("^[A-Z]+.*"))
			{
				voce = true;
			}
			if(e.contains("("))
			{
				int lp = e.indexOf("(");
				int rp = e.indexOf(")");
				count = e.substring(lp + 1, rp);
				stru = e.substring(0, lp).trim();
				try
				{
					if(coro)
					{
						coroHt.put(stru, Integer.parseInt(count));
					}
					else if(voce)
					{
						vociHt.put(stru, Integer.parseInt(count));
					}
					else
					{
						struHt.put(stru, Integer.parseInt(count));
					}
				}
				catch(NumberFormatException ee)
				{
					err("Quantificazione non valida: " + e);
				}
				e = count + stru;
				lastStru = stru;
			}
			else if(e.matches(".* [0-9].*"))
			{
// err(".* [0-9]+ => " + e);
				String[] temp2 = e.split(" ");
				stru = temp2[0];
				count = temp2[1];
				try
				{
					if(coro)
					{
						coroHt.put(stru, Integer.parseInt(count));
					}
					else if(voce)
					{
						vociHt.put(stru, Integer.parseInt(count));
					}
					else
					{
						struHt.put(stru, Integer.parseInt(count));
					}
				}
				catch(NumberFormatException ee)
				{
					System.err.println("Quantificazione non valida: " + e);
				}
				e = count + stru;
				lastStru = stru;
			}
			else if(e.matches("[0-9]+"))
			{
// err("[0-9]+ => " + e);
				if(lastStru != null && struHt.containsKey(lastStru))
				{
					int cc = struHt.get(lastStru).intValue();
					if(coro)
					{
						coroHt.put(lastStru, +cc);
					}
					else if(voce)
					{
						vociHt.put(lastStru, +cc);
					}
					else
					{
						struHt.put(lastStru, ++cc);
					}
				}
				else
				{
					err("Non so che fare con questo token: [" + e + "]");
				}
			}
			else
			{
				stru = e;
				if(coro)
				{
					if(coroHt.containsKey(stru))
					{
						int cc = coroHt.get(stru).intValue();
						coroHt.put(stru, +cc);
					}
					else
					{
						coroHt.put(stru, 1);
					}
				}
				else if(voce)
				{
					if(vociHt.containsKey(stru))
					{
						int cc = vociHt.get(stru).intValue();
						vociHt.put(stru, +cc);
					}
					else
					{
						vociHt.put(stru, 1);
					}
				}
				else
				{
					if(struHt.containsKey(stru))
					{
						int cc = struHt.get(stru).intValue();
						struHt.put(stru, +cc);
					}
					else
					{
						struHt.put(stru, 1);
					}
				}
			}
			coro = false;
			voce = false;
			r += e + ",";
		}
// r = r.replaceAll("([a-zA-Z]+) [0-9],\\1 ([0-9])", "\1 (\2)");
// r = r.replaceAll(",tamb", ",\\1 uro");

		Enumeration<String> keys;
		err("\nStrumenti");
		r = "";
		String key;
		Integer value;
		keys = struHt.keys();
		while(keys.hasMoreElements())
		{
			key = keys.nextElement();
			value = struHt.get(key);
			err(key + " => " + struHt.get(key));
			if(value.intValue() > 1)
			{
				r += value + key + ",";
			}
			else
			{
				r += key + ",";
			}
		}
		err("Coro");
		keys = coroHt.keys();
		if(keys.hasMoreElements())
		{
			r += "Coro(";
			while(keys.hasMoreElements())
			{
				key = keys.nextElement();
				value = coroHt.get(key);
				err(key + " => " + coroHt.get(key));
				r += value + key + ",";
			}
			r = r.substring(0, r.length() - 1) + "),";
		}
		err("Voci");
		keys = vociHt.keys();
		while(keys.hasMoreElements())
		{
			key = keys.nextElement();
			value = vociHt.get(key);
			err(key + " => " + vociHt.get(key));
			r += value + key + ",";
		}
		err(r);
		if(r.length() > 0)
		{
			s.setText(r.substring(0, r.length() - 1));
			err(s.getText());
		}
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
