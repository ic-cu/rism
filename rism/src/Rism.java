import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
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
	private static Logger log;
	private Properties config;
	private String today;
	private String tempDir;

	public Rism()
	{
		config = new Properties();
		try
		{
			config.load(new FileReader("rism.prop"));
			initLogger();
		}
		catch(FileNotFoundException e)
		{
			log.warn("File non trovato: " + e.getMessage());
		}
		catch(IOException e)
		{
			log.error("Impossibile leggere il file di configurazione: "
					+ e.getMessage());
		}

		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyyMMdd");
		today = sdf.format(new Date());

		new SimpleDateFormat(
				config.getProperty("dateStamp.pattern"));

		tempDir = config.getProperty("temp.dir");

		if(config.getProperty("temp.dir.daily") != null)
		{
			tempDir += "/" + today;
		}
		File tDir = null;
		tDir = new File(tempDir);
		tDir.mkdirs();
	}

	private void initLogger() throws FileNotFoundException
	{
		// logger generico
		PatternLayout pl;
		File lf;
		PrintWriter pw;
		WriterAppender wa;
		log = Logger.getLogger("RISM");
		log.setLevel(Level.INFO);
		pl = new PatternLayout(config.getProperty("log.pattern"));
		lf = new File(config.getProperty("log.file"));
		pw = new PrintWriter(lf);
		wa = new WriterAppender(pl, pw);
		log.addAppender(wa);
		wa = new WriterAppender(pl, System.out);
		log.addAppender(wa);
	}

	public static void main(String[] args) throws JDOMException, IOException
	{

		String xmlSource = "input/opac2.xml";
		if(args.length > 0)
		{
			xmlSource = args[0];
		}
		new Log();
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
//			f.leader(record);
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
			log.info(++count + ": " + linkElement.getValue());
			linkElement.setText("Questo era un link: " + linkElement.getValue());
		}

		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		String xmlTarget = xmlSource.substring(0, xmlSource.indexOf(".xml"));
		xmlTarget += ".out.xml";
		xo.output(jdomDocument, new PrintWriter(xmlTarget));
	}

}
