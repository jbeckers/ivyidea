package org.clarent.ivyidea.resolve.dependency;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Guy Mahieu
 */
public class ExternalDependencyFactory {

    private static ExternalDependencyFactory instance = new ExternalDependencyFactory();

    public static ExternalDependencyFactory getInstance() {
        return instance;
    }

    @Nullable
    public ExternalDependency createExternalDependency(@NotNull Artifact artifact, @Nullable File file) {
        ResolvedArtifact resolvedArtifact = new ResolvedArtifact(artifact);
        if (resolvedArtifact.isSourceType()) {
            return new ExternalSourceDependency(artifact, file);
        }
        if (resolvedArtifact.isJavaDocType()) {
            return new ExternalJavaDocDependency(artifact, file);
        }
        if (resolvedArtifact.isClassesType()) {
            return new ExternalJarDependency(artifact, file);
        }
        return null;
    }

    /**
     * Wraps an ivy artifact and provides utility methods to check the type of artifact it is.
     *
     * @author Guy Mahieu
     */
    private static class ResolvedArtifact {

        // TODO: make these types configurable
        private static final String[] SOURCE_TYPES = {"source", "src", "sources", "srcs"};
        private static final String[] JAVADOC_TYPES = {"javadoc", "doc", "docs", "apidoc", "apidocs", "documentation", "documents"};
        private static final String[] CLASSES_TYPES = {"jar", "sar", "war", "ear"};

        private Artifact artifact;

        public ResolvedArtifact(@NotNull Artifact artifact) {
            this.artifact = artifact;
        }

        public boolean isSourceType() {
            return isOfType(Arrays.asList(SOURCE_TYPES));
        }

        public boolean isClassesType() {
            return isOfType(Arrays.asList(CLASSES_TYPES));
        }

        public boolean isJavaDocType() {
            return isOfType(Arrays.asList(JAVADOC_TYPES));
        }

        protected boolean isOfType(@NotNull Collection<String> types) {
            for (String type : types) {
                if (type.equals(artifact.getType())) {
                    return true;
                }
            }
            return false;
        }

    }
}