package org.jboss.da.listings.impl.backup;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.util.FileUtils;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.scm.api.SCMType;
import org.jboss.da.scm.impl.ScmFacade;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bean, which performs regular backup of the whitelist and blacklist to the remote SCM repository.
 * When the configuration contains empty URL to the SCM repository then the backup is disabled.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Stateless
public class BackupController {

    @Inject
    private Logger log;

    private static final String BACKUP_FILE_NAME = "blackWhiteListsBackup.json";

    @Inject
    private ScmFacade scmFacade;

    @Inject
    private Configuration configuration;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private BlackArtifactService blackService;

    private String backupScmUrl;

    private String backupBranch;

    private boolean disabled = false;

    @PostConstruct
    public void init() {
        try {
            backupScmUrl = configuration.getConfig().getBackupScmUrl();
            backupBranch = configuration.getConfig().getBackupScmBranch();
        } catch (ConfigurationParseException ex) {
            log.warn("Couldn't read configuration, BackupController is disabled");
        }
        if (backupScmUrl.isEmpty())
            disabled = true;
    }

    @Schedule(hour = "0", minute = "0", second = "0")
    public void doBackupLists() {
        if (disabled)
            return;

        log.info("Backup of the blacklist and whitelist started.");

        File tempDirectory = null;
        try {
            tempDirectory = Files.createTempDirectory("backup-").toFile();
            String backupString = storeArtifactsToString();
            scmFacade.cloneRepository(SCMType.GIT, backupScmUrl, backupBranch, tempDirectory);

            List<File> filesToCommit = createFile(tempDirectory, backupString);
            scmFacade.commitAndPush(SCMType.GIT, backupScmUrl, tempDirectory, filesToCommit,
                    ZonedDateTime.now() + ": Backup of the whitelist and blacklist");
        } catch (ScmException | IOException ex) {
            log.warn("Backup of the blacklist and whitelist FAILED!", ex);
            return;
        } finally {
            if (tempDirectory != null)
                FileUtils.deleteDirectory(tempDirectory);

        }
        log.info("Backup of the blacklist and whitelist successfully finished");
    }

    private List<File> createFile(File targetDirectory, String backupString)
            throws FileNotFoundException {
        File targetFile = new File(targetDirectory.getAbsolutePath() + "/" + BACKUP_FILE_NAME);
        try (PrintWriter writer = new PrintWriter(targetFile)) {
            writer.println(backupString);
        }

        List<File> files = new ArrayList<>();
        files.add(targetFile);
        return files;
    }

    private String storeArtifactsToString() throws JsonProcessingException {
        List<WhiteArtifact> whiteArtifacts = whiteService.getAll();
        List<BlackArtifact> blackArtifacts = blackService.getAll();
        BackupEntity backupEntity = new BackupEntity(whiteArtifacts, blackArtifacts);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(backupEntity);
    }

}
