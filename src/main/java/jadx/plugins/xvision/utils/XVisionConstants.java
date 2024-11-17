package jadx.plugins.xvision.utils;

public final class XVisionConstants {
    // LLM Service Names
    public static final String GPT4_SERVICE = "GPT-4";
    public static final String CLAUDE_SERVICE = "Claude";
    public static final String CUSTOM_SERVICE = "Custom";

    // API Endpoints
    public static final String OPENAI_API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    public static final String CLAUDE_API_ENDPOINT = "https://api.anthropic.com/v1/messages";

    // Model Names
    public static final String GPT4_MODEL = "gpt-4o";
    public static final String CLAUDE_MODEL = "claude-3-5-sonnet-20241022";

    // System prompt
    public static final String SYSTEM_CONTENT = "You are a helpful assistant.";

    // Preference Keys
    public static final String PREF_SELECTED_LLM = "selectedLLM";
    public static final String PREF_API_KEY = "apiKey";
    public static final String PREF_CUSTOM_ENDPOINT = "customEndpoint";

    // Default Values
    public static final String DEFAULT_LLM = GPT4_SERVICE;
    public static final String DEFAULT_API_KEY = "";
    public static final String DEFAULT_CUSTOM_ENDPOINT = "";

    private XVisionConstants() {
        // Prevent instantiation
    }
}