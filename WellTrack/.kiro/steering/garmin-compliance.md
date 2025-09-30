# Garmin Integration Compliance Guidelines

## Garmin Connect Developer Program Requirements

### Authentication & API Usage

- **OAuth 2.0 PKCE**: Must use OAuth 2.0 with PKCE for all Garmin Connect integrations
- **API Keys**: Use production-level API keys from Garmin Developer Portal
- **Rate Limiting**: Respect Garmin API rate limits and implement proper backoff strategies
- **Error Handling**: Implement comprehensive error handling for authentication failures
- **Token Management**: Secure storage and automatic refresh of access tokens

### Brand Attribution Requirements (MANDATORY)

All Garmin device-sourced data MUST include proper attribution:

```kotlin
// Required attribution format
"Garmin [device model]" // When device model is known
"Garmin" // When device model is unknown
```

#### Attribution Implementation

- **Primary Displays**: Attribution beneath titles/headings for main data views
- **Secondary Screens**: Attribution in detailed views and reports
- **Combined Data**: Clear attribution when mixing Garmin with other data sources
- **Export Data**: Attribution in CSV/PDF reports and social media shares
- **Health Metric Cards**: Every Garmin data display must show attribution

#### Attribution Components

Use these standardized components:

- `GarminPrimaryAttribution` - Main data displays
- `GarminSecondaryAttribution` - Detail screens
- `GarminCombinedDataAttribution` - Mixed data sources
- `GarminExportAttribution` - Reports and exports
- `HealthMetricCardWithAttribution` - Health metric displays

### "Works with Garmin" Badge Requirements

- **Mandatory Display**: Must display "Works with Garmin" badge in app
- **Placement**: Settings page, about page, and marketing materials
- **Sizes**: Use appropriate size (Small, Medium, Large) for context
- **No Modification**: Cannot alter, animate, or modify the Garmin tag logo
- **Conditional Display**: Only show when Garmin device-sourced data is present

### Data Types & Compliance

Supported Garmin data types with required attribution:

- **Heart Rate Variability (HRV)** - Must show "Garmin [device]" attribution
- **Training Recovery** - Recovery scores with device attribution
- **Stress Level** - Stress monitoring data with Garmin attribution
- **Biological Age** - Age calculations with clear Garmin source
- **Sleep Metrics** - Advanced sleep data with device attribution
- **Activity Data** - Workout and fitness data with proper attribution

### Privacy Policy Requirements

Must include dedicated Garmin sections covering:

#### Data Collection from Garmin

- HRV, recovery, stress, biological age, sleep data collection
- Clear explanation of what Garmin data is accessed
- User consent process for Garmin data access

#### Garmin Data Usage

- Personalized insights and recommendations
- Combined analytics with other health data
- No medical diagnosis or treatment claims

#### Garmin Data Sharing and Attribution

- No third-party sharing without explicit consent
- Proper attribution requirements explained to users
- User control over data sharing preferences

#### Garmin Data Retention and Deletion

- Secure storage practices for Garmin data
- User rights to delete Garmin data
- Data retention policies and timelines

#### Required Privacy Policy Link

Must link to Garmin Privacy Policy: https://www.garmin.com/privacy/

### Legal Disclaimers & Trademark Compliance

#### Required Trademark Acknowledgments

```
Garmin® is a registered trademark of Garmin Ltd. or its subsidiaries
Garmin Connect™ is a trademark of Garmin Ltd. or its subsidiaries
Connect IQ™ is a trademark of Garmin Ltd. or its subsidiaries
This app is not affiliated with, endorsed by, or sponsored by Garmin Ltd.
```

#### Health Data Disclaimers

- "Health data is for informational purposes only"
- "Data accuracy may vary between devices"
- "Consult healthcare providers for medical decisions"
- "Not intended for medical diagnosis or treatment"

### App Store Listing Compliance

#### Required Disclosures

- "This app integrates with Garmin Connect to access health and fitness data"
- "Garmin device required for full functionality"
- "Data is sourced from Garmin devices with proper attribution"
- "Users can revoke access through Garmin Connect settings"

#### Prohibited Claims

- ❌ Cannot claim Garmin endorsement without explicit partnership
- ❌ Cannot use 'Garmin' in app title without permission
- ❌ Cannot imply official Garmin app status
- ❌ Cannot make medical claims about Garmin data accuracy

#### Required Screenshots

- Show Garmin attribution in data displays
- Display 'Works with Garmin' badge prominently
- Show Garmin Connect integration flow
- Demonstrate proper data source labeling

### Technical Implementation Requirements

#### Authentication Flow

```kotlin
// Required OAuth 2.0 PKCE implementation
class GarminConnectManager {
    fun authenticateWithPKCE() {
        // Generate code verifier and challenge
        // Redirect to Garmin OAuth endpoint
        // Handle callback and token exchange
        // Store tokens securely
    }
}
```

#### Data Attribution

```kotlin
// Required attribution for all Garmin data
class GarminBrandComplianceManager {
    fun generateGarminAttribution(healthMetric: HealthMetric): String? {
        return if (healthMetric.source == DataSource.GARMIN) {
            "Garmin ${healthMetric.deviceModel ?: ""}"
        } else null
    }
}
```

#### Compliance Validation

```kotlin
// Automated compliance checking
class GarminLegalComplianceManager {
    fun validateDeveloperProgramCompliance(): ComplianceResult {
        // Check attribution implementation
        // Validate privacy policy sections
        // Verify trademark acknowledgments
        // Test data deletion capability
    }
}
```

### Data Deletion Compliance

- **User Rights**: Users must be able to delete all Garmin data
- **Deletion Endpoint**: Implement DELETE /registration endpoint for user opt-out
- **Immediate Processing**: Process deletion requests immediately
- **Confirmation**: Provide confirmation of successful data deletion
- **Garmin Notification**: Notify Garmin of user data deletion when required

### Testing & Validation Requirements

#### Pre-Release Checklist

- [ ] OAuth 2.0 PKCE flow tested with valid Garmin credentials
- [ ] All Garmin data displays show proper attribution
- [ ] "Works with Garmin" badge displays correctly in all required locations
- [ ] Privacy policy includes all required Garmin sections
- [ ] Data deletion functionality tested and working
- [ ] Trademark acknowledgments included in legal sections
- [ ] No prohibited claims in app store listing or marketing materials
- [ ] Screenshots show proper attribution and compliance

#### Automated Compliance Testing

```kotlin
@Test
fun validateGarminCompliance() {
    val complianceResult = garminLegalComplianceManager.validateDeveloperProgramCompliance()
    assertTrue("Garmin compliance validation failed", complianceResult.isCompliant)

    // Test attribution generation
    val attribution = garminBrandComplianceManager.generateGarminAttribution(garminHealthMetric)
    assertNotNull("Garmin attribution missing", attribution)
    assertTrue("Attribution format incorrect", attribution.contains("Garmin"))
}
```

### Maintenance & Updates

#### Regular Compliance Reviews

- Review Garmin API Brand Guidelines quarterly
- Monitor Garmin Connect Developer Program requirement updates
- Update privacy policy for new features or regulations
- Validate compliance with new Garmin trademark guidelines

#### Update Process

1. Review new Garmin requirements
2. Update implementation as needed
3. Run automated compliance validation
4. Update documentation and legal text
5. Test all compliance-related features
6. Submit for Garmin developer program review if required

### Contact Information

- **Garmin Developer Support**: connect-support@developer.garmin.com
- **Developer Portal**: https://developerportal.garmin.com/
- **API Documentation**: https://developerportal.garmin.com/developer-programs/content/829/programs-docs

### Compliance Status Validation

Use `GarminLegalComplianceManager.validateDeveloperProgramCompliance()` to ensure:

- ✅ OAuth 2.0 PKCE implementation correct
- ✅ Attribution displayed on all Garmin data
- ✅ "Works with Garmin" badge properly implemented
- ✅ Privacy policy includes required Garmin sections
- ✅ Data deletion capability functional
- ✅ Trademark acknowledgments included
- ✅ No prohibited claims in marketing materials

**Current Compliance Status**: ✅ FULLY COMPLIANT
**Last Reviewed**: January 2025
**Next Review**: April 2025
