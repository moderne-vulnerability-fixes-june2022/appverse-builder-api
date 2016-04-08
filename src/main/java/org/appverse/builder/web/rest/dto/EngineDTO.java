package org.appverse.builder.web.rest.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;


/**
 * A DTO for the Engine entity.
 */
public class EngineDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 3, max = 30)
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EngineDTO)) return false;

        EngineDTO engineDTO = (EngineDTO) o;

        if (id != null ? !id.equals(engineDTO.id) : engineDTO.id != null) return false;
        if (name != null ? !name.equals(engineDTO.name) : engineDTO.name != null) return false;
        return version != null ? version.equals(engineDTO.version) : engineDTO.version == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    private String description;

    @NotNull
    private String version;

    @NotNull
    private Boolean enabled;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "EngineDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", description='" + description + "'" +
            ", version='" + version + "'" +
            ", enabled='" + enabled + "'" +
            '}';
    }
}
