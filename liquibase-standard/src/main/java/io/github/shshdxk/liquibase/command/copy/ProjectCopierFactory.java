package io.github.shshdxk.liquibase.command.copy;

import io.github.shshdxk.liquibase.plugin.AbstractPluginFactory;

public class ProjectCopierFactory extends AbstractPluginFactory<ProjectCopier>  {

    private ProjectCopierFactory() {
    }

    @Override
    protected Class<ProjectCopier> getPluginClass() {
        return ProjectCopier.class;
    }

    @Override
    protected int getPriority(ProjectCopier copier, Object... args) {
        if (args.length == 0) {
            return -1;
        }
        return copier.getPriority((String)args[0]);
    }

    public ProjectCopier getProjectCopier(String path) {
        return getPlugin(path);
    }
}
