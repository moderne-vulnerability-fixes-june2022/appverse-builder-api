package org.appverse.builder.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;

import static org.appverse.builder.domain.enumeration.BuildStatus.*;

/**
 * A BuildRequest.
 */
@Entity
@Table(name = "build_request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BuildRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "engine", nullable = false)
    private String engine;

    @NotNull
    @Column(name = "platform", nullable = false)
    private String platform;

    @NotNull
    @Column(name = "flavor", nullable = false)
    private String flavor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BuildStatus status = QUEUED;

    @Column(name = "start_time")
    private ZonedDateTime startTime;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    @Size(max = 250)
    @Column(name = "message", length = 250)
    private String message;

    @ManyToOne
    @JoinColumn(name = "chain_id")
    @NotNull
    private BuildChain chain;

    @CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false)
    @JsonIgnore
    private ZonedDateTime createdDate = ZonedDateTime.now();

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

    public BuildChain getChain() {
        return chain;
    }

    public void setChain(BuildChain buildChain) {
        this.chain = buildChain;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
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

    public boolean isFinished() {
        return CANCELLED.equals(status) || FAILED.equals(status) || SUCCESSFUL.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildRequest)) return false;

        BuildRequest that = (BuildRequest) o;

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

    @Override
    public String toString() {
        return "BuildRequest{" +
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

}
