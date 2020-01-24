package org.jboss.da.common.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This class represents the configuration for global stuff in the unified config. We leave it as empty since we don't care
 * about it for now.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultModuleGroup extends AbstractModuleGroup {
}
