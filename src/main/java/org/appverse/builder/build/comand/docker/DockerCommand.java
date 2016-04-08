package org.appverse.builder.build.comand.docker;

import org.appverse.builder.build.comand.BuildCommand;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panthro on 19/01/16.
 */
public class DockerCommand extends BuildCommand {
    private String task;
    private String imageName;
    private String requestInputDir;
    private boolean removeContainer = true;
    private Map<String, String> environmentVariables = new HashMap<>();

    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    /**
     * This builds a list thhat represents the following command
     * <p>
     * structure:  {task} -v {requestInputDir}:{workDir} -w {workDir} --rm=true {imageName} '{script}'
     * <p>
     * example:  run -v /data/work/request/5:/data/input -w /data/input --rm=true android:23 'gradle assemble'
     * <p>
     * NOTICE: it should not add the binary itself
     **/
    @Override
    public List<String> getArgs() {
        List<String> args = new ArrayList<>();
        args.add(getTask());
        args.add("--volume");
        args.add(getRequestInputDir() + ":" + getWorkDir());
        args.add("--workdir");
        args.add(getWorkDir());
        if (isRemoveContainer()) {
            args.add("--rm");
        }
        environmentVariables.forEach((key, value) -> {
            if (!StringUtils.isEmpty(value) && !value.equals(buildScript)) {
                args.add("-e");
                args.add(key + "=" + value);
            }
        });
        args.add(getImageName());
        if (getBuildScript() != null) {
            args.add("sh");
            args.add("-c");
            if (!isCreateScript()) {
                args.add(getBuildScript());
            } else {
                args.add("./" + getScriptFileName()); //TODO this is so ugly that it hurts, using the dirty and fast approach ¯\_(ツ)_/¯
            }

        }
        return args;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setRequestInputDir(String requestInputDir) {
        this.requestInputDir = requestInputDir;
    }

    public String getRequestInputDir() {
        return requestInputDir;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public boolean isRemoveContainer() {
        return removeContainer;
    }

    public void setRemoveContainer(boolean removeContainer) {
        this.removeContainer = removeContainer;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }


    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }
}
