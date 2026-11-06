package verification;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.nio.file.*;
import java.util.zip.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.*;

/** Reads real class bytes without launching or initializing Minecraft. */
public final class StandaloneMixinService extends MixinServiceAbstract
        implements IClassProvider, IClassBytecodeProvider, IClassTracker {
    private static final ClassLoader LOADER = StandaloneMixinService.class.getClassLoader();
    public String getName() { return "offline-verification"; }
    public boolean isValid() { return true; }
    public MixinEnvironment.Phase getInitialPhase() { return MixinEnvironment.Phase.DEFAULT; }
    public IClassProvider getClassProvider() { return this; }
    public IClassBytecodeProvider getBytecodeProvider() { return this; }
    public ITransformerProvider getTransformerProvider() { return null; }
    public IClassTracker getClassTracker() { return this; }
    public IMixinAuditTrail getAuditTrail() { return null; }
    public IFeatureValidator getFeatureValidator() { return IFeatureValidator.ALLOW_ALL; }
    public IAdviceProvider getAdviceProvider() { return IAdviceProvider.GENERIC; }
    public Collection<String> getPlatformAgents() { return Collections.emptyList(); }
    public IContainerHandle getPrimaryContainer() { return new ContainerHandleVirtual("verification"); }
    public InputStream getResourceAsStream(String name) { return LOADER.getResourceAsStream(name); }
    public URL[] getClassPath() { return new URL[0]; }
    public Class<?> findClass(String name) throws ClassNotFoundException { return findClass(name,false); }
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException { return Class.forName(name,initialize,LOADER); }
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException { return findClass(name,initialize); }
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException { return getClassNode(name,false,0); }
    public ClassNode getClassNode(String name, boolean transform) throws ClassNotFoundException, IOException { return getClassNode(name,transform,0); }
    public ClassNode getClassNode(String name, boolean transform, int flags) throws ClassNotFoundException, IOException {
        ClassNode node = new ClassNode();
        new ClassReader(readClass(name)).accept(node,flags);
        return node;
    }
    public static byte[] readClass(String name) throws IOException, ClassNotFoundException {
        String resource = name.replace('.','/')+".class";
        // ART preserves Mojang's old signatures; use ZIP bytes after remapping.
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Path p = Path.of(entry);
            if (Files.isDirectory(p)) {
                if (Files.isRegularFile(p.resolve(resource))) return Files.readAllBytes(p.resolve(resource));
            } else if (Files.isRegularFile(p)) {
                try (ZipFile zip = new ZipFile(p.toFile())) {
                    ZipEntry e = zip.getEntry(resource);
                    if (e != null) return zip.getInputStream(e).readAllBytes();
                }
            }
        }
        try (InputStream in = LOADER.getResourceAsStream(resource)) {
            if (in != null) return in.readAllBytes();
        }
        throw new ClassNotFoundException(name);
    }
    public void registerInvalidClass(String name) {}
    public boolean isClassLoaded(String name) { return false; }
    public String getClassRestrictions(String name) { return ""; }
}
