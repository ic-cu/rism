<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- 
	Questo è l'identity transform che garantisce che tutto ciò per cui non
	è definito un template viene semplicemente copiato in output
-->

	<xsl:output method="xml" indent="yes" encoding="UTF-8" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
