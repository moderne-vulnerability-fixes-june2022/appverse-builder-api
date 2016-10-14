package org.appverse.builder.build.comand;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by panthro on 19/01/16.
 */
public abstract class BuildCommand {


    protected String buildScript;
    protected String binary;
    protected String workDir;
    protected boolean createScript = true;
    protected String scriptFileName;
    protected String beforeBuildScript;

    /**
     * Builds the command as String
     *
     * @return
     */
    public String asString() {
        StringBuilder builder = new StringBuilder();
        Stream.of(asArray()).forEach(arg -> {
            builder.append(StringUtils.containsWhitespace(arg) ? StringUtils.quote(arg) : arg);
            builder.append(' ');
        });
        return StringUtils.trimWhitespace(builder.toString());
    }

    /**
     * Builds the command as an array, with the command binary in the index 0 and then the args
     *
     * @return
     */
    public String[] asArray() {
        List<String> args = getArgs();
        return CollectionUtils.isEmpty(args) ? new String[]{binary} : ArrayUtils.add(getArgs().toArray(new String[args.size()]), 0, getBinary());
    }

    /**
     * @return
     */
    public abstract List<String> getArgs();

    public void setBinary(String binary) {
        this.binary = binary;
    }

    public String getBinary() {
        return binary;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getWorkDir() {
        return workDir;
    }

    @Override
    public String toString() {
        return asString();
    }


    public boolean isCreateScript() {
        return createScript;
    }

    public void setCreateScript(boolean createScript) {
        this.createScript = createScript;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public void setScriptFileName(String scriptFileName) {
        this.scriptFileName = scriptFileName;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public void setBeforeBuildScript(String beforeBuildScript) {
        this.beforeBuildScript = beforeBuildScript;
    }

    public String getBeforeBuildScript() {
        return beforeBuildScript;
    }
}
