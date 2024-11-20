package jadx.plugins.xvision;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginInfo;
import jadx.core.plugins.PluginContext;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;

public class XVisionPluginTest {

    @Test
    public void integrationTest() {
        URL resource = getClass().getResource("/samples/helloworld.jar");
        assertThat(resource).isNotNull();
        File testJar = new File(resource.getFile());

        JadxArgs args = new JadxArgs();
        args.getInputFiles().add(testJar);

        try (JadxDecompiler decompiler = new JadxDecompiler(args)) {
            decompiler.load();
            SortedSet<PluginContext> pluginContexts = decompiler.getPluginManager().getResolvedPluginContexts();
            boolean pluginLoaded = pluginContexts.stream()
                    .anyMatch(context -> context.getPluginInstance() instanceof XVisionPlugin);
            assertThat(pluginLoaded).isTrue();

             PluginContext context = pluginContexts.stream()
                    .filter(ctx -> ctx.getPluginInstance() instanceof XVisionPlugin)
                    .findFirst()
                    .orElseThrow();
            JadxPlugin plugin = (JadxPlugin) context.getPluginInstance();
            assertThat(plugin).isInstanceOf(XVisionPlugin.class);

            JadxPluginInfo info = plugin.getPluginInfo();
            assertThat(info.getPluginId()).isEqualTo("xvision-plugin");
            assertThat(info.getName()).isEqualTo("xVision Plugin");
            assertThat(info.getDescription()).isEqualTo("xVision: LLM integration for code analysis");
        }
    }
}
