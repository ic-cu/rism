import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.JDOMException;

public class TestRE
{

	public static void main(String[] args) throws JDOMException, IOException
	{
		String temp1;
		temp1 = "ciao, parla!";
		temp1 = "kornetto 1, kornetto 2, kornetto 3";
		Matcher m = Pattern.compile("([a-z]+) ([0-9]),").matcher(temp1);
		while(m.find())
		{
			System.out.println(m.group(1));
		}
		String temp2 = temp1.replaceAll(m.group(1), "mors mea tacci tua");
// temp = temp.replaceAll("[a-z]+", "\\1 ");
// temp2 = temp1.replaceAll("\\(pirla\\)", "\\2");
		System.out.println(temp2);
	}
}
