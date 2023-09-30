package yakusu.android.nlp;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {

	public static void handleStaticAnalysisLimitations(String appName, JSONArray ontologyArray){
		if(appName.equals("org.wordpress.android")){
			String settingsString = "{"
					+ "\"full_class_name\":\"org.wordpress.android.widgets.WPTextView\","
					+ "\"class_name\":\"WPTextView\","
					+ "\"class_name_keywords\":[\"WP\",\"Text\",\"View\"],"
					+ "\"class_name_lemmas\":[\"wp\",\"text\",\"view\"],"
					+ "\"text\":\"Settings\","
					+ "\"text_keywords\":[\"settings\"],"
					+ "\"text_lemmas\":[\"settings\"],"
					+ "\"text_reference\":\"my_site_btn_site_settings\","
					+ "\"text_reference_keywords\":[\"my\",\"site\",\"btn\",\"site\",\"settings\"],"
					+ "\"text_reference_lemmas\":[\"my\",\"site\",\"btn\",\"site\",\"settings\"],"
					+ "\"resource_id\":\"2131821204\","
					+ "\"resource_id_reference\":\"my_site_settings_text_view\","
					+ "\"resource_id_reference_lemmas\":[\"my\",\"site\",\"settings\",\"text\",\"view\"],"
					+ "\"resource_id_reference_keywords\":[\"my\",\"site\",\"settings\",\"text\",\"view\"],"
					+ "\"image_reference\":\"\","
					+ "\"image_reference_keywords\":[],"
					+ "\"image_reference_lemmas\":[]"
					+ "}";
			JSONObject settingsOntology = new JSONObject(settingsString);
			ontologyArray.put(settingsOntology);
			String moreString = "{"
					+ "\"full_class_name\":\"org.wordpress.android.ui.prefs.WPPreference\","
					+ "\"class_name\":\"WPPreference\","
					+ "\"class_name_keywords\":[\"WP\",\"Preference\"],"
					+ "\"class_name_lemmas\":[\"wp\",\"preference\"],"
					+ "\"text\":\"More\","
					+ "\"text_keywords\":[\"more\"],"
					+ "\"text_lemmas\":[\"more\"],"
					+ "\"text_reference\":\"site_settings_more_title\","
					+ "\"text_reference_keywords\":[\"site\",\"setting\",\"more\",\"title\"],"
					+ "\"text_reference_lemmas\":[\"site\",\"setting\",\"more\",\"title\"],"
					+ "\"resource_id\":\"2131297695\","
					+ "\"resource_id_reference\":\"pref_more_discussion_settings\","
					+ "\"resource_id_reference_keywords\":[\"pref\",\"more\",\"discussion\",\"settings\"],"
					+ "\"resource_id_reference_lemmas\":[\"pref\",\"more\",\"discussion\",\"settings\"],"
					+ "\"image_reference\":\"\","
					+ "\"image_reference_keywords\":[],"
					+ "\"image_reference_lemmas\":[]"
					+ "}";
			JSONObject moreOntology = new JSONObject(moreString);
			ontologyArray.put(moreOntology);
			String holdForModerationString = "{"
					+ "\"full_class_name\":\"org.wordpress.android.ui.prefs.WPPreference\","
					+ "\"class_name\":\"WPPreference\","
					+ "\"class_name_keywords\":[\"WP\",\"Preference\"],"
					+ "\"class_name_lemmas\":[\"wp\",\"preference\"],"
					+ "\"text\":\"Hold for Moderation\","
					+ "\"text_keywords\":[\"hold\",\"for\",\"moderation\"],"
					+ "\"text_lemmas\":[\"hold\",\"for\",\"moderation\"],"
					+ "\"text_reference\":\"site_settings_moderation_hold_title\","
					+ "\"text_reference_keywords\":[\"site\",\"settings\",\"moderation\",\"hold\",\"title\"],"
					+ "\"text_reference_lemmas\":[\"site\",\"setting\",\"moderation\",\"hold\",\"title\"],"
					+ "\"resource_id\":\"2131297196\","
					+ "\"resource_id_reference\":\"pref_moderation_hold\","
					+ "\"resource_id_reference_keywords\":[\"pref\",\"moderation\",\"hold\"],"
					+ "\"resource_id_reference_lemmas\":[\"pref\",\"moderation\",\"hold\"],"
					+ "\"image_reference\":\"\","
					+ "\"image_reference_keywords\":[],"
					+ "\"image_reference_lemmas\":[]"
					+ "}";
			JSONObject holdForModerationOntology = new JSONObject(holdForModerationString);
			ontologyArray.put(holdForModerationOntology);
		}
		else if(appName.equals("de.uni_potsdam.hpi.openmensa")){
			String favouriteCanteensString = "{"
					+ "\"full_class_name\":\"de.uni_potsdam.hpi.openmensa.api.preferences.SelectFavouritesPreference\","
					+ "\"class_name\":\"SelectFavouritesPreference\","
					+ "\"class_name_keywords\":[\"Select\",\"Favourites\",\"Preference\"],"
					+ "\"class_name_lemmas\":[\"select\",\"favourites\",\"preference\"],"
					+ "\"text\":\"Favourite Canteens\","
					+ "\"text_keywords\":[\"Favourite\",\"Canteens\"],"
					+ "\"text_lemmas\":[\"favourite\",\"canteen\"],"
					+ "\"text_reference\":\"canteen_title\","
					+ "\"text_reference_keywords\":[\"canteen\",\"title\"],"
					+ "\"text_reference_lemmas\":[\"canteen\",\"title\"],"
					+ "\"resource_id\":\"2131034121\","
					+ "\"resource_id_reference\":\"\","
					+ "\"resource_id_reference_keywords\":[],"
					+ "\"resource_id_reference_lemmas\":[],"
					+ "\"image_reference\":\"\","
					+ "\"image_reference_keywords\":[],"
					+ "\"image_reference_lemmas\":[]"
					+ "}";
			JSONObject favouriteCanteensOntology = new JSONObject(favouriteCanteensString);
			ontologyArray.put(favouriteCanteensOntology);
		}
		else if(appName.equals("org.odk.collect.android")){
			String serverString = "{"
				+"\"full_class_name\":\"Preference\","
				+"\"class_name\":\"Preference\","
				+"\"class_name_keywords\":[\"Preference\"],"
				+"\"class_name_lemmas\":[\"preference\"],"
				+"\"text\":\"Server\","
				+"\"text_keywords\":[\"Server\"],"
				+"\"text_lemmas\":[\"server\"],"
				+"\"text_reference\":\"server_settings_title\","
				+"\"text_reference_keywords\":[\"server\",\"settings\",\"title\"],"
				+"\"text_reference_lemmas\":[\"server\",\"settings\",\"title\"],"
				+"\"resource_id\":\"2131231299\","
				+"\"resource_id_reference\":\"\","
				+"\"resource_id_reference_keywords\":[],"
				+"\"resource_id_reference_lemmas\":[],"
				+"\"image_reference\":\"\","
				+"\"image_reference_keywords\":[],"
				+"\"image_reference_lemmas\":[]"
				+"}";
			JSONObject serverOntology = new JSONObject(serverString);
			ontologyArray.put(serverOntology);
		}
	}	
}
