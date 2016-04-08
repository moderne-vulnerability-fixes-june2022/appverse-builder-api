package org.appverse.builder.web.rest.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;


/**
 * A DTO for the EnginePlatformVariable entity.
 */
public class EnginePlatformVariableDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^(\\w|-)+$")
    private String name;

    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnginePlatformVariableDTO)) return false;

        EnginePlatformVariableDTO that = (EnginePlatformVariableDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @NotNull
    private Boolean required;

    private String defaultValue;

    private Long enginePlatformId;

    private String enginePlatformName;

    private String enginePlatformVersion;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Long getEnginePlatformId() {
        return enginePlatformId;
    }

    public void setEnginePlatformId(Long enginePlatformId) {
        this.enginePlatformId = enginePlatformId;
    }

    public String getEnginePlatformName() {
        return enginePlatformName;
    }

    public void setEnginePlatformName(String enginePlatformName) {
        this.enginePlatformName = enginePlatformName;
    }

    public String getEnginePlatformVersion() {
        return enginePlatformVersion;
    }

    public void setEnginePlatformVersion(String enginePlatformVersion) {
        this.enginePlatformVersion = enginePlatformVersion;
    }

    @Override
    public String toString() {
        return "EnginePlatformVariableDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", description='" + description + "'" +
            ", required='" + required + "'" +
            ", defaultValue='" + defaultValue + "'" +
            '}';
    }
}
