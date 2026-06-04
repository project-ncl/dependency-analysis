package org.jboss.da.common.config.validator;

import java.util.EnumSet;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.jboss.da.common.config.Configuration;
import org.jboss.pnc.api.enums.RepositoryType;

public class RepositoryConfigValidator implements ConstraintValidator<ValidRepositoryConfig, Configuration> {

    @Override
    public boolean isValid(Configuration config, ConstraintValidatorContext context) {
        return switch (config.repositoryManagerSupport()) {
            case INDY -> validateIndyConfig(config.indy(), context);
            case ARTIFACTORY -> validateArtifactoryConfig(config.artifactory(), context);
            case DISABLED -> true;
        };
    }

    private boolean validateIndyConfig(Configuration.Indy config, ConstraintValidatorContext context) {
        if (config.indyUrl() == null || config.indyUrl().isBlank()) {
            context.buildConstraintViolationWithTemplate("da.indy.indy-url is empty").addConstraintViolation();
            return false;
        }
        if (config.indyGroup() == null || config.indyGroup().isBlank()) {
            context.buildConstraintViolationWithTemplate("da.indy.indy-group is empty").addConstraintViolation();
            return false;
        }
        if (config.indyGroupPublic() == null || config.indyGroupPublic().isBlank()) {
            context.buildConstraintViolationWithTemplate("da.indy.indy-group-public is empty").addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateArtifactoryConfig(Configuration.Artifactory config, ConstraintValidatorContext context) {
        if (config.url().isEmpty()) {
            context.buildConstraintViolationWithTemplate("da.artifactory.url is empty").addConstraintViolation();
            return false;
        }

        if (config.accessToken().isEmpty()) {
            context.buildConstraintViolationWithTemplate("da.artifactory.access-token is empty")
                    .addConstraintViolation();
            return false;
        }

        var requiredTypes = EnumSet.of(RepositoryType.NPM, RepositoryType.MAVEN);
        if (config.groups() == null || !config.groups().keySet().containsAll(requiredTypes)) {
            context.buildConstraintViolationWithTemplate("da.artifactory.groups is empty or missing repository types")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
