package io.github.shshdxk.liquibase.logging.mdc.customobjects;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.logging.mdc.CustomMdcObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MdcChangeset implements CustomMdcObject {
    private String changesetId;
    private String changesetAuthor;
    private String changesetFilepath;

    public static MdcChangeset fromChangeset(ChangeSet changeSet) {
        return new MdcChangeset(
                changeSet.getId(),
                changeSet.getAuthor(),
                changeSet.getFilePath());
    }
}
