package rpgclasses;

import necesse.engine.GlobalData;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class Config {

    public static class ConfigOption<T> {
        private final String name;
        private final String description;
        private final Class<T> type;
        private final T defaultValue;
        private T value;

        public ConfigOption(String name, String description, Class<T> type, T defaultValue) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public String getName() {
            if (name != null && name.contains("-")) {
                String[] parts = name.split("-");
                if (parts.length > 1) {
                    return parts[1];
                }
            }
            return name;
        }

        public String getCategory() {
            if (name != null && name.contains("-")) {
                String[] parts = name.split("-");
                if (parts.length > 1) {
                    return parts[0];
                }
            }
            return null;
        }

        public void setValue(String value) {
            try {
                Method valueOfMethod = type.getMethod("valueOf", String.class);
                if (Modifier.isStatic(valueOfMethod.getModifiers())) {
                    this.value = type.cast(valueOfMethod.invoke(null, value));
                    return;
                }
            } catch (Exception ignored) {
            }

            try {
                Constructor<T> constructor = type.getConstructor(String.class);
                this.value = constructor.newInstance(value);
                return;
            } catch (Exception ignored) {
            }

            Object parsedValue = parsePrimitive(type, value);
            if (parsedValue != null) {
                this.value = (T) parsedValue;
                return;
            }

            throw new IllegalArgumentException("Could not convert string to " + type.getName());
        }

        private Object parsePrimitive(Class<?> type, String value) {
            try {
                if (type == int.class || type == Integer.class) return Integer.parseInt(value);
                if (type == double.class || type == Double.class) return Double.parseDouble(value);
                if (type == float.class || type == Float.class) return Float.parseFloat(value);
                if (type == long.class || type == Long.class) return Long.parseLong(value);
                if (type == short.class || type == Short.class) return Short.parseShort(value);
                if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
                if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
                if (type == char.class || type == Character.class) return value.charAt(0);
            } catch (Exception e) {
                return null;
            }
            return null;
        }

    }

    public static ConfigOption<?>[] configOptions = new ConfigOption[]{
            new ConfigOption<>(
                    "exp-experienceMod",
                    "Increase or decrease this number to adjust the amount of experience players receive. 2 means they will earn double experience, while 0.5 means they will earn half",
                    float.class, 1F
            ),
            new ConfigOption<>(
                    "exp-firstKillBonus",
                    "Experience bonus when killing a new mob, only the first time. 5 means 5 times more than normal",
                    float.class, 5F
            ),
            new ConfigOption<>(
                    "exp-bossKillBonus",
                    "Experience bonus when killing a boss. 5 means 5 times more than normal",
                    float.class, 5F
            ),
            new ConfigOption<>(
                    "exp-startingExperience",
                    "The experience players start with",
                    int.class, 300
            ),
            new ConfigOption<>(
                    "exp-firstExperienceReq",
                    "The experience required to level up from level 0 to level 1. It also sets the base for all levels",
                    int.class, 300
            ),
            new ConfigOption<>(
                    "exp-experienceReqInc",
                    "The experience requirement will increase by adding this number each time you level up. 0 to no changes",
                    int.class, 60
            ),
            new ConfigOption<>(
                    "exp-squareExperienceReqInc",
                    "The experience requirement will increase by multiplying this number by the square of the level each time you level up. 0 to no changes",
                    int.class, 30
            ),
            new ConfigOption<>(
                    "exp-cubeExperienceReqInc",
                    "The experience requirement will increase by multiplying this number by the cube of the level each time you level up. 0 to no changes",
                    int.class, 3
            )
    };

    public static void startConfig() {
        String filename = GlobalData.rootPath() + "settings\\rpgmod\\settings.cfg";
        System.out.println("Loading RPG Mod settings");
        try {
            File file = new File(filename);
            if (!file.exists()) createNewFile(file);

            InputStreamReader isr = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            boolean createFile = loadConfig(br);
            br.close();
            if (createFile) recreateFile(file);

            System.out.println("Loaded file: " + file.toPath());

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


    private static boolean loadConfig(BufferedReader br) throws IOException {
        String line;
        line = br.readLine();
        if (shouldUpdateFile(line)) {
            return true;
        }
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty() && !line.startsWith("#")) {
                String[] temp = line.split("=");
                if (temp.length < 2) continue;
                String key = temp[0].trim();
                String value = temp[1].trim();

                for (ConfigOption<?> option : configOptions) {
                    if (option.getName().equals(key)) {
                        option.setValue(value);
                        break;
                    }
                }
            }
        }
        return false;
    }

    private static boolean shouldUpdateFile(String firstLine) {
        if (firstLine == null) return true;

        firstLine = firstLine.trim();

        if (firstLine.startsWith("#") && firstLine.toLowerCase().contains("no-update")) {
            return false;
        }

        if (firstLine.startsWith("# v")) {
            String versionStr = firstLine.substring(3).trim();
            return isOlderVersion(versionStr);
        } else if (firstLine.startsWith("#v")) {
            String versionStr = firstLine.substring(2).trim();
            return isOlderVersion(versionStr);
        }

        return true;
    }

    private static boolean isOlderVersion(String current) {
        String[] currParts = current.split("\\.");
        int[] refParts = new int[]{0, 1};

        for (int i = 0; i < Math.max(currParts.length, refParts.length); i++) {
            int curr = 0;
            if (i < currParts.length) {
                try {
                    curr = Integer.parseInt(currParts[i].trim());
                } catch (NumberFormatException ignored) {
                }
            }
            int ref = i < refParts.length ? refParts[i] : 0;

            if (curr < ref) return true;
            if (curr > ref) return false;
        }

        return false;
    }

    public static void recreateFile(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Could not delete existing file: " + file.toPath());
            }
        }

        createNewFile(file);
    }

    private static void createNewFile(File file) throws IOException {
        if (!file.getParentFile().mkdirs() && !file.createNewFile()) {
            throw new IOException("Error creating file: " + file.toPath());
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            writer.write("# " + RPGMod.currentVersion);
            writer.write("\n# ↑↑↑ DO NOT MODIFY unless: To prevent auto-updates when this file becomes obsolete, change the first version line to \"# no-update\"");

            writer.write("\n\n######################");
            writer.write("\n# EXPERIENCE OPTIONS #");
            writer.write("\n######################");

            for (ConfigOption<?> configOption : configOptions) {
                if (Objects.equals(configOption.getCategory(), "exp")) {
                    writer.write("\n\n# " + configOption.description);
                    writer.write("\n# Default: " + configOption.defaultValue);
                    writer.write("\n" + configOption.getName() + "=" + configOption.defaultValue);
                }
            }

            writer.write("\n\n# The formula for the experience required to level up is:");
            writer.write("\n# firstExperienceReq + experienceReqInc * level + squareExperienceReqInc * Math.pow(level, 2) + cubeExperienceReqInc * Math.pow(level, 3)");
            writer.write("\n# Where 'level' is your actual level");

            writer.write("\n\n# Delete this file and run the game to return to the default mod config");

            System.out.println("Created file: " + file.toPath());
        }
    }

    public static float getExperienceMod() {
        return (float) configOptions[0].value;
    }

    public static float getFirstKillBonus() {
        return (float) configOptions[1].value;
    }

    public static float getBossKillBonus() {
        return (float) configOptions[2].value;
    }

    public static int getStartingExperience() {
        return (int) configOptions[3].value;
    }

    public static int getFirstExperienceReq() {
        return (int) configOptions[4].value;
    }

    public static int getExperienceReqInc() {
        return (int) configOptions[5].value;
    }

    public static int getSquareExperienceReqInc() {
        return (int) configOptions[6].value;
    }

    public static int getCubeExperienceReqInc() {
        return (int) configOptions[7].value;
    }
}
