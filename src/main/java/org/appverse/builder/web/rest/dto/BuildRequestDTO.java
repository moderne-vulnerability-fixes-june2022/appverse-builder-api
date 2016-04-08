package org.appverse.builder.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.appverse.builder.domain.enumeration.BuildStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.appverse.builder.domain.enumeration.BuildStatus.*;

/**
 * A DTO for the BuildRequest entity.
 */
public class BuildRequestDTO implements Serializable {

    private Long id;

    @NotNull
    private String engine;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildRequestDTO)) return false;

        BuildRequestDTO that = (BuildRequestDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (engine != null ? !engine.equals(that.engine) : that.engine != null) return false;
        if (platform != null ? !platform.equals(that.platform) : that.platform != null) return false;
        return flavor != null ? flavor.equals(that.flavor) : that.flavor == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (engine != null ? engine.hashCode() : 0);
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (flavor != null ? flavor.hashCode() : 0);
        return result;
    }

    @NotNull

    private String platform;

    @NotNull
    private String flavor;

    @NotNull
    private BuildStatus status;

    private ZonedDateTime startTime;

    private ZonedDateTime endTime;

    @Size(max = 250)
    private String message;

    private Long chainId;

    private String requesterLogin;

    private ZonedDateTime createdDate = ZonedDateTime.now();

    @JsonIgnore
    private EnginePlatformDTO mappedEnginePlatform;
    @JsonIgnore
    private Map<String, String> variables = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public BuildStatus getStatus() {
        return status;
    }

    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long buildChainId) {
        this.chainId = buildChainId;
    }

    public String getRequesterLogin() {
        return requesterLogin;
    }

    public void setRequesterLogin(String requesterLogin) {
        this.requesterLogin = requesterLogin;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public EnginePlatformDTO getMappedEnginePlatform() {
        return mappedEnginePlatform;
    }

    public void setMappedEnginePlatform(EnginePlatformDTO mappedEnginePlatform) {
        this.mappedEnginePlatform = mappedEnginePlatform;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public void start() {
        startTime = ZonedDateTime.now();
        status = RUNNING;
    }

    public void finish(BuildStatus status, String message) {
        endTime = ZonedDateTime.now();
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return "BuildRequestDTO{" +
            "id=" + id +
            ", engine='" + engine + "'" +
            ", platform='" + platform + "'" +
            ", flavor='" + flavor + "'" +
            ", status='" + status + "'" +
            ", startTime='" + startTime + "'" +
            ", endTime='" + endTime + "'" +
            ", message='" + message + "'" +
            '}';
    }


    public boolean isFinished() {
        return CANCELLED.equals(status) || FAILED.equals(status) || SUCCESSFUL.equals(status);
    }
}
