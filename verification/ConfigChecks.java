import dev.fixedvoiceport.PortConfig;
import java.nio.file.*;
public class ConfigChecks {
 public static void main(String[] args) throws Exception {
  if(PortConfig.load()!=24454)throw new AssertionError("default");
  Path p=Path.of("config/fixed-voice-port.properties");
  Files.writeString(p,"port=54563\nextra=retained\n");
  if(PortConfig.load()!=54563)throw new AssertionError("migration");
  PortConfig.save(60714);
  if(PortConfig.load()!=60714 || !Files.readString(p).contains("extra=retained"))throw new AssertionError("save");
  for(int x:new int[]{0,-1,65536}) {
   try {PortConfig.save(x);throw new AssertionError("invalid accepted");}
   catch(IllegalArgumentException expected) {}
  }
  if(PortConfig.load()!=60714)throw new AssertionError("invalid changed config");
  PortConfig.save(1);PortConfig.save(65535);
  if(PortConfig.load()!=65535)throw new AssertionError("bounds");
  System.out.println("PASS: default, existing-config migration, save/load, extra-key preservation, bounds, invalid-value rejection");
 }
}
