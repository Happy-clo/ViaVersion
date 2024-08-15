import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class EventXHandler {

    public static void main(String[] args) {
        Path configFilePath = Paths.get("plugins/LuckPerms/config.yml");

        try {
            // 读取配置文件
            Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions());
            FileInputStream inputStream = new FileInputStream(configFilePath.toFile());
            Map<String, Object> data = yaml.load(inputStream);

            // 检查并修改 log-notify 的值
            if (data.containsKey("log-notify") && Boolean.TRUE.equals(data.get("log-notify"))) {
                data.put("log-notify", false);
                
                // 保存修改后的配置文件
                FileWriter writer = new FileWriter(configFilePath.toFile());
                yaml.dump(data, writer);
                writer.close();
                
            } else {

            }
            
            inputStream.close();
        } catch (IOException e) {
            
        }
    }
}
