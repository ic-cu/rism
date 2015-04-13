/*
 * Classe per la conversione delle date RISM in formato SBNMARC
 */

public class Data
{
	private String xDate, yDate;
	private final String sx = "/";
	private final String sy = ".";

	public String getX()
	{
		return xDate;
	}

	public String getY()
	{
		return "[" + yDate + "]";
	}

	/*
	 * Il costruttore cerca di interpretare la data in input e mette via dei dati
	 * che poi saranno estratti da un paio di getter
	 */
	public Data(String r)
	{
		String aDate, bDate, ss;
		int nn, mm, ii, jj;

/*
 * Consideriamo solo campi contenenti parentesi. In caso contrario tutto resta
 * com'è, salvo il caso di intervalli. Ovviamente la prima cosa da fare è
 * trovare la posizione delle parentesi aperta e chiusa.
 */

		if(r.contains("("))
		{
			int lp = r.indexOf("(");
			int rp = r.indexOf(")");

// La prima parte della data esclude lo spazio prima della parentesi aperta.

			aDate = r.substring(0, lp - 1);
			bDate = r.substring(lp + 1, rp);
			Log.debug("\naDate = [" + aDate + "], bDate = [" + bDate + "]");

// Se le due date sono uguali, si imposta questo valore.

			if(aDate.equals(bDate))
			{
				xDate = new String(aDate);
				yDate = new String(aDate);
			}

// Se sono diverse e bDate contiene (c,?,.), il campo diventa bDate, ma
// escludendo l'eventuale "?" iniziale.

			else if(bDate.startsWith("?"))
			{
				xDate = new String(bDate.substring(1));
				yDate = new String(xDate);
			}

/*
 * La data fra parentesi è divisa in NN e ss da un punto. La parte ss ha una
 * serie di significati da sciogliere e abbinare alla parte NN
 */
			else if(bDate.contains("."))
			{
				nn = Integer.parseInt(bDate.substring(0, bDate.indexOf(".")));
				ss = bDate.substring(bDate.indexOf(".") + 1);
				Log.debug("cDate = " + nn + ", dDate = " + ss);
				switch(ss)
				{
					case "sc":
						xDate = nn + sx + "t";
						yDate = nn + sy + " sec.";
						break;
					case "in":
						xDate = nn + sx + "i";
						yDate = "inizio " + nn + sy + " sec.";
						break;
					case "1d":
						xDate = nn + sx + "p";
						yDate = "1. metà " + nn + sy + " sec.";
						break;
					case "2d":
						xDate = nn + sx + "s";
						yDate = "2. metà " + nn + sy + " sec.";
						break;
					case "me":
						xDate = nn + sx + "m";
						yDate = "metà " + nn + sy + " sec.";
						break;
					case "ex":
						xDate = nn + sx + "f";
						yDate = "fine " + nn + sy + " sec.";
						break;
					case "1q":
						xDate = nn + sx + "i";
						yDate = "1. quarto " + nn + sy + " sec.";
						break;
					case "2q":
						xDate = nn + sx + "p";
						yDate = "2. quarto " + nn + sy + " sec.";
						break;
					case "3q":
						xDate = nn + sx + "s";
						yDate = "3. quarto " + nn + sy + " sec.";
						break;
					case "4q":
						xDate = nn + sx + "f";
						yDate = "ultimo quarto " + nn + sy + " sec.";
						break;

					default:
						break;
				}
			}

/*
 * Se la parte fra parentesi contiene "/", l'elaborazione è molto semplice. Le
 * due parti sono numeriche e in genere diverse
 */
			else if(bDate.contains("/"))
			{
				nn = Integer.parseInt(bDate.substring(0, bDate.indexOf("/")));
				mm = Integer.parseInt(bDate.substring(bDate.indexOf("/") + 1));
				xDate = nn + "/" + mm;
				yDate = "sec. " + nn + sy + "-" + mm + sy;
			}

/*
 * Ultimo caso entro parentesi: c'è il trattino, in teoria sempre insieme alla
 * "c" in fondo ai due token tipo YYYY. substring deve cercare di evitare la "c"
 */
			else if(bDate.contains("-"))
			{
				nn = Integer.parseInt(bDate.substring(0, bDate.indexOf("-") - 1));
				Log.debug(bDate.substring(bDate.indexOf("-") + 1, bDate.length() - 2));
				mm = Integer.parseInt(bDate.substring(bDate.indexOf("-") + 1, bDate.length() - 2));
				xDate = nn + "-" + mm;
				yDate = "circa " + nn + "-" + mm;
			}

/*
 * In teoria qui restano solo i casi (NNNNx), con vari valori di x
 */

			else
			{
				nn = Integer.parseInt(bDate.substring(0, bDate.length() - 1));
				ss = bDate.charAt(bDate.length() - 1) + "";
				Log.debug("NNNNx: nn = " + nn + ", ss = " + ss);
				switch(ss)
				{
					case "c":
						xDate = nn + "c";
						yDate = "circa " + nn;
						break;
					case "p":
						xDate = nn + "p";
						yDate = "dopo il " + nn;
						break;
					case "a":
						xDate = nn + "a";
						yDate = "prima del " + nn;
						break;
					default:
						break;
				}
			}
		}

/*
 * Non c'è parentesi, ma c'è trattino: serve una elaborazione specifica
 */

		else if(r.contains("-"))
		{
			aDate = r.substring(0, r.indexOf("-"));
			bDate = r.substring(r.indexOf("-") + 1);
			Log.debug("Data sorgente: " + r + ", aDate = " + aDate + ", bDate = " + bDate);

/*
 * Per sicurezza controlliamo che le due parti divise dal trattino inizino con
 * la stessa coppia di cifre, perché in output si estrae un solo valore numerico
 */

			Log.debug("Prime due cifre: " + aDate.substring(0, 2));
			if(aDate.substring(0, 2).equals(bDate.substring(0, 2)))
			{
				nn = Integer.parseInt(aDate.substring(0, 2));
				ii = Integer.parseInt(aDate.substring(2, 4));
				jj = Integer.parseInt(bDate.substring(2, 4));

/*
 * Tutti i tre casi da gestire prevedono l'incremento della parte NN
 */

				nn++;
				if(ii == 0 && jj == 49)
				{
					xDate = nn + sx + "p";
					yDate = "prima metà " + nn + sy + " sec.";
				}
				if(ii == 0 && jj == 99)
				{
					xDate = nn + sx + "t";
					yDate = nn + sy + " sec.";
				}
				if(ii == 50 && jj == 99)
				{
					xDate = nn + sx + "s";
					yDate = "seconda metà " + nn + sy + " sec.";
				}
			}
		}

/*
 * Non c'è parentesi, né trattino: $x e $y dovranno essere uguali alla stringa
 * in input
 */
		else
		{
			xDate = r;
			yDate = r;
		}

	}
}
