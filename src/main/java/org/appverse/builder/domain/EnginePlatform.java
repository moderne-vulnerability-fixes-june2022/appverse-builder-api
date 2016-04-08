package org.appverse.builder.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.appverse.builder.domain.enumeration.ImageType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.persistence.FetchType.EAGER;

/**
 * A EnginePlatform.
 */
@Entity
@Table(name = "engine_platform")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EnginePlatform implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(min = 3, max = 30)
    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @NotNull
    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "image_name")
    private String imageName;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType = ImageType.DOCKER;

    @OneToMany(mappedBy = "enginePlatform", cascade = CascadeType.ALL)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<EnginePlatformVariable> enginePlatformVariables = new HashSet<>();

    @ElementCollection(fetch = EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "engine_platform_agent_requirement", joinColumns = @JoinColumn(name = "engine_platform_id"))
    private Map<String, String> agentRequirements = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "engine_id")
    @NotNull
    private Engine engine;

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

    public Set<EnginePlatformVariable> getEnginePlatformVariables() {
        return enginePlatformVariables;
    }

    public void setEnginePlatformVariables(Set<EnginePlatformVariable> enginePlatformVariables) {
        this.enginePlatformVariables = enginePlatformVariables;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public Map<String, String> getAgentRequirements() {
        return agentRequirements;
    }

    public void setAgentRequirements(Map<String, String> agentRequirements) {
        this.agentRequirements = agentRequirements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnginePlatform)) return false;

        EnginePlatform that = (EnginePlatform) o;

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

    @Override
    public String toString() {
        return "EnginePlatform{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", version='" + version + "'" +
            ", imageName='" + imageName + "'" +
            ", enabled='" + enabled + "'" +
            ", imageType='" + imageType + "'" +
            '}';
    }
}
