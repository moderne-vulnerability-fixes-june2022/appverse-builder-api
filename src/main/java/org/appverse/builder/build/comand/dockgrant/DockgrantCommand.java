package org.appverse.builder.build.comand.dockgrant;

import org.appverse.builder.build.comand.BuildCommand;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panthro on 07/03/16.
 */
public class DockgrantCommand extends BuildCommand {
    private boolean quiet;
    private boolean removeContainer;
    private String path;
    private String task;
    private String requestInputDir;
    private String imageUrl;
    private String imageName;
    private Map<String, String> environmentVariables = new HashMap<>();

    /**
     * This builds a list thhat represents the following command
     * <p>
     * structure:  {task} -v {requestInputDir}:{workDir} -w {workDir} --rm {imageName} '{script}'
     * <p>
     * example:  dockgrant run -v data:/tmp/data -w /tmp/data --rm --image hashicorp/precise64 --script "echo $VAR > file.txt && cat file.txt" -q
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
        args.add("--image");
        args.add(getImageName());
        if (path != null) {
            args.add("--path");
            args.add(getPath());
        }
        if (imageUrl != null) {
            args.add("--imageurl=" + getImageUrl());
        }
        if (getBuildScript() != null) {

        }
        if (getBuildScript() != null) {
            args.add("--script");
            if (!isCreateScript()) {
                args.add("sh -c \"" + getBuildScript() + "\"");
            } else {
                args.add("./" + getScriptFileName()); //TODO this is so ugly that it hurts, using the dirty and fast approach ¯\_(ツ)_/¯
            }

        }
        return args;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setRemoveContainer(boolean removeContainer) {
        this.removeContainer = removeContainer;
    }

    public boolean isRemoveContainer() {
        return removeContainer;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public void setRequestInputDir(String requestInputDir) {
        this.requestInputDir = requestInputDir;
    }

    public String getRequestInputDir() {
        return requestInputDir;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }
}
