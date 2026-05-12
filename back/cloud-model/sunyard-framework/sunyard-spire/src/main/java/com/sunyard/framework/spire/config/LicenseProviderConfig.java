package com.sunyard.framework.spire.config;

import com.sunyard.framework.spire.constant.SpireKeyConstants;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;

/**
 * @author yzy
 * @desc
 * @since 2025/10/31
 */
@Configuration
public class LicenseProviderConfig {


    @PostConstruct
    public void initLicense() {
        // 设置 License Key
        com.spire.doc.license.LicenseProvider.setLicenseKey(SpireKeyConstants.SPIRE_KEY);
        com.spire.xls.license.LicenseProvider.setLicenseKey(SpireKeyConstants.SPIRE_KEY);
        com.spire.presentation.license.LicenseProvider.setLicenseKey(SpireKeyConstants.SPIRE_KEY);
        com.spire.pdf.license.LicenseProvider.setLicenseKey(SpireKeyConstants.SPIRE_KEY);
    }
}
