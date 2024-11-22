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
        System.out.println("Initializing jadxArgs: " + (jadxArgs != null ? "success" : "null"));
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

    public static boolean hasCustomDefaultPrompt() {
        if (jadxArgs == null) {
            return false;
        }
        String savedPrompt = jadxArgs.getPluginOptions().get(PREF_KEY_DEFAULT_PROMPT);
        return savedPrompt != null && !savedPrompt.isEmpty();
    }
    
    public static String getDefaultPrompt() {
        System.out.println("Getting default prompt");
        System.out.println("jadxArgs is: " + (jadxArgs != null ? "not null" : "null"));
        if (hasCustomDefaultPrompt()) {
            String prompt = getPreference(PREF_KEY_DEFAULT_PROMPT, XVisionConstants.DEFAULT_PROMPT_TEMPLATE);
            System.out.println("Retrieved custom prompt: " + prompt);
            return prompt;
        }
        System.out.println("Using default template prompt");
        return XVisionConstants.DEFAULT_PROMPT_TEMPLATE;
    }

    public static void setDefaultPrompt(String defaultPrompt) {
        System.out.println("Attempting to save default prompt: " + defaultPrompt);
        System.out.println("jadxArgs is: " + (jadxArgs != null ? "not null" : "null"));
        if (jadxArgs != null) {
            jadxArgs.getPluginOptions().put(PREF_KEY_DEFAULT_PROMPT, defaultPrompt);
            System.out.println("Default prompt saved successfully");
        } else {
            System.out.println("Failed to save default prompt - jadxArgs is null");
        }
    }
}