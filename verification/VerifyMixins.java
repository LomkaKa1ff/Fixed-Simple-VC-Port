package verification;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

public final class VerifyMixins {
    public static void main(String[] args) throws Exception {
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        Mixins.addConfiguration("fixed_voice_port.mixins.json");
        Class<?> type=Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
        Constructor<?> ctor=type.getDeclaredConstructor();ctor.setAccessible(true);
        IMixinTransformer transformer=(IMixinTransformer)ctor.newInstance();
        for (String target: args) {
            byte[] original=StandaloneMixinService.readClass(target);
            byte[] transformed=transformer.transformClassBytes(target,target,original);
            if (Arrays.equals(original,transformed)) throw new AssertionError("No mixin applied to "+target);
            ClassNode node=new ClassNode();new ClassReader(transformed).accept(node,0);
            int hooks=0;
            for (MethodNode m:node.methods) if (m.name.contains("fixedVoicePort$")) hooks++;
            if (hooks<3) throw new AssertionError("Missing injected methods: "+target);
            Path out=Path.of(System.getProperty("verify.output"),target.replace('.','/')+".class");
            Files.createDirectories(out.getParent());Files.write(out,transformed);
            System.out.println("TRANSFORMED "+target+" ("+hooks+" hooks)");
        }
    }
}
