package org.wikidata.wdtk.datamodel.jsonconverter;

/*
 * #%L
 * Wikidata Toolkit Data Model
 * %%
 * Copyright (C) 2014 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.*;
import org.wikidata.wdtk.datamodel.interfaces.Claim;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.NoValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.StatementRank;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TermedDocument;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

/**
 * Implementation of {@link Converter} that provides a Conversions from Objects
 * constructed by the
 * {@link org.wikidata.wdtk.datamodel.interfaces.DataObjectFactory} into a
 * json-format using JSONObjects from the org.json library.
 * 
 * @author Michael Günther
 * 
 */
public class ConverterImpl implements Converter<JSONObject> {

	final String KEY_ENTITY_TYP_ITEM = "item";
	final String KEY_ENTITY_TYP_PROPERTY = "property";

	final String KEY_ID = "id";
	final String KEY_TITLE = "title";
	final String KEY_CLAIMS = "claims";
	final String KEY_SNAKS = "snaks";
	final String KEY_ALIASES = "aliases";
	final String KEY_DESCRIPTIONS = "descriptions";
	final String KEY_SITE_LINKS = "sitelinks";
	final String KEY_LABELS = "labels";
	final String KEY_DATATYP = "datatype";
	final String KEY_TYP = "type";
	final String KEY_SNAK_TYP = "snaktype";
	final String KEY_PROPERTY = "property";
	final String KEY_VALUE = "value";
	final String KEY_DATAVALUE = "datavalue";
	final String KEY_MAINSNAK = "mainsnak";
	final String KEY_QUALIFIERS = "qualifiers";

	final String STD_UNIT_VALUE = "1";

	/**
	 * Creates a json-representation for the aliases of an document.
	 * 
	 * @param document
	 * @return JSONObject representing aliases of a {@link TermedDocument}
	 * @throws JSONException
	 */
	JSONObject convertAliasesToJson(TermedDocument document)
			throws JSONException {
		JSONObject result = new JSONObject();
		for (String key : document.getAliases().keySet()) {
			JSONArray alias = new JSONArray();
			result.put(key, alias);
			for (MonolingualTextValue value : document.getAliases().get(key)) {
				alias.put(visit(value));
			}
		}

		return result;
	}

	/**
	 * Creates a json-representation for the descriptions of an
	 * {@link TermedeDocument}.
	 * 
	 * @param document
	 * @return JSONObject representing descriptions of a {@link TermedDocument}
	 * @throws JSONException
	 */

	JSONObject convertDescriptionsToJson(TermedDocument document)
			throws JSONException {
		JSONObject result = new JSONObject();
		for (String key : document.getDescriptions().keySet()) {
			result.put(key, visit(document.getDescriptions().get(key)));
		}
		return result;

	}

	/**
	 * Creates a json-representation for the labels of a {@link TermedDocument}.
	 * 
	 * @param document
	 * @return JSONObject of labels
	 * @throws JSONException
	 */

	JSONObject convertLabelsToJson(TermedDocument document)
			throws JSONException {
		JSONObject result = new JSONObject();
		for (String key : document.getLabels().keySet()) {
			result.put(key, visit(document.getLabels().get(key)));
		}
		return result;
	}

	/**
	 * Creates a json-representation for the SiteLinks of an
	 * {@link ItemDocument}.
	 * 
	 * @param document
	 * @return JSONObject representation for
	 *         {@link org.wikidata.wdtk.datamodel.interfaces.SiteLink} objects
	 * @throws JSONException
	 */
	JSONObject convertSiteLinksToJson(ItemDocument document)
			throws JSONException {
		JSONObject result = new JSONObject();
		for (String key : document.getSiteLinks().keySet()) {
			result.put(key, visit(document.getSiteLinks().get(key)));
		}
		return result;
	}

	/**
	 * Creates a json-representation for qualifiers of a {@link Claim}.
	 * 
	 * @param qualifiers
	 * @return JSONObject of qualifiers
	 */
	JSONObject convertQualifiersToJson(List<? extends Snak> qualifiers) {
		JSONObject result = new JSONObject();
		Set<String> qualifierGroups = new HashSet<String>();
		for (Snak qualifier : qualifiers) {
			final String pId = qualifier.getPropertyId().getEntityType();
			if (!qualifierGroups.contains(pId)) {
				result.put(pId, new JSONArray());
			}
			final JSONArray group = (JSONArray) result.get(pId);
			group.put(convertSnakToJson(qualifier));
		}
		return result;
	}

	/**
	 * Adds the attributes occurring in every {@link TermedDocument} to elem.
	 * 
	 * @param document
	 * @param elem
	 * @return JSONObject with "aliases", "descriptions", "labels" key
	 */
	JSONObject addTermedDocumentAttributes(TermedDocument document,
			JSONObject elem) {
		JSONObject result = elem;
		result.put(KEY_ID, document.getEntityId().getId());
		result.put(KEY_TITLE, document.getEntityId().getId());
		if (!document.getAliases().isEmpty()) {
			result.put(KEY_ALIASES, convertAliasesToJson(document));
		}
		if (!document.getDescriptions().isEmpty()) {
			result.put(KEY_DESCRIPTIONS, convertDescriptionsToJson(document));
		}
		if (!document.getLabels().isEmpty()) {
			result.put(KEY_LABELS, convertLabelsToJson(document));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertClaimToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.Claim)
	 */
	@Override
	public JSONObject visit(Claim claim) throws JSONException {
		JSONObject result = new JSONObject();
		result.put(KEY_MAINSNAK, convertSnakToJson(claim.getMainSnak()));
		if (!claim.getQualifiers().isEmpty()) {
			result.put(KEY_QUALIFIERS,
					convertQualifiersToJson(claim.getQualifiers()));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertItemDocumentToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.ItemDocument)
	 */
	@Override
	public JSONObject visit(ItemDocument itemDocument) throws JSONException {
		JSONObject result = new JSONObject();
		JSONObject statementGroups = new JSONObject();
		result = addTermedDocumentAttributes(itemDocument, result);
		result.put(KEY_TYP, KEY_ENTITY_TYP_ITEM);
		if (!itemDocument.getStatementGroups().isEmpty()) {
			result.put(KEY_CLAIMS, statementGroups);
		}
		if (!itemDocument.getSiteLinks().isEmpty()) {
			result.put(KEY_SITE_LINKS, convertSiteLinksToJson(itemDocument));
		}

		for (StatementGroup statementGroup : itemDocument.getStatementGroups()) {
			statementGroups.put(
					statementGroup.getProperty().getId().toString(),
					convertStatementGroupToJson(statementGroup));

		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wikidata.wdtk.datamodel.jsonconverter.Converter#
	 * convertPropertyDocumentToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.PropertyDocument)
	 */
	@Override
	public JSONObject visit(PropertyDocument document) throws JSONException {
		JSONObject result = new JSONObject();
		result.put(KEY_TYP, KEY_ENTITY_TYP_PROPERTY);
		result = addTermedDocumentAttributes(document, result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertReferenceToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.Reference)
	 */
	@Override
	public JSONObject visit(Reference ref) throws JSONException {
		JSONObject result = new JSONObject();
		JSONObject snaks = new JSONObject();
		JSONArray snakOrder = new JSONArray();
		Set<String> snakGroups = new HashSet<String>();

		// no ref.hash value...

		result.put(KEY_SNAKS, snaks);
		result.put("snak-order", snakOrder);

		for (ValueSnak snak : ref.getSnaks()) {
			final String pId = snak.getPropertyId().getId();
			if (!snakGroups.contains(pId)) {
				snaks.put(pId, new JSONArray());
				snakGroups.add(pId);
			}
			final JSONArray group = (JSONArray) snaks.get(pId);
			group.put(visit(snak));
		}

		for (String pId : snakGroups) {
			snakOrder.put(pId);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertStatementToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.Statement)
	 */
	@Override
	public JSONObject visit(Statement statement) throws JSONException {
		JSONObject result = new JSONObject();

		result.put(KEY_ID, statement.getStatementId());
		result.put(KEY_MAINSNAK, convertSnakToJson(statement.getClaim()
				.getMainSnak()));
		if (statement.getClaim().getQualifiers().isEmpty() == false) {
			result.put(KEY_QUALIFIERS,
					convertQualifiersToJson((List<? extends Snak>) statement
							.getClaim().getQualifiers()));
		}
		// What about the Subject?
		result.put(KEY_TYP, "statement");
		result.put("rank", convertStatementRankToJson(statement.getRank()));
		JSONArray references = new JSONArray();
		if (!statement.getReferences().isEmpty()) {
			result.put("references", references);
		}
		for (Reference ref : statement.getReferences()) {
			references.put(visit(ref));
		}
		return result;
	}

	/**
	 * Creates a json-representation of a
	 * {@link org.wikidata.wdtk.datamodel.interfaces.StatementGroup}.
	 * 
	 * @param statementGroup
	 * @return Json representation of a
	 *         {@link org.wikidata.wdtk.datamodel.interfaces.StatementGroup}
	 * @throws JSONException
	 */
	public JSONArray convertStatementGroupToJson(StatementGroup statementGroup)
			throws JSONException {
		JSONArray statements = new JSONArray();
		for (Statement statement : statementGroup.getStatements()) {
			statements.put(visit(statement));
		}
		return statements;
	}

	/**
	 * Creates a json-representation of a
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ValueSnak},
	 * {@link org.wikidata.wdtk.datamodel.interfaces.NoValueSnak} or a
	 * {@link org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak}.
	 * 
	 * @param snak
	 * @return JSONObject representing for a specific
	 *         {@link org.wikidata.wdtk.datamodel.interfaces.Snak}
	 * @throws JSONException
	 */
	public JSONObject convertSnakToJson(Snak snak) throws JSONException {
		JSONObject result;
		if (snak instanceof NoValueSnak) {
			result = visit((NoValueSnak) snak);
		} else if (snak instanceof SomeValueSnak) {
			result = visit((SomeValueSnak) snak);
		} else if (snak instanceof ValueSnak) {
			result = visit((ValueSnak) snak);
		} else {
			throw new IllegalArgumentException("Snaktype is unknown!");
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertValueSnakToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.ValueSnak)
	 */
	@Override
	public JSONObject visit(ValueSnak snak) throws JSONException {
		JSONObject result = new JSONObject();
		result.put(KEY_SNAK_TYP, "value");
		result.put(KEY_PROPERTY, snak.getPropertyId().getId());

		// maybe there are more possibilities
		if (snak.getValue() instanceof EntityIdValue) {
			// TODO put in datatype result.put(KEY_DATATYP, ????);
			result.put(KEY_DATAVALUE,
					convertEntityIdValueToJson((EntityIdValue) snak.getValue()));
		} else if (snak.getValue() instanceof TimeValue) {
			// TODO put in datatype result.put(KEY_DATATYP, ????);
			result.put(KEY_DATAVALUE, visit((TimeValue) snak.getValue()));
		} else if (snak.getValue() instanceof GlobeCoordinatesValue) {
			// TODO put in datatype result.put(KEY_DATATYP, ????);
			result.put(KEY_DATAVALUE,
					visit((GlobeCoordinatesValue) snak.getValue()));
		} else if (snak.getValue() instanceof QuantityValue) {
			// TODO put in datatype result.put(KEY_DATATYP, ????);
			result.put(KEY_DATAVALUE, visit((QuantityValue) snak.getValue()));
		} else if (snak.getValue() instanceof StringValue) {
			// TODO put in datatype result.put(KEY_DATATYP, ????);
			result.put(KEY_DATAVALUE, visit((StringValue) snak.getValue()));
		} else {
			throw new IllegalArgumentException("class of the value "
					+ snak.getValue().getClass() + " is unknown");
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertNoValueSnakToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.NoValueSnak)
	 */
	@Override
	public JSONObject visit(NoValueSnak snak) throws JSONException {
		JSONObject result = new JSONObject();
		result.put(KEY_SNAK_TYP, "novalue");
		result.put(KEY_PROPERTY, snak.getPropertyId().getId());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wikidata.wdtk.datamodel.jsonconverter.Converter#
	 * convertSomeValueSnakToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak)
	 */
	@Override
	public JSONObject visit(SomeValueSnak snak) throws JSONException {
		JSONObject result = new JSONObject();
		result.put(KEY_SNAK_TYP, "somevalue");
		result.put(KEY_PROPERTY, snak.getPropertyId().getId());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wikidata.wdtk.datamodel.jsonconverter.Converter#
	 * convertQuantityValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.QuantityValue)
	 */
	@Override
	public JSONObject visit(QuantityValue value) throws JSONException {
		JSONObject result = new JSONObject();
		JSONObject valueResult = new JSONObject();

		result.put(KEY_VALUE, valueResult);

		valueResult.put("amount",
				DatatypeConverters.formatBigDecimal(value.getNumericValue()));
		valueResult.put("unit", STD_UNIT_VALUE);
		valueResult.put("upperBound",
				DatatypeConverters.formatBigDecimal(value.getUpperBound()));
		valueResult.put("lowerBound",
				DatatypeConverters.formatBigDecimal(value.getLowerBound()));

		result.put(KEY_TYP, "quantity");

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertTimeValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.TimeValue)
	 */
	@Override
	public JSONObject visit(TimeValue value) throws JSONException {
		JSONObject result = new JSONObject();
		JSONObject valueResult = new JSONObject();

		result.put(KEY_VALUE, valueResult);

		valueResult.put("time", DatatypeConverters.formatTimeISO8601(value));
		valueResult.put("timezone", value.getTimezoneOffset());
		valueResult.put("before", value.getBeforeTolerance());
		valueResult.put("after", value.getAfterTolerance());
		valueResult.put("precision", value.getPrecision());
		valueResult.put("calendarmodel", value.getPreferredCalendarModel());

		result.put(KEY_TYP, "time");

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wikidata.wdtk.datamodel.jsonconverter.Converter#
	 * convertGlobeCoordinatesValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue)
	 */
	@Override
	public JSONObject visit(GlobeCoordinatesValue value) throws JSONException {
		JSONObject result = new JSONObject();
		JSONObject valueResult = new JSONObject();
		result.put(KEY_VALUE, valueResult);

		valueResult.put("latitude", value.getLatitude());
		valueResult.put("longitude", value.getLongitude());
		valueResult.put("precision", value.getPrecision()
				/ GlobeCoordinatesValue.PREC_DEGREE);
		valueResult.put("globe", value.getGlobe());

		result.put(KEY_TYP, "globecoordinate");

		return result;
	}

	/**
	 * Creates a json-representation of an
	 * {@link org.wikidata.wdtk.datamodel.interfaces.EntityIdValue}.
	 * 
	 * @param value
	 * @return json-representation of an
	 *         {@link org.wikidata.wdtk.datamodel.interfaces.EntityIdValue}
	 * @throws JSONException
	 */
	public JSONObject convertEntityIdValueToJson(EntityIdValue value)
			throws JSONException {

		JSONObject result = new JSONObject();
		JSONObject valueResult = new JSONObject();

		result.put(KEY_TYP, "wikibase-entityid");
		switch (value.getEntityType()) {
		case EntityIdValue.ET_ITEM:
			valueResult = visit((ItemIdValue) value);
			break;
		case EntityIdValue.ET_PROPERTY:
			valueResult = visit((PropertyIdValue) value);
			break;
		default:
			throw new JSONException("Unknown EntityType: "
					+ value.getEntityType());
		}

		result.put(KEY_VALUE, valueResult);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertStringValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.StringValue)
	 */
	@Override
	public JSONObject visit(StringValue value) throws JSONException {
		JSONObject result = new JSONObject();
		result.put(KEY_VALUE, value.getString());
		result.put(KEY_TYP, "string");
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wikidata.wdtk.datamodel.jsonconverter.Converter#
	 * convertDatatypeIdValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue)
	 */
	@Override
	public JSONObject visit(DatatypeIdValue value) throws JSONException {
		// TODO implement
		return new JSONObject(); // empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertItemIdValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.ItemIdValue)
	 */
	@Override
	public JSONObject visit(ItemIdValue value) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("entity-type", KEY_ENTITY_TYP_ITEM);
		result.put("numeric-id", value.getId());

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wikidata.wdtk.datamodel.jsonconverter.Converter#
	 * convertMonolingualTextValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue)
	 */
	@Override
	public JSONObject visit(MonolingualTextValue value) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("language", value.getLanguageCode());
		result.put(KEY_VALUE, value.getText());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wikidata.wdtk.datamodel.jsonconverter.Converter#
	 * convertPropertyIdValueToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue)
	 */
	@Override
	public JSONObject visit(PropertyIdValue value) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("entity-type", KEY_ENTITY_TYP_PROPERTY);
		result.put("numeric-id", value.getId());

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wikidata.wdtk.datamodel.jsonconverter.Converter#convertSiteLinkToJson
	 * (org.wikidata.wdtk.datamodel.interfaces.SiteLink)
	 */
	@Override
	public JSONObject visit(SiteLink link) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("site", link.getSiteKey());
		result.put(KEY_TITLE, link.getArticleTitle());
		result.put("badges", new JSONArray()); // always empty at the moment
		return result;
	}

	/**
	 * Creates a string notation for
	 * {@link org.wikidata.wdtk.datamodel.interfaces.StatementRank}.
	 * 
	 * @param rank
	 * 
	 * @return {@link org.wikidata.wdtk.datamodel.interfaces.StatementRank} in
	 *         string notation for the json-format
	 * 
	 */
	public String convertStatementRankToJson(StatementRank rank) {
		return rank.toString().toLowerCase();
	}

	/**
	 * If the parameter is an ItemDocument or a PropertyDocument the respective
	 * function for document is called, otherwise it will throw an
	 * IllegalArgumentException.
	 * 
	 * @param document
	 * @return JSONObject for
	 *         {@link org.wikidata.wdtk.datamodel.interfaces.TermedDocument}
	 * @throws JSONException
	 */
	public JSONObject convertTermedDocumentToJson(TermedDocument document)
			throws JSONException {

		JSONObject result = new JSONObject();
		if (document instanceof ItemDocument) {
			result = visit((ItemDocument) document);
		} else if (document instanceof PropertyDocument) {
			result = visit((PropertyDocument) document);
		} else {
			throw new IllegalArgumentException("Class of document is unknown");
		}

		return result;
	}
}
