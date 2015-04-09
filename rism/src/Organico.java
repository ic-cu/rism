import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import org.jdom2.Element;

public class Organico
{
	private Hashtable<String, Integer> ch, vh, sh;
	private Queue<String> cq, vq, sq;
	private int nStru = 0, nVoci = 0;

	public int getnStru()
	{
		return nStru;
	}

	public int getnVoci()
	{
		return nVoci;
	}

	public void organico(Element s)
	{
		String temp = s.getText();
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

		Log.info("Inizio elaborazione organico");
		for(String e : eList)
		{
			e = e.trim();
			Log.debug(e);
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
				try
				{
					count = e.substring(lp + 1, rp);
				}
				catch(StringIndexOutOfBoundsException ee)
				{
					String msg = "parentesi chiusa non trovata in <" + e + ">, indice " + rp;
					StringIndexOutOfBoundsException e2 = new StringIndexOutOfBoundsException(msg);
					throw e2;
				}
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
					Log.error("Quantificazione non valida: " + e);
				}
				e = count + item;
				lastStru = item;
			}
			else if(e.matches(".* [0-9].*"))
			{
				Log.debug(".* [0-9]+ => " + e);
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
				Log.debug("[0-9]+ => " + e);
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
					Log.error("Non so che fare con questo token: [" + e + "]");
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
		Log.debug("\nVoci");
		while(vq.peek() != null)
		{
			key = vq.poll();
			val = vh.get(key);
			Log.debug(key + " => " + vh.get(key));
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

		Log.debug("Coro");
		if(cq.peek() != null)
		{
			r += "Coro(";
			while(cq.peek() != null)
			{
				key = cq.poll();
				val = ch.get(key);
				Log.debug(key + " => " + ch.get(key));
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

		Log.debug("Strumenti");
		while(sq.peek() != null)
		{
			key = sq.poll();
			val = sh.get(key);
			Log.debug(key + " => " + sh.get(key));
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

		Log.debug(r);
		if(r.length() > 0)
		{
			s.setText(r.substring(0, r.length() - 1));
			Log.debug(s.getText());
		}
		Log.info("Fine elaborazione organico");
	}
}
