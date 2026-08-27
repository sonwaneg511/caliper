package com.caliper.location.gmb.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import com.google.api.services.mybusinessbusinessinformation.v1.model.TimeOfDay;

@Configuration
public class GMBHelper {

	public TimeOfDay getTime(String time) {
		String[] split = time.split(":");
		TimeOfDay timeOfDay = new TimeOfDay();
		timeOfDay.setHours(Integer.valueOf(split[0]));
		timeOfDay.setMinutes(Integer.valueOf(split[1]));
		//		timeOfDay.setSeconds(0);
		//		timeOfDay.setNanos(0);
		//System.out.println("Hours = "+Integer.decode(split[0]) +" minutes = "+Integer.decode(split[1]));
		return timeOfDay;
	}
	
	public static String getCountryByCode(String countryCode) {
	    if (StringUtils.isEmpty(countryCode)) {
	        return "";
	    }
	    return COUNTRY_MAP.getOrDefault(countryCode.toUpperCase(), "");
	}
	
	private static final Map<String, String> COUNTRY_MAP = new HashMap<>();

	static {
	    COUNTRY_MAP.put("AC", "Ascension Island");
	    COUNTRY_MAP.put("AD", "Andorra");
	    COUNTRY_MAP.put("AE", "United Arab Emirates");
	    COUNTRY_MAP.put("AF", "Afghanistan");
	    COUNTRY_MAP.put("AG", "Antigua & Barbuda");
	    COUNTRY_MAP.put("AI", "Anguilla");
	    COUNTRY_MAP.put("AL", "Albania");
	    COUNTRY_MAP.put("AM", "Armenia");
	    COUNTRY_MAP.put("AO", "Angola");
	    COUNTRY_MAP.put("AQ", "Antarctica");
	    COUNTRY_MAP.put("AR", "Argentina");
	    COUNTRY_MAP.put("AS", "American Samoa");
	    COUNTRY_MAP.put("AT", "Austria");
	    COUNTRY_MAP.put("AU", "Australia");
	    COUNTRY_MAP.put("AW", "Aruba");
	    COUNTRY_MAP.put("AX", "Åland Islands");
	    COUNTRY_MAP.put("AZ", "Azerbaijan");

	    COUNTRY_MAP.put("BA", "Bosnia & Herzegovina");
	    COUNTRY_MAP.put("BB", "Barbados");
	    COUNTRY_MAP.put("BD", "Bangladesh");
	    COUNTRY_MAP.put("BE", "Belgium");
	    COUNTRY_MAP.put("BF", "Burkina Faso");
	    COUNTRY_MAP.put("BG", "Bulgaria");
	    COUNTRY_MAP.put("BH", "Bahrain");
	    COUNTRY_MAP.put("BI", "Burundi");
	    COUNTRY_MAP.put("BJ", "Benin");
	    COUNTRY_MAP.put("BL", "St. Barthélemy");
	    COUNTRY_MAP.put("BM", "Bermuda");
	    COUNTRY_MAP.put("BN", "Brunei");
	    COUNTRY_MAP.put("BO", "Bolivia");
	    COUNTRY_MAP.put("BQ", "Caribbean Netherlands");
	    COUNTRY_MAP.put("BR", "Brazil");
	    COUNTRY_MAP.put("BS", "Bahamas");
	    COUNTRY_MAP.put("BT", "Bhutan");
	    COUNTRY_MAP.put("BV", "Bouvet Island");
	    COUNTRY_MAP.put("BW", "Botswana");
	    COUNTRY_MAP.put("BY", "Belarus");
	    COUNTRY_MAP.put("BZ", "Belize");

	    COUNTRY_MAP.put("CA", "Canada");
	    COUNTRY_MAP.put("CC", "Cocos (Keeling) Islands");
	    COUNTRY_MAP.put("CD", "Congo (DRC)");
	    COUNTRY_MAP.put("CF", "Central African Republic");
	    COUNTRY_MAP.put("CG", "Congo (Republic)");
	    COUNTRY_MAP.put("CH", "Switzerland");
	    COUNTRY_MAP.put("CI", "Côte d’Ivoire");
	    COUNTRY_MAP.put("CK", "Cook Islands");
	    COUNTRY_MAP.put("CL", "Chile");
	    COUNTRY_MAP.put("CM", "Cameroon");
	    COUNTRY_MAP.put("CN", "China");
	    COUNTRY_MAP.put("CO", "Colombia");
	    COUNTRY_MAP.put("CR", "Costa Rica");
	    COUNTRY_MAP.put("CU", "Cuba");
	    COUNTRY_MAP.put("CV", "Cape Verde");
	    COUNTRY_MAP.put("CW", "Curaçao");
	    COUNTRY_MAP.put("CX", "Christmas Island");
	    COUNTRY_MAP.put("CY", "Cyprus");
	    COUNTRY_MAP.put("CZ", "Czech Republic");

	    COUNTRY_MAP.put("DE", "Germany");
	    COUNTRY_MAP.put("DJ", "Djibouti");
	    COUNTRY_MAP.put("DK", "Denmark");
	    COUNTRY_MAP.put("DM", "Dominica");
	    COUNTRY_MAP.put("DO", "Dominican Republic");
	    COUNTRY_MAP.put("DZ", "Algeria");

	    COUNTRY_MAP.put("EC", "Ecuador");
	    COUNTRY_MAP.put("EE", "Estonia");
	    COUNTRY_MAP.put("EG", "Egypt");
	    COUNTRY_MAP.put("EH", "Western Sahara");
	    COUNTRY_MAP.put("ER", "Eritrea");
	    COUNTRY_MAP.put("ES", "Spain");
	    COUNTRY_MAP.put("ET", "Ethiopia");

	    COUNTRY_MAP.put("FI", "Finland");
	    COUNTRY_MAP.put("FJ", "Fiji");
	    COUNTRY_MAP.put("FK", "Falkland Islands");
	    COUNTRY_MAP.put("FM", "Micronesia");
	    COUNTRY_MAP.put("FO", "Faroe Islands");
	    COUNTRY_MAP.put("FR", "France");

	    COUNTRY_MAP.put("GA", "Gabon");
	    COUNTRY_MAP.put("GB", "United Kingdom");
	    COUNTRY_MAP.put("GD", "Grenada");
	    COUNTRY_MAP.put("GE", "Georgia");
	    COUNTRY_MAP.put("GF", "French Guiana");
	    COUNTRY_MAP.put("GG", "Guernsey");
	    COUNTRY_MAP.put("GH", "Ghana");
	    COUNTRY_MAP.put("GI", "Gibraltar");
	    COUNTRY_MAP.put("GL", "Greenland");
	    COUNTRY_MAP.put("GM", "Gambia");
	    COUNTRY_MAP.put("GN", "Guinea");
	    COUNTRY_MAP.put("GP", "Guadeloupe");
	    COUNTRY_MAP.put("GQ", "Equatorial Guinea");
	    COUNTRY_MAP.put("GR", "Greece");
	    COUNTRY_MAP.put("GS", "South Georgia & South Sandwich Islands");
	    COUNTRY_MAP.put("GT", "Guatemala");
	    COUNTRY_MAP.put("GU", "Guam");
	    COUNTRY_MAP.put("GW", "Guinea-Bissau");
	    COUNTRY_MAP.put("GY", "Guyana");

	    COUNTRY_MAP.put("HK", "Hong Kong");
	    COUNTRY_MAP.put("HM", "Heard Island & McDonald Islands");
	    COUNTRY_MAP.put("HN", "Honduras");
	    COUNTRY_MAP.put("HR", "Croatia");
	    COUNTRY_MAP.put("HT", "Haiti");
	    COUNTRY_MAP.put("HU", "Hungary");

	    COUNTRY_MAP.put("ID", "Indonesia");
	    COUNTRY_MAP.put("IE", "Ireland");
	    COUNTRY_MAP.put("IL", "Israel");
	    COUNTRY_MAP.put("IM", "Isle of Man");
	    COUNTRY_MAP.put("IN", "India");
	    COUNTRY_MAP.put("IO", "British Indian Ocean Territory");
	    COUNTRY_MAP.put("IQ", "Iraq");
	    COUNTRY_MAP.put("IR", "Iran");
	    COUNTRY_MAP.put("IS", "Iceland");
	    COUNTRY_MAP.put("IT", "Italy");

	    COUNTRY_MAP.put("JE", "Jersey");
	    COUNTRY_MAP.put("JM", "Jamaica");
	    COUNTRY_MAP.put("JO", "Jordan");
	    COUNTRY_MAP.put("JP", "Japan");

	    COUNTRY_MAP.put("KE", "Kenya");
	    COUNTRY_MAP.put("KG", "Kyrgyzstan");
	    COUNTRY_MAP.put("KH", "Cambodia");
	    COUNTRY_MAP.put("KI", "Kiribati");
	    COUNTRY_MAP.put("KM", "Comoros");
	    COUNTRY_MAP.put("KN", "Saint Kitts & Nevis");
	    COUNTRY_MAP.put("KP", "North Korea");
	    COUNTRY_MAP.put("KR", "South Korea");
	    COUNTRY_MAP.put("KW", "Kuwait");
	    COUNTRY_MAP.put("KY", "Cayman Islands");
	    COUNTRY_MAP.put("KZ", "Kazakhstan");

	    COUNTRY_MAP.put("LA", "Laos");
	    COUNTRY_MAP.put("LB", "Lebanon");
	    COUNTRY_MAP.put("LC", "Saint Lucia");
	    COUNTRY_MAP.put("LI", "Liechtenstein");
	    COUNTRY_MAP.put("LK", "Sri Lanka");
	    COUNTRY_MAP.put("LR", "Liberia");
	    COUNTRY_MAP.put("LS", "Lesotho");
	    COUNTRY_MAP.put("LT", "Lithuania");
	    COUNTRY_MAP.put("LU", "Luxembourg");
	    COUNTRY_MAP.put("LV", "Latvia");
	    COUNTRY_MAP.put("LY", "Libya");

	    COUNTRY_MAP.put("MA", "Morocco");
	    COUNTRY_MAP.put("MC", "Monaco");
	    COUNTRY_MAP.put("MD", "Moldova");
	    COUNTRY_MAP.put("ME", "Montenegro");
	    COUNTRY_MAP.put("MF", "Saint Martin");
	    COUNTRY_MAP.put("MG", "Madagascar");
	    COUNTRY_MAP.put("MH", "Marshall Islands");
	    COUNTRY_MAP.put("MK", "North Macedonia");
	    COUNTRY_MAP.put("ML", "Mali");
	    COUNTRY_MAP.put("MM", "Myanmar");
	    COUNTRY_MAP.put("MN", "Mongolia");
	    COUNTRY_MAP.put("MO", "Macao");
	    COUNTRY_MAP.put("MP", "Northern Mariana Islands");
	    COUNTRY_MAP.put("MQ", "Martinique");
	    COUNTRY_MAP.put("MR", "Mauritania");
	    COUNTRY_MAP.put("MS", "Montserrat");
	    COUNTRY_MAP.put("MT", "Malta");
	    COUNTRY_MAP.put("MU", "Mauritius");
	    COUNTRY_MAP.put("MV", "Maldives");
	    COUNTRY_MAP.put("MW", "Malawi");
	    COUNTRY_MAP.put("MX", "Mexico");
	    COUNTRY_MAP.put("MY", "Malaysia");
	    COUNTRY_MAP.put("MZ", "Mozambique");

	    COUNTRY_MAP.put("NA", "Namibia");
	    COUNTRY_MAP.put("NC", "New Caledonia");
	    COUNTRY_MAP.put("NE", "Niger");
	    COUNTRY_MAP.put("NF", "Norfolk Island");
	    COUNTRY_MAP.put("NG", "Nigeria");
	    COUNTRY_MAP.put("NI", "Nicaragua");
	    COUNTRY_MAP.put("NL", "Netherlands");
	    COUNTRY_MAP.put("NO", "Norway");
	    COUNTRY_MAP.put("NP", "Nepal");
	    COUNTRY_MAP.put("NR", "Nauru");
	    COUNTRY_MAP.put("NU", "Niue");
	    COUNTRY_MAP.put("NZ", "New Zealand");

	    COUNTRY_MAP.put("OM", "Oman");

	    COUNTRY_MAP.put("PA", "Panama");
	    COUNTRY_MAP.put("PE", "Peru");
	    COUNTRY_MAP.put("PF", "French Polynesia");
	    COUNTRY_MAP.put("PG", "Papua New Guinea");
	    COUNTRY_MAP.put("PH", "Philippines");
	    COUNTRY_MAP.put("PK", "Pakistan");
	    COUNTRY_MAP.put("PL", "Poland");
	    COUNTRY_MAP.put("PM", "Saint Pierre & Miquelon");
	    COUNTRY_MAP.put("PN", "Pitcairn");
	    COUNTRY_MAP.put("PR", "Puerto Rico");
	    COUNTRY_MAP.put("PS", "Palestine");
	    COUNTRY_MAP.put("PT", "Portugal");
	    COUNTRY_MAP.put("PW", "Palau");
	    COUNTRY_MAP.put("PY", "Paraguay");

	    COUNTRY_MAP.put("QA", "Qatar");

	    COUNTRY_MAP.put("RE", "Réunion");
	    COUNTRY_MAP.put("RO", "Romania");
	    COUNTRY_MAP.put("RS", "Serbia");
	    COUNTRY_MAP.put("RU", "Russia");
	    COUNTRY_MAP.put("RW", "Rwanda");

	    COUNTRY_MAP.put("SA", "Saudi Arabia");
	    COUNTRY_MAP.put("SB", "Solomon Islands");
	    COUNTRY_MAP.put("SC", "Seychelles");
	    COUNTRY_MAP.put("SD", "Sudan");
	    COUNTRY_MAP.put("SE", "Sweden");
	    COUNTRY_MAP.put("SG", "Singapore");
	    COUNTRY_MAP.put("SH", "St. Helena");
	    COUNTRY_MAP.put("SI", "Slovenia");
	    COUNTRY_MAP.put("SJ", "Svalbard & Jan Mayen");
	    COUNTRY_MAP.put("SK", "Slovakia");
	    COUNTRY_MAP.put("SL", "Sierra Leone");
	    COUNTRY_MAP.put("SM", "San Marino");
	    COUNTRY_MAP.put("SN", "Senegal");
	    COUNTRY_MAP.put("SO", "Somalia");
	    COUNTRY_MAP.put("SR", "Suriname");
	    COUNTRY_MAP.put("SS", "South Sudan");
	    COUNTRY_MAP.put("ST", "São Tomé & Príncipe");
	    COUNTRY_MAP.put("SV", "El Salvador");
	    COUNTRY_MAP.put("SX", "Sint Maarten");
	    COUNTRY_MAP.put("SY", "Syria");
	    COUNTRY_MAP.put("SZ", "Eswatini");

	    COUNTRY_MAP.put("TC", "Turks & Caicos Islands");
	    COUNTRY_MAP.put("TD", "Chad");
	    COUNTRY_MAP.put("TF", "French Southern Territories");
	    COUNTRY_MAP.put("TG", "Togo");
	    COUNTRY_MAP.put("TH", "Thailand");
	    COUNTRY_MAP.put("TJ", "Tajikistan");
	    COUNTRY_MAP.put("TK", "Tokelau");
	    COUNTRY_MAP.put("TL", "Timor-Leste");
	    COUNTRY_MAP.put("TM", "Turkmenistan");
	    COUNTRY_MAP.put("TN", "Tunisia");
	    COUNTRY_MAP.put("TO", "Tonga");
	    COUNTRY_MAP.put("TR", "Turkey");
	    COUNTRY_MAP.put("TT", "Trinidad & Tobago");
	    COUNTRY_MAP.put("TV", "Tuvalu");
	    COUNTRY_MAP.put("TW", "Taiwan");
	    COUNTRY_MAP.put("TZ", "Tanzania");

	    COUNTRY_MAP.put("UA", "Ukraine");
	    COUNTRY_MAP.put("UG", "Uganda");
	    COUNTRY_MAP.put("UM", "U.S. Outlying Islands");
	    COUNTRY_MAP.put("US", "United States");
	    COUNTRY_MAP.put("UY", "Uruguay");
	    COUNTRY_MAP.put("UZ", "Uzbekistan");

	    COUNTRY_MAP.put("VA", "Vatican City");
	    COUNTRY_MAP.put("VC", "St. Vincent & Grenadines");
	    COUNTRY_MAP.put("VE", "Venezuela");
	    COUNTRY_MAP.put("VG", "British Virgin Islands");
	    COUNTRY_MAP.put("VI", "U.S. Virgin Islands");
	    COUNTRY_MAP.put("VN", "Vietnam");
	    COUNTRY_MAP.put("VU", "Vanuatu");

	    COUNTRY_MAP.put("WF", "Wallis & Futuna");
	    COUNTRY_MAP.put("WS", "Samoa");

	    COUNTRY_MAP.put("YE", "Yemen");
	    COUNTRY_MAP.put("YT", "Mayotte");

	    COUNTRY_MAP.put("ZA", "South Africa");
	    COUNTRY_MAP.put("ZM", "Zambia");
	    COUNTRY_MAP.put("ZW", "Zimbabwe");

	    COUNTRY_MAP.put("XK", "Kosovo");
	}
}
