<?xml version="1.0" ?>
<xsl:stylesheet version="2.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.loc.gov/MARC21/slim" xmlns:zs="http://www.loc.gov/zing/srw/"
	xmlns:slim="http://www.loc.gov/MARC21/slim" exclude-result-prefixes="#default xsi slim">

	<xsl:output method="xml" indent="yes" />

	<xsl:import href="identity.xsl" />

	<!--
		partiamo dal livello "record", perché i livelli superiori non sembrano
		interessanti
	-->

	<xsl:template match="//record">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="//slim:datafield[@tag='031']/slim:subfield[@code='r']">
		<xsl:copy>
			<xsl:copy-of select="@code" />
			<xsl:value-of select="translate(.,'|', '')" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//slim:datafield[@tag='240']/slim:subfield[@code='r']">
		<xsl:copy>
			<xsl:copy-of select="@code" />
			<xsl:value-of select="translate(.,'|', '')" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
