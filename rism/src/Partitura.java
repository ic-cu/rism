public class Partitura
{
	private String x, y;
	private boolean score = false;

	public Partitura(String p)
	{
		String q = "", r = "";
		x = y = "";

/*
 * Si mette via l'eventuale parte dopo ":". Di quello che rimane, si mette via
 * l'eventuale parte fra parentesi (comprese le stesse parentesi), che va
 * riutilizzata dopo aver interpretato la parte prima delle parentesi. Anche
 * eventuali numerici iniziali vanno rimossi
 */

		if(p.contains(":"))
		{
			r = new String(p.substring(p.indexOf(":") + 2));

/*
 * La parte dopo i ":" richiede un'elaborazione diversa a seconda che sia del
 * tipo NNa o a.NN, con "a" che per giunta va trasformato opportunamente
 */

			if(r.matches("^[0-9]+.*"))
			{
				r = r.replaceAll("f", " c");
				r = r.replaceAll("p", " p");
			}
			else
			{
				r = r.replaceAll("f\\.", "c. ");
				r = r.replaceAll("p\\.", "p. ");
			}

/*
 * Si mette via la parte prima di ":" e si estrae subito l'eventuale parte fra
 * parentesi
 */

			p = p.substring(0, p.indexOf(":"));
			Log.debug("p = [" + p + "],  r = [" + r + "]");
		}
		if(p.contains("("))
		{
			q = new String(p.substring(p.indexOf("(") - 1));
			p = p.substring(0, p.indexOf("(") - 1);
			Log.debug("p = [" + p + "],  q = [" + q + "]");
		}

/*
 * Adesso p è la parte prima di ":" e senza la parte fra "()". Eventuali
 * numerici iniziali vanno tolti, sperando che effettivamente siano sempre
 * seguiti da " ". Dopo quest'ultima pulizia andiamo a cercare singole parole
 */

		if(p.matches("^[0-9]+.*"))
		{
			p = p.substring(p.indexOf(" ") + 1);
		}

		switch(p)
		{
			case "directorium":
				x = "PC";
				y = "1 parte conduttore";
				break;

			case "other":
				x = "Z";
				y = "*** non ho capito bene ***";
				break;

/*
 * Se q contiene "x" è del tipo (2x), e corrisponde a "2 partiture".
 * Prudentemente, q è inizializzata a "", in modo che il test non sollevi
 * eccezioni di pointer. Oltre alla "x" c'è qualche altro caso strano
 */

			case "score":
				if(q.contains("x"))
				{
					x = "PU";
					y = "2 partiture";
				}
				else if(q.contains("keyboard score"))
				{
					x = "PR";
					y = "1 partitura ristretta";
				}
				else if(q.contains("vocal score"))
				{
					x = "SP";
					y = "1 spartito";
				}
				else
				{
					x = "PU";
					y = "1 partitura" + q;
				}
				score = true;
				break;

			case "scores":
				x = "PU";
				y = "partiture";
				break;

			case "part":
				x = "PA";
				y = "parte";
				break;

			case "parts":
				x = "PA";
				y = "parti";
				break;

			case "St.":
				x = "PA";
				y = "parti";
				break;

			case "short scores":
				x = "SP";
				y = "spartiti";
				break;

/*
 * Vanno distinti alcuni possibili valori di q
 */

			case "short score":
				if(q.contains("piano score"))
				{
					x = "SP";
					y = "1 spartito";
				}
				else if(q.contains("vocal score"))
				{
					x = "SP";
					y = "1 spartito";
				}
				else if(q.contains("choral score"))
				{
					x = "PV";
					y = "1 partitura vocale";
				}
				else
				{
					x = "SP";
					y = "1 spartito";
				}
				break;

			default:
				break;
		}

/*
 * Bisogna tenere conto dell'eventuale parte dopo ":"
 */

		if(r.length() > 0)
		{
			y += " (" + r + ")";
		}

	}

	public boolean isScore()
	{
		return score;
	}

	public String getX()
	{
		return x;
	}

	public String getY()
	{
		return y;
	}
}
