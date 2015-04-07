import java.text.DecimalFormat;
import java.util.Iterator;

import org.jdom2.Element;

public class Filtro
{
	private static Element sf240m = null;
	private static String sf594a = null;
	private Organico oa, os;

	private static void err(String msg)
	{
		System.err.println(msg);
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

	public void leader(Element r)
	{
		System.err.println(r.getChildText("leader", null));
	}

	public void controlFields(Element r)
	{
		String tag = null;
		for(Element d : r.getChildren("controlfield", null))
		{
			tag = d.getAttributeValue("tag");
			tag = new DecimalFormat("000").format(Integer.parseInt(tag));
			System.err.println(tag + "  " + d.getText());
		}
	}

	public void dataFields(Element r)
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

	public void subFields(Element df)
	{
		String tag = df.getAttributeValue("tag");
		String code = null;

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
 * essere poi utilizzato insieme a quello analitico. Serve un reference
 * all'elemento, altrimenti le modifiche andrebbero perdute.
 */

				case "240$m":
					sf240m = sf;
					err("sf240m = " + sf240m.getText());
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
					oa = new Organico();
					oa.organico(sf);
					sf594a = sf.getText();

/*
 * Se 240$m e 594$a sono diversi, anche il 240$m tenuto da parte va elaborato
 * con le stesse regole con cui è stato appena elaborato il 594%a. In caso
 * contrario, ancora da gestire, il 240$m va ricavato contando voci e strumenti
 * del 594$a già elaborato
 */
					err("594$a = " + sf594a);
					err("240$m = " + sf240m.getText());
					os = new Organico();
					os.organico(sf240m);
					if(!sf240m.getText().equals(sf594a))
					{
						err("organico sintetico diverso da analitico");
					}
					else
					{
/*
 * in questo caso, conteggio voci e strumenti (e cori?) e mettere in 240$m
 */
						err("organico sintetico uguale ad analitico");
						int nVoci = os.getnVoci();
						int nStru = os.getnStru();
						err(os.getnVoci() + "V," + os.getnStru() + "stru");
						String osString = "";
						if(nVoci > 0)
						{
							osString = nVoci + "V";
							if(nStru > 0)
							{
								osString += "," + nStru + "stru";
							}
						}
						else if(nStru > 0)
						{
							osString = nStru + "stru";
						}
						sf240m.setText(osString);
					}
					err(sf.getText());
					break;

				default:
					break;
			}
		}
		System.err.println();
	}

	/*
	 * public static void main(String[] args) throws JDOMException, IOException {
	 * 
	 * // read the XML into a JDOM2 document. SAXBuilder jdomBuilder = new
	 * SAXBuilder(); Document jdomDocument = jdomBuilder.build(new
	 * FileReader(xmlSource));
	 * 
	 * // use the default implementation XPathFactory xFactory =
	 * XPathFactory.instance(); Element root = jdomDocument.getRootElement();
	 * Namespace zs = root.getNamespace("zs");
	 * 
	 * for(Element zsRecord : root.getChild("records", zs).getChildren("record",
	 * zs)) { Element zsRecordData = zsRecord.getChild("recordData", zs); Element
	 * record = zsRecordData.getChild("record", null); leader(record);
	 * controlFields(record); dataFields(record); } //
	 * System.out.println(def.getPrefix()); ArrayList<Namespace> nss = new
	 * ArrayList<Namespace>(); nss.add(zs); // nss.add(null); // nss.add(boh);
	 * XPathExpression<Element> expr = xFactory.compile("//record",
	 * Filters.element(), null, nss); List<Element> links =
	 * expr.evaluate(jdomDocument); int count = 0; for(Element linkElement :
	 * links) { System.out.println(++count + ": " + linkElement.getValue());
	 * linkElement.setText("Questo era un link: " + linkElement.getValue()); }
	 * 
	 * XMLOutputter xo = new XMLOutputter(); xo.output(jdomDocument, new
	 * PrintWriter("opac2.out.xml")); }
	 */

}
