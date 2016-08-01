package org.appverse.builder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * Properties specific to Appverse Builder.
 * <p>
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "apb", ignoreUnknownFields = false)
@Component
public class AppverseBuilderProperties {

    private Build build = new Build();

    private Agent agent = new Agent();
    private Auth auth = new Auth();

    private String baseUrl = "http://localhost:8080";
    private Cli cli = new Cli();

    private final Async async = new Async();

    private final Http http = new Http();

    private final Datasource datasource = new Datasource();

    private final Cache cache = new Cache();

    private final Mail mail = new Mail();

    private final Security security = new Security();

    private final Swagger swagger = new Swagger();

    private final Metrics metrics = new Metrics();

    private final CorsConfiguration cors = new CorsConfiguration();


    public Async getAsync() {
        return async;
    }

    public Http getHttp() {
        return http;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public Cache getCache() {
        return cache;
    }

    public Mail getMail() {
        return mail;
    }

    public Security getSecurity() {
        return security;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public CorsConfiguration getCors() {
        return cors;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Cli getCli() {
        return cli;
    }

    public void setCli(Cli cli) {
        this.cli = cli;
    }


    public static class Cli {

        private String registry;
        private String packageName = "apb-cli";
        private String commandName = "apb";

        public String getRegistry() {
            return registry;
        }

        public void setRegistry(String registry) {
            this.registry = registry;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }
    }

    public static class Auth {


        private Ldap Ldap = new Ldap();
        private boolean registrationEnabled = false;
        private String downloadTokenSecret = "P4kjB}+`XZj72NY#YaRq#Vf5*fY5H9?aER7ty2},)U]CQW--A+[4J$pR}f,^-M,9!5RK7tJsY8Cw";
        /**
         * 48h by default
         */
        private Integer downloadExpireAfterSeconds = 172800;
        private String nonExpireTokenClientId = "CIClient";
        private String nonExpireTokenScope = "ci";

        public Ldap getLdap() {
            return Ldap;
        }

        public void setLdap(Ldap ldap) {
            this.Ldap = ldap;
        }

        public boolean isRegistrationEnabled() {
            return registrationEnabled;
        }

        public void setRegistrationEnabled(boolean registrationEnabled) {
            this.registrationEnabled = registrationEnabled;
        }

        public String getDownloadTokenSecret() {
            return downloadTokenSecret;
        }

        public void setDownloadTokenSecret(String downloadTokenSecret) {
            this.downloadTokenSecret = downloadTokenSecret;
        }

        public Integer getDownloadExpireAfterSeconds() {
            return downloadExpireAfterSeconds;
        }

        public void setDownloadExpireAfterSeconds(Integer downloadExpireAfterSeconds) {
            this.downloadExpireAfterSeconds = downloadExpireAfterSeconds;
        }

        public String getNonExpireTokenClientId() {
            return nonExpireTokenClientId;
        }

        public void setNonExpireTokenClientId(String nonExpireTokenClientId) {
            this.nonExpireTokenClientId = nonExpireTokenClientId;
        }

        public String getNonExpireTokenScope() {
            return nonExpireTokenScope;
        }

        public void setNonExpireTokenScope(String nonExpireTokenScope) {
            this.nonExpireTokenScope = nonExpireTokenScope;
        }

        public static class Ldap {

            private boolean enabled = false;
            private String groupSearchBase;
            private String groupRoleAttribute;
            private String groupSearchFilter;
            private String userSearchBase;
            private String userSearchFilter;
            private String url;
            private String base;
            private String userDn;
            private String password;
            private boolean anonymousReadOnly;
            private String userDnPattern;
            private String emailAttribute = "mail";
            private String firstNameAttribute = "givenName";
            private String lastNameAttribute = "sn";
            private String fullNameAttribute = "cn";
            private String defaultEmailDomain = "appverse.org";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getGroupSearchBase() {
                return groupSearchBase;
            }

            public void setGroupSearchBase(String groupSearchBase) {
                this.groupSearchBase = groupSearchBase;
            }

            public String getGroupRoleAttribute() {
                return groupRoleAttribute;
            }

            public void setGroupRoleAttribute(String groupRoleAttribute) {
                this.groupRoleAttribute = groupRoleAttribute;
            }


            public String getGroupSearchFilter() {
                return groupSearchFilter;
            }

            public void setGroupSearchFilter(String groupSearchFilter) {
                this.groupSearchFilter = groupSearchFilter;
            }

            public String getUserSearchBase() {
                return userSearchBase;
            }

            public void setUserSearchBase(String userSearchBase) {
                this.userSearchBase = userSearchBase;
            }

            public String getUserSearchFilter() {
                return userSearchFilter;
            }

            public void setUserSearchFilter(String userSearchFilter) {
                this.userSearchFilter = userSearchFilter;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getBase() {
                return base;
            }

            public void setBase(String base) {
                this.base = base;
            }

            public String getUserDn() {
                return userDn;
            }

            public void setUserDn(String userDn) {
                this.userDn = userDn;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public boolean isAnonymousReadOnly() {
                return anonymousReadOnly;
            }

            public void setAnonymousReadOnly(boolean anonymousReadOnly) {
                this.anonymousReadOnly = anonymousReadOnly;
            }

            public String getUserDnPattern() {
                return userDnPattern;
            }

            public void setUserDnPattern(String userDnPattern) {
                this.userDnPattern = userDnPattern;
            }

            public String getEmailAttribute() {
                return emailAttribute;
            }

            public void setEmailAttribute(String emailAttribute) {
                this.emailAttribute = emailAttribute;
            }

            public String getFirstNameAttribute() {
                return firstNameAttribute;
            }

            public void setFirstNameAttribute(String firstNameAttribute) {
                this.firstNameAttribute = firstNameAttribute;
            }

            public String getLastNameAttribute() {
                return lastNameAttribute;
            }

            public void setLastNameAttribute(String lastNameAttribute) {
                this.lastNameAttribute = lastNameAttribute;
            }

            public String getFullNameAttribute() {
                return fullNameAttribute;
            }

            public void setFullNameAttribute(String fullNameAttribute) {
                this.fullNameAttribute = fullNameAttribute;
            }

            public String getDefaultEmailDomain() {
                return defaultEmailDomain;
            }

            public void setDefaultEmailDomain(String defaultEmailDomain) {
                this.defaultEmailDomain = defaultEmailDomain;
            }
        }
    }

    public static class Build {

        private Command command = new Command();

        private Agent agent = new Agent();

        private String buildRootFolderName = "apb";

        private File buildRoot = new File(System.getProperty("java.io.tmpdir"));

        private String unknownEngine = "unknown";

        private String unknownPlatform = "unknown";

        private String unknownFlavor = "unknown";

        private String buildInfoFileName = ".apb.yml";

        private String compressedPayloadName = "input.zip";
        private String inputDirName = "input";

        private String artifactCompressedName = "artifacts";

        private String artifactCompressedExtension = ".zip";
        private String logFileName = "build.log";
        private String artifactsDirName = "artifacts";
        private String buildIgnoreFileName = ".apbignore";

        public String getBuildRootFolderName() {
            return buildRootFolderName;
        }

        public void setBuildRootFolderName(String buildRootFolderName) {
            this.buildRootFolderName = buildRootFolderName;
        }

        public File getBuildRoot() {
            return new File(buildRoot, buildRootFolderName);
        }

        public void setBuildRoot(File buildRoot) {
            this.buildRoot = buildRoot;
        }

        public String getUnknownEngine() {
            return unknownEngine;
        }

        public void setUnknownEngine(String unknownEngine) {
            this.unknownEngine = unknownEngine;
        }

        public String getUnknownPlatform() {
            return unknownPlatform;
        }

        public void setUnknownPlatform(String unknownPlatform) {
            this.unknownPlatform = unknownPlatform;
        }

        public String getUnknownFlavor() {
            return unknownFlavor;
        }

        public void setUnknownFlavor(String unknownFlavor) {
            this.unknownFlavor = unknownFlavor;
        }

        public String getBuildInfoFileName() {
            return buildInfoFileName;
        }

        public void setBuildInfoFileName(String buildInfoFileName) {
            this.buildInfoFileName = buildInfoFileName;
        }

        public String getCompressedPayloadName() {
            return compressedPayloadName;
        }

        public void setCompressedPayloadName(String compressedPayloadName) {
            this.compressedPayloadName = compressedPayloadName;
        }

        public String getInputDirName() {
            return inputDirName;
        }

        public void setInputDirName(String inputDirName) {
            this.inputDirName = inputDirName;
        }

        public String getArtifactCompressedName() {
            return artifactCompressedName;
        }

        public void setArtifactCompressedName(String artifactCompressedName) {
            this.artifactCompressedName = artifactCompressedName;
        }

        public String getArtifactCompressedExtension() {
            return artifactCompressedExtension;
        }

        public void setArtifactCompressedExtension(String artifactCompressedExtension) {
            this.artifactCompressedExtension = artifactCompressedExtension;
        }

        public Agent getAgent() {
            return agent;
        }

        public void setAgent(Agent agent) {
            this.agent = agent;
        }

        public Command getCommand() {
            return command;
        }

        public void setCommand(Command command) {
            this.command = command;
        }

        public String getLogFileName() {
            return logFileName;
        }

        public void setLogFileName(String logFileName) {
            this.logFileName = logFileName;
        }

        public String getArtifactsDirName() {
            return artifactsDirName;
        }

        public void setArtifactsDirName(String artifactsDirName) {
            this.artifactsDirName = artifactsDirName;
        }

        public String getBuildIgnoreFileName() {
            return buildIgnoreFileName;
        }

        public void setBuildIgnoreFileName(String buildIgnoreFileName) {
            this.buildIgnoreFileName = buildIgnoreFileName;
        }
    }

    public static class Agent {
        private int defaultQueueSize = 10;
        private int defaultConcurrentBuilds = 2;
        private int defaultTerminationTimeout = 120; //in seconds
        private int defaultSshPort = 22;
        private String defaultAgentUser = "apb";
        private String defaultRemoteRoot = "/build";
        private String defaultFileSeparator = "/";
        private long defaultAgentRefreshTimeInSeconds = 60;
        private long maxIdleTimeInSeconds = 5 * 60;

        public int getDefaultQueueSize() {
            return defaultQueueSize;
        }

        public void setDefaultQueueSize(int defaultQueueSize) {
            this.defaultQueueSize = defaultQueueSize;
        }

        public int getDefaultTerminationTimeout() {
            return defaultTerminationTimeout;
        }

        public int getDefaultConcurrentBuilds() {
            return defaultConcurrentBuilds;
        }

        public void setDefaultTerminationTimeout(int defaultTerminationTimeout) {
            this.defaultTerminationTimeout = defaultTerminationTimeout;
        }

        public void setDefaultConcurrentBuilds(int defaultConcurrentBuilds) {
            this.defaultConcurrentBuilds = defaultConcurrentBuilds;
        }


        public int getDefaultSshPort() {
            return defaultSshPort;
        }

        public String getDefaultAgentUser() {
            return defaultAgentUser;
        }

        public String getDefaultRemoteRoot() {
            return defaultRemoteRoot;
        }

        public void setDefaultSshPort(int defaultSshPort) {
            this.defaultSshPort = defaultSshPort;
        }

        public void setDefaultAgentUser(String defaultAgentUser) {
            this.defaultAgentUser = defaultAgentUser;
        }

        public void setDefaultRemoteRoot(String defaultRemoteRoot) {
            this.defaultRemoteRoot = defaultRemoteRoot;
        }

        public String getDefaultFileSeparator() {
            return defaultFileSeparator;
        }

        public void setDefaultFileSeparator(String defaultFileSeparator) {
            this.defaultFileSeparator = defaultFileSeparator;
        }

        public long getDefaultAgentRefreshTimeInSeconds() {
            return defaultAgentRefreshTimeInSeconds;
        }

        public void setDefaultAgentRefreshTimeInSeconds(long defaultAgentRefreshTimeInSeconds) {
            this.defaultAgentRefreshTimeInSeconds = defaultAgentRefreshTimeInSeconds;
        }

        public long getMaxIdleTimeInSeconds() {
            return maxIdleTimeInSeconds;
        }

        public void setMaxIdleTimeInSeconds(long maxIdleTimeInSeconds) {
            this.maxIdleTimeInSeconds = maxIdleTimeInSeconds;
        }
    }

    public static class Command {
        private Docker docker = new Docker();
        private Dockgrant dockgrant = new Dockgrant();

        public Docker getDocker() {
            return docker;
        }

        public void setDocker(Docker docker) {
            this.docker = docker;
        }

        public Dockgrant getDockgrant() {
            return dockgrant;
        }

        public void setDockgrant(Dockgrant dockgrant) {
            this.dockgrant = dockgrant;
        }
    }

    public static class Docker {
        private String binary = "docker";
        private String task = "run";
        private String workDir = "/data/input";


        public String getBinary() {
            return binary;
        }

        public void setBinary(String binary) {
            this.binary = binary;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public String getWorkDir() {
            return workDir;
        }

        public void setWorkDir(String workDir) {
            this.workDir = workDir;
        }
    }

    public static class Dockgrant {
        private String binary = "dockgrant";
        private String task = "run";
        private String workDir = "/data/input";


        public String getBinary() {
            return binary;
        }

        public void setBinary(String binary) {
            this.binary = binary;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public String getWorkDir() {
            return workDir;
        }

        public void setWorkDir(String workDir) {
            this.workDir = workDir;
        }
    }


    public static class Async {

        private int corePoolSize = 2;

        private int maxPoolSize = 50;

        private int queueCapacity = 10000;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    public static class Http {

        private final Cache cache = new Cache();

        public Cache getCache() {
            return cache;
        }

        public static class Cache {

            private int timeToLiveInDays = 31;

            public int getTimeToLiveInDays() {
                return timeToLiveInDays;
            }

            public void setTimeToLiveInDays(int timeToLiveInDays) {
                this.timeToLiveInDays = timeToLiveInDays;
            }
        }
    }

    public static class Datasource {

        private boolean cachePrepStmts = true;

        private int prepStmtCacheSize = 250;

        private int prepStmtCacheSqlLimit = 2048;

        private boolean useServerPrepStmts = true;

        public boolean isCachePrepStmts() {
            return cachePrepStmts;
        }

        public void setCachePrepStmts(boolean cachePrepStmts) {
            this.cachePrepStmts = cachePrepStmts;
        }

        public int getPrepStmtCacheSize() {
            return prepStmtCacheSize;
        }

        public void setPrepStmtCacheSize(int prepStmtCacheSize) {
            this.prepStmtCacheSize = prepStmtCacheSize;
        }

        public int getPrepStmtCacheSqlLimit() {
            return prepStmtCacheSqlLimit;
        }

        public void setPrepStmtCacheSqlLimit(int prepStmtCacheSqlLimit) {
            this.prepStmtCacheSqlLimit = prepStmtCacheSqlLimit;
        }

        public boolean isUseServerPrepStmts() {
            return useServerPrepStmts;
        }

        public void setUseServerPrepStmts(boolean useServerPrepStmts) {
            this.useServerPrepStmts = useServerPrepStmts;
        }
    }

    public static class Cache {

        private final Ehcache ehcache = new Ehcache();
        private int timeToLiveSeconds = 3600;

        public int getTimeToLiveSeconds() {
            return timeToLiveSeconds;
        }

        public void setTimeToLiveSeconds(int timeToLiveSeconds) {
            this.timeToLiveSeconds = timeToLiveSeconds;
        }

        public Ehcache getEhcache() {
            return ehcache;
        }

        public static class Ehcache {

            private String maxBytesLocalHeap = "16M";

            public String getMaxBytesLocalHeap() {
                return maxBytesLocalHeap;
            }

            public void setMaxBytesLocalHeap(String maxBytesLocalHeap) {
                this.maxBytesLocalHeap = maxBytesLocalHeap;
            }
        }
    }

    public static class Mail {

        private String from = "apb@localhost";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }

    public static class Security {

        private final Rememberme rememberme = new Rememberme();

        public Rememberme getRememberme() {
            return rememberme;
        }

        public static class Rememberme {

            @NotNull
            private String key;

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }
        }
    }

    public static class Swagger {

        private String title = "apb API";

        private String description = "apb API documentation";

        private String version = "0.0.1";

        private String termsOfServiceUrl;

        private String contact;

        private String license;

        private String licenseUrl;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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

        public String getTermsOfServiceUrl() {
            return termsOfServiceUrl;
        }

        public void setTermsOfServiceUrl(String termsOfServiceUrl) {
            this.termsOfServiceUrl = termsOfServiceUrl;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public String getLicenseUrl() {
            return licenseUrl;
        }

        public void setLicenseUrl(String licenseUrl) {
            this.licenseUrl = licenseUrl;
        }
    }

    public static class Metrics {

        private final Jmx jmx = new Jmx();

        private final Spark spark = new Spark();

        private final Graphite graphite = new Graphite();

        public Jmx getJmx() {
            return jmx;
        }

        public Spark getSpark() {
            return spark;
        }

        public Graphite getGraphite() {
            return graphite;
        }

        public static class Jmx {

            private boolean enabled = true;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        public static class Spark {

            private boolean enabled = false;

            private String host = "localhost";

            private int port = 9999;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }
        }

        public static class Graphite {

            private boolean enabled = false;

            private String host = "localhost";

            private int port = 2003;

            private String prefix = "apb";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getPrefix() {
                return prefix;
            }

            public void setPrefix(String prefix) {
                this.prefix = prefix;
            }
        }
    }
}
