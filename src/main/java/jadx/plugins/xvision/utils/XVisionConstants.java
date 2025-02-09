package jadx.plugins.xvision.utils;

public final class XVisionConstants {

    public static final String PREF_DEFAULT_PROMPT = "defaultPrompt";
    public static final String GPT4_SERVICE = "GPT-4o";
    public static final String CLAUDE_SERVICE = "Claude-Sonnet-latest";
    public static final String CUSTOM_SERVICE = "Custom";

    // API Endpoints
    public static final String OPENAI_API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    public static final String CLAUDE_API_ENDPOINT = "https://api.anthropic.com/v1/messages";

    // Model Names
    public static final String GPT4_MODEL = "gpt-4o";
    public static final String CLAUDE_MODEL = "claude-3-5-sonnet-latest";

        // DeepSeek R1 Constants
    public static final String DEEPSEEK_R1_SERVICE = "DeepSeek-R1";
    public static final String DEEPSEEK_R1_MODEL = "deepseek-reasoner";

    // DeepSeek V3 Constants
    public static final String DEEPSEEK_V3_SERVICE = "DeepSeek-V3";
    public static final String DEEPSEEK_V3_MODEL = "deepseek-chat";

    // Common DeepSeek endpoint
    public static final String DEEPSEEK_API_ENDPOINT = "https://api.deepseek.com/v1/chat/completions";

    // System prompt
    public static final String SYSTEM_CONTENT = "You are an expert Java Developer & Reverse Engineer.";

    public static final String PREF_SELECTED_LLM = "selectedLLM";
    public static final String PREF_API_KEY = "apiKey";
    public static final String PREF_CUSTOM_ENDPOINT = "customEndpoint";

    // Default Values
    public static final String DEFAULT_LLM = GPT4_SERVICE;
    public static final String DEFAULT_API_KEY = "";
    public static final String DEFAULT_CUSTOM_ENDPOINT = "";

    // Ref: https://github.com/skylot/jadx/issues/1884#issue-1727047157
    public static final String DEFAULT_PROMPT_TEMPLATE = """
            Let the variable names and method names of the following code change as the name implies, the original meaning of the code cannot be changed, the order cannot be changed, and the unprocessed ones remain as they are, the number of lines of the code cannot be optimized, the code cannot be omitted, the code cannot be deleted or added, and the naming conflict cannot be allowed . The original name should be written above them in the form of a comment, keep the comment. Line comments must be added to Each line of code to explain the meaning of the code, and comments between multiple lines of code also need to be marked.
            """;

    private XVisionConstants() {
        // Prevent instantiation
    }
}