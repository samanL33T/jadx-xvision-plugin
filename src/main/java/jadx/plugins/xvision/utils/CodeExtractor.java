package jadx.plugins.xvision.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeExtractor {
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```java\\s*\\n(.*?)```", Pattern.DOTALL);

    public static String extractJavaCode(String llmResponse) {
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(llmResponse);
        StringBuilder extractedCode = new StringBuilder();
        
        while (matcher.find()) {
            String code = matcher.group(1);
            extractedCode.append(code).append("\n\n");
        }
        
        return extractedCode.toString().trim();
    }
}