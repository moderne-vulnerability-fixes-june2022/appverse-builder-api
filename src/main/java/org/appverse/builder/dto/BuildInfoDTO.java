package org.appverse.builder.dto;

/**
 * Created by panthro on 29/12/15.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildInfoDTO {

    @JsonProperty("name")
    private String name;
    @JsonProperty("engine")
    private EngineInfoDTO engine;

    /**
     * @return The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The engine
     */
    @JsonProperty("engine")
    public EngineInfoDTO getEngine() {
        return engine;
    }

    /**
     * @param engine The engine
     */
    @JsonProperty("engine")
    public void setEngine(EngineInfoDTO engine) {
        this.engine = engine;
    }

    @Override
    public String toString() {
        return "BuildInfoDTO{" +
            "name='" + name + '\'' +
            ", engine=" + engine +
            '}';
    }

    public static class EngineInfoDTO {

        @JsonProperty("name")
        private String name;
        @JsonProperty("version")
        private String version;
        @JsonProperty("platforms")
        private List<PlatformInfoDTO> platforms = new ArrayList<>();
        @JsonProperty(value = "env")
        private Map<String, String> env = new HashMap<>();

        /**
         * @return The name
         */
        @JsonProperty("name")
        public String getName() {
            return name;
        }

        /**
         * @param name The name
         */
        @JsonProperty("name")
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return The version
         */
        @JsonProperty("version")
        public String getVersion() {
            return version;
        }

        /**
         * @param version The version
         */
        @JsonProperty("version")
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * @return The platforms
         */
        @JsonProperty("platforms")
        public List<PlatformInfoDTO> getPlatforms() {
            return platforms;
        }

        /**
         * @param platforms The platforms
         */
        @JsonProperty("platforms")
        public void setPlatforms(List<PlatformInfoDTO> platforms) {
            this.platforms = platforms;
        }

        @JsonProperty("env")
        public Map<String, String> getEnv() {
            return env;
        }

        @JsonProperty("env")
        public void setEnv(Map<String, String> env) {
            this.env = env;
        }

        @Override
        public String toString() {
            return "EngineInfoDTO{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", platforms=" + platforms +
                ", env=" + env +
                '}';
        }
    }


    public static class FlavorInfoDTO {

        @JsonProperty("name")
        private String name;
        @JsonProperty("env")
        private Map<String, String> env = new HashMap<>();

        /**
         * @return The name
         */
        @JsonProperty("name")
        public String getName() {
            return name;
        }

        /**
         * @param name The name
         */
        @JsonProperty("name")
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return The env
         */
        @JsonProperty("env")
        public Map<String, String> getEnv() {
            return env;
        }

        /**
         * @param env The env
         */
        @JsonProperty("env")
        public void setEnv(Map<String, String> env) {
            this.env = env;
        }

        @Override
        public String toString() {
            return "FlavorInfoDTO{" +
                "name='" + name + '\'' +
                ", env=" + env +
                '}';
        }
    }

    public static class PlatformInfoDTO {

        @JsonProperty("name")
        private String name;
        @JsonProperty("version")
        private String version;
        @JsonProperty("env")
        private Map<String, String> env = new HashMap<>();
        @JsonProperty("flavors")
        private List<FlavorInfoDTO> flavors = new ArrayList<>();

        /**
         * @return The name
         */
        @JsonProperty("name")
        public String getName() {
            return name;
        }

        /**
         * @param name The name
         */
        @JsonProperty("name")
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return The version
         */
        @JsonProperty("version")
        public String getVersion() {
            return version;
        }

        /**
         * @param version The version
         */
        @JsonProperty("version")
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * @return The env
         */
        @JsonProperty("env")
        public Map<String, String> getEnv() {
            return env;
        }

        /**
         * @param env The env
         */
        @JsonProperty("env")
        public void setEnv(Map<String, String> env) {
            this.env = env;
        }

        /**
         * @return The flavors
         */
        @JsonProperty("flavors")
        public List<FlavorInfoDTO> getFlavors() {
            return flavors;
        }

        /**
         * @param flavors The flavors
         */
        @JsonProperty("flavors")
        public void setFlavors(List<FlavorInfoDTO> flavors) {
            this.flavors = flavors;
        }

        @Override
        public String toString() {
            return "PlatformInfoDTO{" +
                "name='" + name + '\'' +
                ", version=" + version +
                ", env=" + env +
                ", flavors=" + flavors +
                '}';
        }
    }


}
