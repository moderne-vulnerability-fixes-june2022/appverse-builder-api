package org.appverse.builder.web.rest.dto;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A DTO for the BuildChain entity.
 */
public class BuildChainDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime createdDate;

    private Long requesterId;

    private String requesterLogin;

    private Set<BuildRequestDTO> requests;

    private Map<String, String> options = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long userId) {
        this.requesterId = userId;
    }

    public String getRequesterLogin() {
        return requesterLogin;
    }

    public void setRequesterLogin(String userLogin) {
        this.requesterLogin = userLogin;
    }

    public Set<BuildRequestDTO> getRequests() {
        return requests;
    }

    public void setRequests(Set<BuildRequestDTO> requests) {
        this.requests = requests;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildChainDTO)) return false;

        BuildChainDTO that = (BuildChainDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (requesterId != null ? !requesterId.equals(that.requesterId) : that.requesterId != null) return false;
        return requesterLogin != null ? requesterLogin.equals(that.requesterLogin) : that.requesterLogin == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (requesterId != null ? requesterId.hashCode() : 0);
        result = 31 * result + (requesterLogin != null ? requesterLogin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BuildChainDTO{" +
            "id=" + id +
            ", createdDate='" + createdDate + "'" +
            '}';
    }
}
