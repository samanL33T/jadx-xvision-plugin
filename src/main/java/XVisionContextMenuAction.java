// Ref: https://github.com/Devilx86/jadx-ai-view-plugin/blob/main/src/main/java/jadx/plugins/aiview/JadxAiViewAction.java

package jadx.plugins.xvision;

import jadx.api.JavaNode;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.gui.JadxGuiContext;
import java.util.function.Consumer;
import jadx.api.metadata.ICodeAnnotation;

public class XVisionContextMenuAction implements Consumer<ICodeNodeRef> {
    private final JadxGuiContext guiContext;
    private final XVisionPlugin plugin;

    public XVisionContextMenuAction(JadxGuiContext guiContext, XVisionPlugin plugin) {
        this.guiContext = guiContext;
        this.plugin = plugin;
    }

    @Override
    public void accept(ICodeNodeRef nodeRef) {
        JavaNode node = plugin.getContext().getDecompiler().getJavaNodeByRef(nodeRef);
        if (node != null) {
            String code = plugin.getCode(node);
            if (code != null) {
                plugin.analyzeCode(code);
            }
        }
    }

    public static void addToContextMenu(JadxGuiContext guiContext, XVisionPlugin plugin) {
        XVisionContextMenuAction action = new XVisionContextMenuAction(guiContext, plugin);
        guiContext.addPopupMenuAction("Analyze with xVision", 
            XVisionContextMenuAction::canActivate, 
            null, 
            action);
    }

   public static Boolean canActivate(ICodeNodeRef ref) {
        return ref != null && (
            ref.getAnnType() == ICodeAnnotation.AnnType.METHOD || ref.getAnnType() == ICodeAnnotation.AnnType.CLASS
        );
    }
}
