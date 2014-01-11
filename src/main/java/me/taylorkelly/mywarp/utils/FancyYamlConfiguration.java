package me.taylorkelly.mywarp.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.ScalarAnalysis;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

/**
 * A custom implementation of {@link YamlConfiguration} which fixes saving
 * multi-line strings. This change only needs a small modification on the
 * DumperOptions. The rest of code is completely mirrored from bukkit's default
 * implementation but needed due to restricted constructors or methods.
 * 
 * @see <a href="https://bukkit.atlassian.net/browse/BUKKIT-48">BUKKIT-48</a>
 */
public class FancyYamlConfiguration extends YamlConfiguration {
    private final DumperOptions yamlOptions = new FancyDumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        String header = buildHeader();
        String dump = yaml.dump(getValues(false));

        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return header + dump;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        try {
            input = (Map<?, ?>) yaml.load(contents);
        } catch (YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        String header = parseHeader(contents);
        if (header.length() > 0) {
            options().header(header);
        }

        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    /**
     * Creates a new {@link FancyYamlConfiguration}, loading from the given
     * file.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored. If
     * the specified input is not a valid config, a blank config will be
     * returned.
     * 
     * @param file
     *            Input file
     * @return Resulting configuration
     * @throws IllegalArgumentException
     *             Thrown if file is null
     */
    public static FancyYamlConfiguration loadConfiguration(File file) {
        Validate.notNull(file, "File cannot be null");

        FancyYamlConfiguration config = new FancyYamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }

        return config;
    }

    /**
     * Creates a new {@link FancyYamlConfiguration}, loading from the given
     * stream.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored. If
     * the specified input is not a valid config, a blank config will be
     * returned.
     * 
     * @param stream
     *            Input stream
     * @return Resulting configuration
     * @throws IllegalArgumentException
     *             Thrown if stream is null
     */
    public static FancyYamlConfiguration loadConfiguration(InputStream stream) {
        Validate.notNull(stream, "Stream cannot be null");

        FancyYamlConfiguration config = new FancyYamlConfiguration();

        try {
            config.load(stream);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
        }

        return config;
    }

    // This will be included in snakeyaml 1.10, but until then we have to do it
    // manually.
    private class FancyDumperOptions extends DumperOptions {
        @Override
        public DumperOptions.ScalarStyle calculateScalarStyle(ScalarAnalysis analysis,
                DumperOptions.ScalarStyle style) {
            if (analysis.scalar.contains("\n") || analysis.scalar.contains("\r")) {
                return ScalarStyle.LITERAL;
            } else {
                return super.calculateScalarStyle(analysis, style);
            }
        }
    }
}
