package xyz.realplussmp.bounty.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class MessageUtil {

    private static MiniMessage mm;
    private static FileConfiguration messages;

    public static void init(FileConfiguration config) {
        mm = MiniMessage.miniMessage();
        messages = config;
    }

    public static Component get(String key, Map<String, String> placeholders) {
        String raw = messages.getString(key, "<red>Missing message: " + key + "</red>");
        for (var entry : placeholders.entrySet()) {
            raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return reset(mm.deserialize(raw));
    }

    public static Component get(String key) {
        return reset(mm.deserialize(messages.getString(key, "<red>Missing message: " + key + "</red>")));
    }

    public static List<Component> getList(String key, Map<String, String> placeholders) {
        return messages.getStringList(key).stream().map(raw -> {
            for (var entry : placeholders.entrySet()) {
                raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            return reset(mm.deserialize(raw));
        }).toList();
    }

    public static List<Component> getList(String key) {
        return messages.getStringList(key).stream()
                .map(mm::deserialize)
                .map(MessageUtil::reset)
                .toList();
    }

    private static Component reset(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
