<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>
<!--
	c'è una perfetta corrispondenza fra "listof_scheda_BIBLIO" sorgente
	e biblioteche nell'output, quindi si può partire da questo template
	che crea "biblioteche" e poi applicare il template per le singole
	"scheda_BIBLIO" del sorgente
-->
	<xsl:template match="/listof_scheda_BIBLIO">
	  <xsl:element name="biblioteche">
<xsl:attribute name="xsi:noNamespaceSchemaLocation">http://anagrafe.iccu.sbn.it/opencms/opencms/informazioni/formato-di-scambio/biblioteca-1.6.xsd</xsl:attribute>
	
	<!-- dataExport va poi cambiato a mano -->
		  <xsl:element name="data-export">2013-11-04T00:00:00</xsl:element>
			<xsl:apply-templates select="scheda_BIBLIO"/>
		</xsl:element>
  </xsl:template>
<!--
	questo template lavora su tutti gli elementi "scheda_BIBLIO"; non
	serve indicarne in modo esatto il path, perché non risulta ci siano
	altri elementi con questo nome con percorsi diversi; l'elemento input
	corrisponde esattamente a "biblioteca" in output;
	il template per adesso è in grado di creare "anagrafica" con nome,
	indirizzo e contatti, poi crea anche patrimonio e servizi
-->
  <xsl:template match="scheda_BIBLIO">
		<xsl:element name="biblioteca">
			<xsl:element name="anagrafica">
				<xsl:apply-templates select="RELAZIONE"/>
				<fonte>
				<descrizione>Regione Liguria</descrizione>
				<url>http://www.culturainliguria.it/cultura/it/Temi/Luoghivisita/biblioteche.do</url>
				</fonte>
				<xsl:element name="nomi">
					<xsl:element name="attuale">
						<xsl:value-of select="NOMEBIB"/>
					</xsl:element>
				</xsl:element>
			<xsl:element name="codici">
					<xsl:element name="isil">
						<xsl:value-of select="CODISIL"/>
					</xsl:element>
		<!-- solo se c'é IDPOLO si deve selezionare il codice SBN-->
				<xsl:if test="string-length(IDPOLO)=3">
				<xsl:element name="sbn">	
				<xsl:value-of select="concat(IDPOLO,CODBIB)"/>
			</xsl:element>
				</xsl:if>
			</xsl:element>
				<xsl:apply-templates select="RECAPITI"/>
				<xsl:apply-templates select="ANNO"/>
			</xsl:element>
			<xsl:call-template name="patrimonio"/>
			<xsl:call-template name="servizi"/>
		</xsl:element>
  <!--</xsl:template>
  
  <xsl:template match="//scheda_BIBLIO/RECAPITI/COMUNE"> 
			<xsl:element name="comune">
				<xsl:value-of select="."/>
			</xsl:element>
	  -->	
  </xsl:template> 

 <xsl:import href="convert-liguria-comune.xsl"/>
  
  <xsl:template match="//scheda_BIBLIO/RECAPITI">
	<xsl:element name="indirizzo">
		<xsl:element name="via">
			<xsl:value-of select="INDIRIZZO" />
		</xsl:element>
		<xsl:element name="cap">
			<xsl:value-of select="CAP" />
		</xsl:element>
		<xsl:apply-templates select="COMUNE" />

		<!--
			metteremo in altro template <xsl:element name="comune"> <xsl:value-of
			select="COMUNE"/> </xsl:element> <xsl:element name="provincia">
			<xsl:value-of select="PROVINCIA"/> </xsl:element>
		-->
	</xsl:element>

	<!-- il comune,ora obbligatorio andrà mappato con la tabella ISTAT -->
	<xsl:element name="contatti">
		<!-- il prefisso è obbligatorio, ma è sempre +39 -->
		<xsl:element name="telefonici">
			<xsl:element name="telefonico">
				<xsl:attribute name="tipo">telefono</xsl:attribute>
				<xsl:element name="prefisso">+39</xsl:element>
				<xsl:element name="numero">
					<xsl:value-of select="TELEFONO" />
				</xsl:element>
			</xsl:element>
			<xsl:element name="telefonico">
				<xsl:attribute name="tipo">fax</xsl:attribute>
				<xsl:element name="prefisso">+39</xsl:element>
				<xsl:element name="numero">
					<xsl:value-of select="FAX" />
				</xsl:element>
			</xsl:element>
		</xsl:element>
		<xsl:element name="altri">
			<xsl:element name="altro">
				<xsl:attribute name="tipo">e-mail</xsl:attribute>
				<xsl:element name="valore">
					<xsl:value-of select="E_MAIL" />
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:element>
</xsl:template>
<xsl:template match="//scheda_BIBLIO/RECAPITI/HOMEPAGE">
		<xsl:element name="altro">
			<xsl:attribute name="tipo">url</xsl:attribute>
			<xsl:element name="valore">
				<xsl:value-of select="."/>
			</xsl:element>
		</xsl:element>
  </xsl:template>
  <xsl:template match="//scheda_BIBLIO/RELAZIONE">
		<xsl:element name="data-censimento">
			<xsl:value-of select="@ANNO"/>
		</xsl:element>
		<xsl:element name="data-aggiornamento">
			<xsl:value-of select="concat(substring(@DATAULTAGG, 7, 4), 
			  '-', substring(@DATAULTAGG, 4, 2),
				'-', substring(@DATAULTAGG, 1, 2), 'T00:00:00')"/>
		</xsl:element>
  </xsl:template>
  <xsl:template match="scheda_BIBLIO/ANNO">
		<xsl:element name="istituzione">
			<xsl:element name="data-istituzione">
				<xsl:value-of select="."/>
			</xsl:element>
		</xsl:element>
  </xsl:template>

<!-- patrimonio, solo se esiste (ma purtroppo a volte è vuoto)-->

  <xsl:template name="patrimonio">
		<xsl:element name="patrimonio">
			<xsl:apply-templates select='CONSISTENZA'/>
			<xsl:if test="FONDISPEC">
				<xsl:call-template name="FONDISPEC"/>
			</xsl:if>
			<xsl:apply-templates select='CONSISTENZA/TOTCONS'/>
		</xsl:element>
  </xsl:template>

<!-- fondi speciali, solo se esistono -->

  <xsl:template name="FONDISPEC">
			<xsl:element name="fondi-speciali">
				<xsl:for-each select='FONDISPEC'>
					<fondo-speciale>
					<nome><xsl:value-of select="DENOMINAZIONE"/></nome>
					<descrizione><xsl:value-of select="DESCRIZIONE"/></descrizione>
					</fondo-speciale>
				</xsl:for-each>
			</xsl:element>
	</xsl:template>

	<!-- 
	il totale-posseduto non viene creato se il sorgente è vuoto, come purtroppo abbiamo
	verificato; il trucco è string-length
	-->
  <xsl:template match="//scheda_BIBLIO/CONSISTENZA/TOTCONS">
		<xsl:if test="not(string-length()=0)">
			<xsl:element name="totale-posseduto">
				<xsl:value-of select="."/>
			</xsl:element>
		</xsl:if>
  </xsl:template>

  <!-- il posseduto di ogni materiale è ignorato se "" (sempre con string-length) 
  c'é da verificare se un materiali vuoto cancella tutto il precedente-->
  <xsl:template match="//scheda_BIBLIO/CONSISTENZA">
		<xsl:element name="materiali">
		<xsl:for-each select="CATEGORIA/DETTAGLIO">
		<xsl:element name="materiale">
			<xsl:attribute name="nome">
				<xsl:value-of select="@descrizione"/>
			</xsl:attribute>
			<xsl:if test="not(string-length(@quantita)=0)">
				<xsl:attribute name="posseduto">
					<xsl:value-of select="@quantita"/>
				</xsl:attribute>
			</xsl:if>
		</xsl:element>
		</xsl:for-each>
		</xsl:element>
	 </xsl:template>

  <!-- spesso descrizioni vuote, testare anche qui con string-length? -->
  
  <xsl:template match="//scheda_BIBLIO/FONDISPEC">
		<xsl:element name="fondo-speciale">
			<xsl:element name="nome">
				<xsl:value-of select="DENOMINAZIONE"/>
			</xsl:element>
			<xsl:element name="descrizione">
				<xsl:value-of select="DESCRIZIONE"/>
			</xsl:element>
		</xsl:element>
  </xsl:template>

<!-- qualcosa nei servizi, poco più di orario, il resto è difficile da usare -->

  <xsl:template name="servizi">
		<xsl:element name="servizi">
			<xsl:element name="orario">
			<!-- le due righe sono necessarie perché l'orario ufficiale deve precedere le
			variazioni (si assume che "invernale" significhi "ufficiale" -->
				<xsl:apply-templates select="APERTURA/ORARIO[@descrizione='Orario Invernale']"/>
				<xsl:apply-templates select="APERTURA/ORARIO[@descrizione='Orario Estivo']"/>
				<xsl:apply-templates select="APERTURA/CHIUSURA"/>
			</xsl:element>
			<!-- questa è stata una fatica: invoca il template solo se l'elemento SERVIZIO
			contiene la stringa 'nformazion' (pare non funzionino "espressioni regolari", quindi
			'nformazion' è il massimo che si possa fare) -->
			<xsl:apply-templates select="SERVIZI/SERVIZIO[contains(., 'nformazion')]"/>
			<xsl:element name="internet">
						<xsl:attribute name="attivo">s</xsl:attribute>
		</xsl:element>
			</xsl:element>
  </xsl:template>

<!-- 
  qui il problema è ottenere per primo l'orario ufficiale, come previsto
	dal formato ICCU; è stato risolto invocando due volte il template con @descrizione
	diverse, vedi sopra
-->
  <xsl:template match="//scheda_BIBLIO/APERTURA/ORARIO">
		<xsl:if test="@descrizione='Orario Invernale'">
			<xsl:element name="ufficiale">
				<xsl:apply-templates select="GIORNO"/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="@descrizione='Orario Estivo'">
			<xsl:element name="variazione">
				<xsl:apply-templates select="GIORNO"/>
					<xsl:element name="note">
						<xsl:value-of select="@descrizione"/>
					</xsl:element>
			</xsl:element>
		</xsl:if>
  </xsl:template>

<!--
  qui la cosa più difficile è riportare i nomi dei giorni alle
	abbreviazioni usate nel formato ICCU: la tecnica usata consiste nel
	prendere le prime tre lettere e, in mancanza di una funzione
	"lower-case" introdotta solo in XPath 2.0, tradurre le sei possibili
	lettere maiuscole nelle rispettive minuscole;
	il template va applicato solo dall'interno di un elemento
	"orario/ufficiale" oppure "orario/variazione" dell'output
-->

  <xsl:template match="//scheda_BIBLIO/APERTURA/ORARIO/GIORNO">
		<xsl:element name="orario">
			<xsl:attribute name="giorno">
				<xsl:value-of select="translate(substring(@nome,1,3), 'LMGVSD', 'lmgvsd')"/>
			</xsl:attribute>
			<xsl:attribute name="dalle"><xsl:value-of select="FASCIA/@dalle"/></xsl:attribute>
			<xsl:attribute name="alle"><xsl:value-of select="FASCIA/@alle"/></xsl:attribute>
		</xsl:element>
  </xsl:template>

  <xsl:template match="//scheda_BIBLIO/APERTURA/CHIUSURA">
		<xsl:element name="chiusura">
			<!--  <xsl:element name="note"> -->
			<note><xsl:value-of select="normalize-space(.)"/></note>
			<!--  </xsl:element> -->
		</xsl:element>
  </xsl:template>
	<xsl:template match="//scheda_BIBLIO/SERVIZI/SERVIZIO">
		<xsl:element name="informazioni-bibliografiche">
		<xsl:attribute name="attivo">s</xsl:attribute>
			<xsl:element name="servizio-interno">s</xsl:element>
	</xsl:element>
	</xsl:template>
</xsl:stylesheet>
