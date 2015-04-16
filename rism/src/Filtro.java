import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;

public class Filtro
{
	private static Element sf240m = null;
	private static String sf594a = null;
	private Organico oa, os;
	private String badOSf = "z";

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

	public String leader(Element r)
	{
		return r.getChildText("leader", null);
	}

	public void controlFields(Element r)
	{
		String tag = null;
		Log.info("Inizio elaborazione dei control field");
		for(Element d : r.getChildren("controlfield", null))
		{
			tag = d.getAttributeValue("tag");
			tag = new DecimalFormat("000").format(Integer.parseInt(tag));
			System.err.println(tag + "  " + d.getText());
		}
		Log.info("Fine elaborazione dei control field");
	}

	public void dataFields(Element r)
	{
		String tag = null;
		String ind = null;
		Log.info("Inizio elaborazione dei data field");
		for(Element df : r.getChildren("datafield", null))
		{
			tag = df.getAttributeValue("tag");
			tag = new DecimalFormat("000").format(Integer.parseInt(tag));
			Log.info("Datafield " + tag);
			ind = df.getAttributeValue("ind1");
			ind += df.getAttributeValue("ind2");
			Log.debug(ind);
			subFields(df);
		}
		Log.info("Fine elaborazione dei data field");
	}

	public void subFields(Element df)
	{
		String tag = df.getAttributeValue("tag");
		String code = null;
		Log.info("Inizio elaborazione dei subfield");

/*
 * Serve un clone del df su cui iterare e il cui sotto albero non sarà
 * modificato, anche se i singoli valori di elementi e attributi potrebbero
 * essere modificati senza problemi. Il problema nasce se si aggiunge o toglie
 * un elemento, perché questo altera la lista di figli su cui si sta iterando.
 * Per questo motivo non si può usare un iteratore, ma una lista vera e propria,
 * che si può scorrere ma anche incrementare senza problemi. L'iteratore provoca
 * invece un'eccezione se la lista sottostante viene modificata, almeno in certi
 * casi
 */
		Element dfClone = df.clone();
		Element sf;
		List<Element> sfList = df.getChildren();
		int i = 0;
		for(Element sfClone : dfClone.getChildren())
		{
			sf = sfList.get(i++);
			code = tag + "$" + sfClone.getAttributeValue("code");
			Log.info("Subfield " + code);
			Log.debug(sfClone.getText());
			switch(code)
			{
				case "100$a":
					if(!sfClone.getText().contains(","))
					{
						df.setAttribute("ind1", "0");
					}
					break;

				case "700$a":
					if(!sfClone.getText().contains(","))
					{
						df.setAttribute("ind1", "0");
					}

				case "031$r":
					sf.setText(sfClone.getText().replace("|", ""));
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
					Log.debug("sf240m = " + sf240m.getText());
					break;

				case "240$r":
					sf.setText(sfClone.getText().replace("|", ""));
					break;

/*
 * Per gestire meglio le datazioni con strumenti MARC, il 260$c originale viene
 * conservato, e viene creato un 260$x fittizio con i valori prodotti dal metodo
 * dateX() e un 260$y popolato dal metdo dateY()
 */
				case "260$c":
					Element s2 = new Element("subfield", df.getNamespace()).setAttribute("code", "x");
					Data d = new Data(sfClone.getText());
					s2.addContent(d.getX());
					df.addContent(s2);
					s2 = new Element("subfield", df.getNamespace()).setAttribute("code", "y");
					s2.addContent(d.getY());
					df.addContent(s2);
					break;

/*
 * Partiture. Si lascia inalterato il sottocampo, ma se ne aggiungono due
 * fittizi per facilitare l'ulteriore elaborazione con usemarcon
 */

				case "300$a":
					Element s4 = new Element("subfield", df.getNamespace()).setAttribute("code", "x");
					Partitura p = new Partitura(sfClone.getText());
					Log.debug("x = " + p.getX() + ", y = " + p.getY());
					if(p.getX() != "")
					{
						s4.addContent(p.getX());
						df.addContent(s4);
					}
					if(p.getY() != "")
					{
						s4 = new Element("subfield", df.getNamespace()).setAttribute("code", "y");
						s4.addContent(p.getY());
						df.addContent(s4);
					}
					break;

				case "594$a":
					sf594a = sfClone.getText();
					if(sf594a == null)
						Log.warn("594$a nullo");
					else
						Log.debug("-->" + sf594a);
					if(sf240m == null) Log.warn("240$m nullo");
					oa = new Organico();
					try
					{
						oa.organico(sf);
					}

/*
 * Il caso di indice fuori scala è quasi certamente segno dell'impossibilità di
 * elaborare un organico secondo le regole fin qui implementate. Si crea allora
 * un 596$z fittizio per evidenziare questo problema. Per sicurezza, si usa un
 * stringa configurabile, piuttosto che la "z" già codificata nel codice
 */

					catch(StringIndexOutOfBoundsException e)
					{
						Log.error(e.getMessage());
						Element s3 = new Element("subfield", df.getNamespace()).setAttribute("code", badOSf);
						s3.setText("1");
						df.addContent(s3);
					}
					sf594a = sf.getText();

/*
 * Se 240$m e 594$a sono diversi, anche il 240$m tenuto da parte va elaborato
 * con le stesse regole con cui è stato appena elaborato il 594%a. In caso
 * contrario, ancora da gestire, il 240$m va ricavato contando voci e strumenti
 * del 594$a già elaborato
 */
					Log.debug("594$a = " + sf594a);
					Log.debug("240$m = " + sf240m.getText());
					os = new Organico();
					os.organico(sf240m);
					if(!sf240m.getText().equals(sf594a))
					{
						Log.debug("organico sintetico diverso da analitico");
					}
					else
					{
/*
 * in questo caso, conteggio voci e strumenti (e cori?) e mettere in 240$m
 */
						Log.debug("organico sintetico uguale ad analitico");
						int nVoci = os.getnVoci();
						int nStru = os.getnStru();
						Log.debug(os.getnVoci() + "V," + os.getnStru() + "str");
						String osString = "";
						if(nVoci > 0)
						{
							osString = nVoci + "V";
							if(nStru > 0)
							{
								osString += "," + nStru + "str";
							}
						}
						else if(nStru > 0)
						{
							osString = nStru + "str";
						}
						sf240m.setText(osString);
					}
					Log.debug(sf.getText());
					break;

				default:
					break;
			}
		}
		System.err.println();
		Log.info("Fine elaborazione dei subfield");
	}
}
