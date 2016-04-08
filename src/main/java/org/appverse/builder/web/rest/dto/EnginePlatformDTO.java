package org.appverse.builder.web.rest.dto;

import org.appverse.builder.domain.enumeration.ImageType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A DTO for the EnginePlatform entity.
 */
public class EnginePlatformDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 3, max = 30)
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnginePlatformDTO)) return false;

        EnginePlatformDTO that = (EnginePlatformDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return version != null ? version.equals(that.version) : that.version == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @NotNull
    private String version;

    private String imageName;

    @NotNull
    private Boolean enabled;

    @NotNull
    private ImageType imageType = ImageType.DOCKER;

    private Long engineId;

    private String engineName;

    private String engineVersion;

    private Map<String, String> agentRequirements = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public Long getEngineId() {
        return engineId;
    }

    public void setEngineId(Long engineId) {
        this.engineId = engineId;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public Map<String, String> getAgentRequirements() {
        return agentRequirements;
    }

    public void setAgentRequirements(Map<String, String> agentRequirements) {
        this.agentRequirements = agentRequirements;
    }

    @Override
    public String toString() {
        return "EnginePlatformDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", version='" + version + "'" +
            ", imageName='" + imageName + "'" +
            ", enabled='" + enabled + "'" +
            ", imageType='" + imageType + "'" +
            '}';
    }
}
