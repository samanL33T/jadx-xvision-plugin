package jadx.plugins.xvision;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.plugins.xvision.utils.XVisionConstants;

public class PreferencesManager {
    private static final String PREF_KEY_LLM_TYPE = "xvision.llmType";
    private static final String PREF_KEY_CUSTOM_ENDPOINT = "xvision.customEndpoint";
    private static final String PREF_KEY_API_KEY = "xvision.apiKey";
    private static final String PREF_KEY_DEFAULT_PROMPT = "xvision.defaultPrompt";

    private static JadxArgs jadxArgs;

    public static void initializeJadxArgs(JadxDecompiler decompiler) {
        jadxArgs = decompiler.getArgs();
    }

    public static String getPreference(String key, String defaultValue) {
        if (jadxArgs == null) {
            return defaultValue;
        }
        return jadxArgs.getPluginOptions().getOrDefault(key, defaultValue);
    }

    public static void setPreference(String key, String value) {
        if (jadxArgs != null) {
            jadxArgs.getPluginOptions().put(key, value);
        }
    }

    public static String getLLMType() {
        return getPreference(PREF_KEY_LLM_TYPE, XVisionConstants.DEFAULT_LLM);
    }

    public static void setLLMType(String llmType) {
        setPreference(PREF_KEY_LLM_TYPE, llmType);
    }


    public static String getCustomEndpoint() {
        return getPreference(PREF_KEY_CUSTOM_ENDPOINT, XVisionConstants.DEFAULT_CUSTOM_ENDPOINT);
    }

    public static void setCustomEndpoint(String customEndpoint) {
        setPreference(PREF_KEY_CUSTOM_ENDPOINT, customEndpoint);
    }

    public static String getApiKey() {
        return getPreference(PREF_KEY_API_KEY, XVisionConstants.DEFAULT_API_KEY);
    }

    public static void setApiKey(String apiKey) {
        setPreference(PREF_KEY_API_KEY, apiKey);
    }

    public static String getDefaultPrompt() {
        return getPreference(PREF_KEY_DEFAULT_PROMPT, XVisionConstants.DEFAULT_PROMPT_TEMPLATE);
    }

    public static void setDefaultPrompt(String defaultPrompt) {
        setPreference(PREF_KEY_DEFAULT_PROMPT, defaultPrompt);
    }
}