# Properties that can be set to start Appverse Builder
This document describes the properties available when starting the Appverse Builder-API.
All properties are spring boot properties, so they should be used in the same way.

[All default spring properties can be found here][1]


To launch the built jar with a property use the following

`java -D<property.name>=<property.value> apb.war`

During the build a `.original` file will also be created, this a a `war` file without a embedded _tomcat_ to be dropped in any  __JEE__ application container   

Example:

`java --spring.profiles.active=prod -Dspring.mail.host=smtp.example.com -jar apb.war`


## Environment
There are 2 specific environments to launch the application, they disable/enable some features.

1. __PROD__ `--spring.profiles.active=prod`
    This is the profile that should be used when deploying the application, it enables more caching, js css and html minification, etc.
    
2. __DEV__ `--spring.profiles.active=dev` (this is the default if not specified)
    This is a development profile and should be used only when developing locally.
 
## Database

Here are some samples of how to launch the application for distinct databases:

The application uses liquibase to generate the tables.

1. __MySQL__
    
    ```
    spring.datasource.url='jdbc:mysql://<hostname>:<port>/<database>?useUnicode=true&characterEncoding=utf8'
    spring.datasource.username=<mysql_user>
    spring.datasource.password=<mysql_password>
    ```
    
2. __SQL Server__

    ```
    spring.datasource.url='jdbc:jtds:sqlserver://<hostname>:<port>/<database>'
    spring.datasource.username=<SQLserver_username>
    spring.datasource.password=<SQLserver_password>
    spring.jpa.database-platform='org.hibernate.dialect.SQLServerDialect'
    spring.jpa.database='SQL_SERVER'
    ```
    
3. __Hypersonic__ Persistent `hsqldb` (for testing purposes)

    ```
    spring.datasource.url='jdbc:h2:file:<path_to_.db_file_>;DB_CLOSE_DELAY=-1'
    spring.jpa.database-platform='org.appverse.builder.domain.util.FixedH2Dialect'
    spring.jpa.database='H2'
    ```
    
## Mail Server

Mail server uses the common Spring boot properties, that can be found in [Spring boot documentation][1], following there is an example:

```
apb.mail.from=noreply@example.com
spring.mail.host=smtp.example.com
```

   
## LDAP
These are all LDAP properties, the properties you need to set depends on the LDAP server that the code will connect.
More info can be found on the [Spring Security LDAP implementation][2]


* __enabled__ = `true` or `false` to enable/disable 
* __url__ = the ldap url eg `ldaps://ldap.example.com:<port>/DC=example,DC=com`
* __base__ = the base to search (you can also put this on the url as the example above)
* __user-dn__ = the LDAP user that will be used to authenticate to LDAP in the format `DC=example,DC=com,....`
* __password__ = is the password for the given _user-dn_
* __group-search-base__ = The base to search for users ex `DC=example,DC=com`
* __group-role-attribute__ = If you want to map user roles from LDAP to the application
* __user-search-filter__ = A filter to search for users in the LDAP format, this should contain a `{0}` that will be replaced with the username eg: `CN={0}`
* __anonymous-read-only__ = if this ldap is anonymous read only (refer to the [official documentation][2])
* __user-dn-pattern__ = fill is if you want to use user dn patterns (refer to the [official documentation][2])
* __email-attribute__ (default "mail") = the ldap attribute go get the user e-mail 
* __first-name-attribute__ (default "givenName") = the ldap attribute to get the first name 
* __last-name-attribute__ (default "sn") = the ldap attribute to get the last name
* __full-name-attribute__ (default "cn") = the ldap attribute to get the full name (if only this is specified the application will try to guess the first and last name)

Example:

```
apb.auth.ldap.enabled=true'
apb.auth.ldap.url='ldaps://ldap.example.com:<port>/DC=example,DC=com'
apb.auth.ldap.user-dn='CN=myuser,OU=mygroup,OU=othergroup,DC=example,DC=com'
apb.auth.ldap.password=MyH@ardt0Gu3$$pWD'
apb.auth.ldap.user-search-filter='CN={0}'
```


## Client customization
In case the `apb-cli` have been built with a different setup than the original, you can specify the customization here

* __apb.cli.registry__ = the npm registry the client is published
* __apb.cli.packageName__ (default "apb-cli") = the package name name
* __apb.cli.commandName__ (default "apb") = the command name

## Download Token and Registration customization parameters
* __apb.auth.download-token-secret__ = the token to be used when generating download links (a default secure one is provided)
* __apb.auth.download-expire-after-seconds (default __172800__ _48h_)_ = the amount, in seconds, that a download token should expire after it's creation 
* __apb.auth.registrationEnabled__ = `true` or `false` to enable/disable user registration.

## Build customization
These are only necessary if any any customization is needed

>___NOTE:___ if any of this customizations is changed after the application have successfully execute builds, artifacts and logs might stop working

* __apb.build.build-root-folder-name__ (default __"apb"__) = The build root folder name, there Appverse Builder will work
* __apb.build.build-root__ (default __"java.io.tmpdir"__) = The root where apb should work
* __apb.build.unknown-engine__ (default __"unknown"__) = Which name to use when no __engine__ is found
* __apb.build.unknown-platform__ (default __"unknown"__) = Which name to use when no __platform__ is found
* __apb.build.unknown-flavor__ (default __"unknown"__) = Which name to use when no __flavor__ is found
* __apb.build.build-info-file-name__ (default __".apb"__) = What is the name of configuration file that is expected in the input payload, extensions that can be used: `yml` or `json`
* __apb.build.compressed-payload-name__ (default __"input.zip"__) = When saving the payload, which name to use
* __apb.build.input-dir-name__ (default __"input"__) = When decompressing the payload, the name of the folder that will be used
* __apb.build.artifact-compressed-name__ (default __"artifacts"__) = when compressing artifacts, the name of the file to use
* __apb.build.artifact-compressed-extension__ (default __".zip"__) = when compressing artifacts, the extension of the file to use
* __apb.build.log-file-name__ (default __"build.log"__) = The default file log name
* __apb.build.artifacts-dir-name__ (default __"artifacts"__) = when storing build artifacts, the name of the folder that sould be used


## Build Agent default settings and customizations
all properties starting with _default_ can be overwritten using [__Build Agent Properties__][3]  

> ___NOTE:___ More information about how build works can be found on the [Build Flow][4] document

* __apb.build.agent.defaultQueueSize__ (default __10__) = queue size for any agent 
* __apb.build.agent.defaultConcurrentBuilds__ (default __2__) = Concurrent builds an agent can handle
* __apb.build.agent.defaultTerminationTimeout__ (default __120__ (seconds)) = The amount of seconds apb should wait when trying to shutdown an agent
* __apb.build.agent.defaultSshPort__ (default __22__) = the default SSH port when connecting to remote agents
* __apb.build.agent.defaultAgentUser__ (default __"apb"__) = the default user when connecting to remote agents
* __apb.build.agent.defaultRemoteRoot__ (default __"/build"__) = the default path on agents to be used when storing build files
* __apb.build.agent.defaultFileSeparator__ (default __"/"__) = the default file separator for the agent
* __apb.build.agent.defaultAgentRefreshTimeInSeconds__ (default __60__) = the refresh time in seconds that the application will check if any build agent information has been updated
* __apb.build.agent.maxIdleTimeInSeconds__ (default __5 * 60__) = the max amount of time in seconds an agent can be idle without being recycled


## Docker Build customization

> ___NOTE:___ This are very specific configurations, if you don't know docker leave them as they are

* __apb.build.command.docker.binary__ (default __"docker"__) = the docker binary name
* __apb.build.command.docker.task__ (default __"run"__) = the docker task name
* __apb.build.command.docker.work-dir__ (default __"/data/input"__) = the default docker work dir (`--work-dir`)

## Dockgrant customization


> ___NOTE:___ This are very specific configurations, if you don't know dockgrant or vagrant leave them as they are
> ___NOTE:___ More information about dockgrant can be found on [dockgrant github repository][5]

* __apb.build.command.docker.binary__ (default __"docker"__) = the docker binary name
* __apb.build.command.docker.task__ (default __"run"__) = the docker task name
* __apb.build.command.docker.work-dir__ (default __"/data/input"__) = the default docker work dir (`--work-dir`)


## More advanced customizations

More advanced customizations can be used, and they can be found on the original [ApbProperties][6]

[1]: https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html
[2]: https://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html
[3]: build-agent-properties.md
[4]: build-flow.md
[5]: https://github.com/ferranvila/dockgrant
[6]: ./src/main/java/com/gft/apb/config/ApbProperties.java
