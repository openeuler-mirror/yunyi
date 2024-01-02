package com.tongtech.console.domain.vo;

import com.tongtech.console.domain.RdsVersion;
import com.tongtech.console.domain.TemplateGroup;

import java.util.List;

public class RdsVersionVo extends RdsVersion {

    public RdsVersionVo(RdsVersion rv, List<TemplateGroup> templateGroups) {
        this.setVersionId(rv.getVersionId());
        this.setVersionNo(rv.getVersionNo());
        this.setDefaultGroupId(rv.getDefaultGroupId());
        this.setDefaultVersion(rv.isDefaultVersion());
        this.setStatus(rv.getStatus());
        this.setSoftwareName(rv.getSoftwareName());

        this.setCreateBy(rv.getCreateBy());
        this.setCreateTime(rv.getCreateTime());
        this.setUpdateBy(rv.getUpdateBy());
        this.setUpdateTime(rv.getUpdateTime());
        this.setRemark(rv.getRemark());
        this.setTemplateGroups(templateGroups);
    }

    private List<TemplateGroup> templateGroups;

    public List<TemplateGroup> getTemplateGroups() {
        return templateGroups;
    }

    public void setTemplateGroups(List<TemplateGroup> templateGroups) {
        this.templateGroups = templateGroups;
    }
}
