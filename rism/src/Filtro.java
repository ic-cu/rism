import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

	private static void err(String msg)
	{
		System.err.println(msg);
	}

	private static void organico(Element s)
	{
		String temp = s.getText();
		Hashtable<String, Integer> ch, vh, sh;
		Queue<String> cq, vq, sq;
		ch = new Hashtable<String, Integer>();
		vh = new Hashtable<String, Integer>();
		sh = new Hashtable<String, Integer>();
		cq = new LinkedList<String>();
		vq = new LinkedList<String>();
		sq = new LinkedList<String>();
		String item = null, lastStru = null;
		String count = null;

		String[] eList = temp.split(",");
		String r = new String();

// i cori richiedono una gestione delicata

		boolean coro = false;
		boolean voce = false;

		err("");
		for(String e : eList)
		{
			e = e.trim();
			err(e);
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
				item = e.substring(0, lp).trim();
				try
				{
					if(coro)
					{
						ch.put(item, Integer.parseInt(count));
						cq.add(item);
					}
					else if(voce)
					{
						vh.put(item, Integer.parseInt(count));
						vq.add(item);
					}
					else
					{
						sh.put(item, Integer.parseInt(count));
						sq.add(item);
					}
				}
				catch(NumberFormatException ee)
				{
					err("Quantificazione non valida: " + e);
				}
				e = count + item;
				lastStru = item;
			}
			else if(e.matches(".* [0-9].*"))
			{
// err(".* [0-9]+ => " + e);
				String[] temp2 = e.split(" ");
				item = temp2[0];
				count = temp2[1];
				try
				{
					if(coro)
					{
						ch.put(item, Integer.parseInt(count));
						cq.add(item);
					}
					else if(voce)
					{
						vh.put(item, Integer.parseInt(count));
						vq.add(item);
					}
					else
					{
						sh.put(item, Integer.parseInt(count));
						sq.add(item);
					}
				}
				catch(NumberFormatException ee)
				{
					System.err.println("Quantificazione non valida: " + e);
				}
				e = count + item;
				lastStru = item;
			}
			else if(e.matches("[0-9]+"))
			{
// err("[0-9]+ => " + e);
				if(lastStru != null && sh.containsKey(lastStru))
				{
					int cc = sh.get(lastStru).intValue();
					if(coro)
					{
						ch.put(lastStru, +cc);
						cq.add(lastStru);
					}
					else if(voce)
					{
						vh.put(lastStru, +cc);
						vq.add(lastStru);
					}
					else
					{
						sh.put(lastStru, ++cc);
						sq.add(lastStru);
					}
				}
				else
				{
					err("Non so che fare con questo token: [" + e + "]");
				}
			}
			else
			{
				item = e;
				if(coro)
				{
					if(ch.containsKey(item))
					{
						int cc = ch.get(item).intValue();
						ch.put(item, +cc);
					}
					else
					{
						ch.put(item, 1);
						cq.add(item);
					}
				}
				else if(voce)
				{
					if(vh.containsKey(item))
					{
						int cc = vh.get(item).intValue();
						vh.put(item, +cc);
					}
					else
					{
						vh.put(item, 1);
						vq.add(item);
					}
				}
				else
				{
					if(sh.containsKey(item))
					{
						int cc = sh.get(item).intValue();
						sh.put(item, +cc);
					}
					else
					{
						sh.put(item, 1);
						sq.add(item);
					}
				}
			}
			coro = false;
			voce = false;
			r += e + ",";
		}

		r = "";
		String key;
		Integer val;
		err("\nVoci");
		int nVoci = 0;
		while(vq.peek() != null)
		{
			key = vq.poll();
			val = vh.get(key);
			err(key + " => " + vh.get(key));
			if(val.intValue() > 1)
			{
				r += val + key + ",";
			}
			else
			{
				r += key + ",";
			}
			nVoci += val.intValue();
		}

		err("Coro");
		if(cq.peek() != null)
		{
			r += "Coro(";
			while(cq.peek() != null)
			{
				key = cq.poll();
				val = ch.get(key);
				err(key + " => " + ch.get(key));
				if(val.intValue() > 1)
				{
					r += val + key + ",";
				}
				else
				{
					r += key + ",";
				}
			}
			r = r.substring(0, r.length() - 1) + "),";
		}

		err("Strumenti");
		int nStru = 0;
		while(sq.peek() != null)
		{
			key = sq.poll();
			val = sh.get(key);
			err(key + " => " + sh.get(key));
			if(val.intValue() > 1)
			{
				r += val + key + ",";
			}
			else
			{
				r += key + ",";
			}
			nStru += val.intValue();
		}

		err(r);
		if(r.length() > 0)
		{
			s.setText(r.substring(0, r.length() - 1));
			err(s.getText());
		}
		if(nVoci * nStru > 0)
		{
		}
		else if(nVoci > 0)
		{
		}
	}

	private static String dateX(Element s)
	{
		String r = s.getText();
		String aDate, bDate, rDate = null;

/*
 * Consideriamo solo campi contenenti parentesi. In caso contrario tutto resta
 * com'è. Ovviamente la prima cosa da fare è trovare la posizione delle
 * parentesi aperta e chiusa.
 */

		if(r.contains("("))
		{
			int lp = r.indexOf("(");
			int rp = r.indexOf(")");

// La prima parte della data esclude lo spazio prima della parentesi aperta.

			aDate = r.substring(0, lp - 1);
			bDate = r.substring(lp + 1, rp);
			err("\naDate = [" + aDate + "], bDate = [" + bDate + "]");

// Se le due date sono uguali, si imposta questo valore.

			if(aDate.equals(bDate))
			{
				rDate = new String(aDate);
			}

// Se sono diverse e bDate contiene (c,?,.), il campo diventa bDate, ma
// escludendo l'eventuale "?" iniziale.

			else if(bDate.startsWith("?"))
			{
				rDate = new String(bDate.substring(1));
			}
			else
			{
				rDate = new String(bDate);
			}
		}
		return rDate;
	}

	private static String dateY(Element s)
	{
		String r = s.getText();
		String aDate, bDate, rDate = null;

/*
 * Consideriamo solo campi contenenti parentesi. In caso contrario tutto resta
 * com'è. Ovviamente la prima cosa da fare è trovare la posizione delle
 * parentesi aperta e chiusa.
 */

		if(r.contains("("))
		{
			int lp = r.indexOf("(");
			int rp = r.indexOf(")");

// La prima parte della data esclude lo spazio prima della parentesi aperta.

			aDate = r.substring(0, lp - 1);
			bDate = r.substring(lp + 1, rp);
			err("\naDate = [" + aDate + "], bDate = [" + bDate + "]");

// Se le due date sono uguali, si imposta questo valore.

			if(aDate.equals(bDate))
			{
				rDate = new String(aDate);
			}

// Se sono diverse e bDate contiene (c,?,.), il campo diventa bDate, ma
// escludendo l'eventuale "?" iniziale.

			else if(bDate.startsWith("?"))
			{
				rDate = new String(bDate.substring(1));
			}
			else
			{
				rDate = new String(bDate);
			}
		}
		return rDate;
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
		for(Element df : r.getChildren("datafield", null))
		{
			tag = df.getAttributeValue("tag");
			tag = new DecimalFormat("000").format(Integer.parseInt(tag));
			ind = df.getAttributeValue("ind1");
			ind += df.getAttributeValue("ind2");
			err(tag + ind);
			subFields(df);
		}
	}

	private static void subFields(Element df)
	{
		String tag = df.getAttributeValue("tag");
		String code = null;
		String sf240m = "";
		String sf594a = null;

/*
 * Serve un clone del df su cui iterare e il cui sotto albero non sarà
 * modificato, anche se i singoli valori di elementi e attributi potrebbero
 * essere modificati senza problemi. Il problema nasce se si aggiunge o toglie
 * un elemento, perché questo altera la lista di figli su cui si sta iterando
 */
		Element df2 = df.clone();
		Element sf;
		Iterator<Element> dfcIter = df.getChildren().iterator();
		for(Element sf2 : df2.getChildren())
		{
			sf = dfcIter.next();
			System.err.print("$" + sf2.getAttributeValue("code") + sf2.getText());
			code = tag + "$" + sf2.getAttributeValue("code");
			switch(code)
			{
				case "100$a":
					if(!sf2.getText().contains(","))
					{
						df.setAttribute("ind1", "0");
					}
					break;

				case "700$a":
					if(!sf2.getText().contains(","))
					{
						df.setAttribute("ind1", "0");
					}
					break;

				case "031$r":
					sf.setText(sf2.getText().replace("|", ""));
					break;

				case "240$a":
					nonSort(sf);
					break;

/*
 * L'organico sintetico, che viene sempre per primo (si spera), si mette via per
 * essere poi utilizzato insieme a quello analitico
 */

				case "240$m":
					sf240m = sf2.getText();
					break;

				case "240$r":
					sf.setText(sf2.getText().replace("|", ""));
					break;

/*
 * Per gestire meglio le datazioni con strumenti MARC, il 260$c originale viene
 * conservato, e viene creato un 260$x fittizio con i valori prodotti dal metodo
 * date()
 */
				case "260$c":
					Element s2 = new Element("subfield", df.getNamespace()).setAttribute("code", "x");
					s2.addContent(dateX(sf2));
					df.addContent(s2);
					s2 = new Element("subfield", df.getNamespace()).setAttribute("code", "y");
					s2.addContent(dateY(sf2));
					df.addContent(s2);
					break;

				case "594$a":
					sf594a = sf2.getText();
					if(sf594a == null)
						err("594$a nullo");
					else
						err("-->" + sf594a);
					if(sf240m == null) err("240$m nullo");
					organico(sf);

/*
 * Se 240$m e 594$a sono diversi, il primo si deve ricavare dal secondo (o era
 * in caso contrario?)
 */

					if(!sf240m.equals(sf594a))
					{
// organico(sf);
					}
					err(sf.getText());
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

		for(Element zsRecord : root.getChild("records", zs).getChildren("record", zs))
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
