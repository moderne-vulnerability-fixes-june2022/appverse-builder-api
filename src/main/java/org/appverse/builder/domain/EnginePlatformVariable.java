package org.appverse.builder.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A EnginePlatformVariable.
 */
@Entity
@Table(name = "engine_platform_variable")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EnginePlatformVariable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^(\\w|-)+$")
    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "required", nullable = false)
    private Boolean required;

    @Column(name = "default_value")
    private String defaultValue;

    @ManyToOne
    @JoinColumn(name = "engine_platform_id")
    @NotNull
    private EnginePlatform enginePlatform;

    public EnginePlatformVariable(String name, String defaultValue, Boolean required, EnginePlatform enginePlatform) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.required = required;
        this.enginePlatform = enginePlatform;
    }

    public EnginePlatformVariable() {
    }

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

    public EnginePlatform getEnginePlatform() {
        return enginePlatform;
    }

    public void setEnginePlatform(EnginePlatform enginePlatform) {
        this.enginePlatform = enginePlatform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnginePlatformVariable)) return false;

        EnginePlatformVariable that = (EnginePlatformVariable) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EnginePlatformVariable{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", description='" + description + "'" +
            ", required='" + required + "'" +
            ", defaultValue='" + defaultValue + "'" +
            '}';
    }
}
