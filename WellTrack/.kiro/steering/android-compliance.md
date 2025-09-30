# Android App Compliance Guidelines

## Google Play Store Requirements

### App Manifest Compliance

- **Target SDK**: Must target API 36 (Android 15) or higher
- **Min SDK**: API 26 minimum for Health Connect compatibility
- **Permissions**: Request only necessary permissions with runtime permission handling
- **Backup Rules**: Implement proper data extraction and backup rules (`@xml/data_extraction_rules`, `@xml/backup_rules`)

### Required Permissions & Justification

```xml
<!-- Core Health Data -->
<uses-permission android:name="android.permission.health.READ_*" /> <!-- Health Connect integration -->

<!-- Security & Authentication -->
<uses-permission android:name="android.permission.USE_BIOMETRIC" /> <!-- Secure app access -->
<uses-permission android:name="android.permission.USE_FINGERPRINT" /> <!-- Legacy biometric support -->

<!-- Camera & Storage -->
<uses-permission android:name="android.permission.CAMERA" /> <!-- Recipe OCR scanning -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /> <!-- Image import -->

<!-- Notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> <!-- Meal reminders -->
<uses-permission android:name="android.permission.VIBRATE" /> <!-- Timer alerts -->

<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" /> <!-- API calls -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <!-- Connection status -->
```

### Privacy & Data Protection

- **Privacy Policy**: Must include comprehensive health data handling policies
- **Data Deletion**: Implement complete user data deletion capability
- **Consent Management**: Clear opt-in/opt-out for all data collection
- **Encryption**: All sensitive data must be encrypted at rest and in transit
- **Biometric Security**: Implement secure biometric authentication for health data access

### Accessibility Compliance (WCAG 2.1 AA)

- **Screen Reader Support**: Full TalkBack compatibility
- **Touch Targets**: Minimum 48dp touch targets (44dp with accessibility services)
- **Color Contrast**: 4.5:1 ratio for normal text, 3:1 for large text
- **Focus Management**: Proper focus order and visual indicators
- **Content Descriptions**: Meaningful descriptions for all UI elements
- **Text Scaling**: Support up to 200% text scaling
- **Animation Controls**: Respect system animation preferences

### Health Data Compliance

- **Health Connect Integration**: Proper implementation of Health Connect APIs
- **Medical Disclaimers**: Clear disclaimers that app is not medical advice
- **Data Accuracy**: Disclaimers about device data accuracy limitations
- **Professional Consultation**: Encourage users to consult healthcare providers

### App Store Listing Requirements

- **Age Rating**: 12+ (Medical/Treatment Information)
- **Category**: Health & Fitness (Primary), Food & Drink (Secondary)
- **Content Rating**: Appropriate for health and medical information display
- **Screenshots**: Must show proper data attribution and privacy controls
- **Description**: Include all required health data disclosures

### Security Requirements

- **Network Security**: Use HTTPS for all API communications
- **Certificate Pinning**: Implement for critical API endpoints
- **Token Security**: Secure storage of authentication tokens
- **Session Management**: Proper session timeout and cleanup
- **Input Validation**: Sanitize all user inputs to prevent injection attacks

### Performance & Quality

- **ANR Prevention**: No Application Not Responding errors
- **Crash Rate**: <2% crash rate for stable releases
- **Battery Optimization**: Efficient background processing
- **Memory Management**: Proper memory cleanup and leak prevention
- **Startup Time**: App launch under 3 seconds on target devices

### Testing Requirements

- **Unit Tests**: Minimum 70% code coverage
- **Integration Tests**: Test all external API integrations
- **UI Tests**: Automated testing of critical user flows
- **Accessibility Tests**: Automated accessibility validation
- **Security Tests**: Penetration testing for sensitive data handling
- **Performance Tests**: Load testing for data synchronization

### Build Configuration Compliance

- **ProGuard/R8**: Enable code obfuscation for release builds
- **Signing**: Proper app signing with secure keystore
- **Build Variants**: Separate debug, staging, and release configurations
- **Environment Variables**: Secure handling of API keys and secrets
- **16KB Page Size**: Support for Android 15+ 16KB page size requirements

### Prohibited Content & Behavior

- **Medical Claims**: Cannot make medical diagnosis or treatment claims
- **Misleading Information**: No false or misleading health information
- **Unauthorized Data Access**: Only access explicitly permitted health data
- **Third-Party Sharing**: No unauthorized sharing of health data
- **Malicious Behavior**: No data harvesting or unauthorized tracking

### Compliance Validation Checklist

- [ ] All permissions properly justified and documented
- [ ] Privacy policy covers all data collection and usage
- [ ] Health data disclaimers prominently displayed
- [ ] Accessibility features tested with screen readers
- [ ] Security measures implemented and tested
- [ ] App store listing includes all required disclosures
- [ ] Performance benchmarks met
- [ ] Legal compliance validated (GDPR, CCPA, HIPAA considerations)
