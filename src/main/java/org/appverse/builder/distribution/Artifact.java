package org.appverse.builder.distribution;

import org.appverse.builder.domain.enumeration.DistributionChannelType;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;

import java.net.URI;

/**
 * Created by panthro on 13/01/16.
 */
public class Artifact {

    public static final String FILE = "file";

    private URI uri;

    private String name;

    private Long distributionChannelId;

    private String distributionChannelName;

    private DistributionChannelType distributionChannelType;

    private Long sizeInBytes;

    public Artifact() {
    }

    public Artifact(URI uri, String name, Long sizeInBytes) {
        this.uri = uri;
        this.name = name;
        this.sizeInBytes = sizeInBytes;
    }

    public Artifact(URI uri, String name, DistributionChannelDTO distributionChannelDTO, Long sizeInBytes) {
        this.uri = uri;
        this.name = name;
        this.sizeInBytes = sizeInBytes;
        this.distributionChannelId = distributionChannelDTO.getId();
        this.distributionChannelName = distributionChannelDTO.getName();
        this.distributionChannelType = distributionChannelDTO.getType();
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocal() {
        return uri != null && uri.getScheme().equals(FILE);
    }

    public Long getDistributionChannelId() {
        return distributionChannelId;
    }

    public void setDistributionChannelId(Long distributionChannelId) {
        this.distributionChannelId = distributionChannelId;
    }

    public String getDistributionChannelName() {
        return distributionChannelName;
    }

    public void setDistributionChannelName(String distributionChannelName) {
        this.distributionChannelName = distributionChannelName;
    }

    public DistributionChannelType getDistributionChannelType() {
        return distributionChannelType;
    }

    public void setDistributionChannelType(DistributionChannelType distributionChannelType) {
        this.distributionChannelType = distributionChannelType;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artifact)) return false;

        Artifact artifact = (Artifact) o;

        if (uri != null ? !uri.equals(artifact.uri) : artifact.uri != null) return false;
        if (name != null ? !name.equals(artifact.name) : artifact.name != null) return false;
        if (distributionChannelId != null ? !distributionChannelId.equals(artifact.distributionChannelId) : artifact.distributionChannelId != null)
            return false;
        if (distributionChannelName != null ? !distributionChannelName.equals(artifact.distributionChannelName) : artifact.distributionChannelName != null)
            return false;
        if (distributionChannelType != artifact.distributionChannelType) return false;
        return sizeInBytes != null ? sizeInBytes.equals(artifact.sizeInBytes) : artifact.sizeInBytes == null;

    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (distributionChannelId != null ? distributionChannelId.hashCode() : 0);
        result = 31 * result + (distributionChannelName != null ? distributionChannelName.hashCode() : 0);
        result = 31 * result + (distributionChannelType != null ? distributionChannelType.hashCode() : 0);
        result = 31 * result + (sizeInBytes != null ? sizeInBytes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Artifact{" +
            "name='" + name + '\'' +
            ", uri=" + uri +
            ", distributionChannelId=" + distributionChannelId +
            ", sizeInBytes=" + sizeInBytes +
            '}';
    }
}
