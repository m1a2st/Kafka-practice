package io.demos.kafka.configprovider;

import org.apache.kafka.common.config.ConfigData;
import org.apache.kafka.common.config.provider.ConfigProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapConfigProvider implements ConfigProvider {
    @Override
    public ConfigData get(String s) {
        Map<String, String> data = new HashMap<>();

        data.put("server", "localhost:9092");

        return new ConfigData(data);
    }

    @Override
    public ConfigData get(String s, Set<String> keys) {
        Map<String, String> data = new HashMap<>();

        if (keys.contains("server")) {
            data.put("server", "localhost:9092");
        }

        return new ConfigData(data);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
