# Material Icons Quick Reference for WellTrack

## Common Icon Mistakes and Corrections

This reference lists Material Icons that do NOT exist and their correct alternatives.

### Icons That Don't Exist ❌

| Invalid Icon | Correct Alternative | Use Case |
|--------------|---------------------|----------|
| `Icons.Default.Group` | `Icons.Default.People` | Groups, family members |
| `Icons.Default.AccessTime` | `Icons.Default.Schedule` | Time, scheduling |
| `Icons.Default.CloudDone` | `Icons.Default.CloudQueue` or `Icons.Default.Check` | Cloud sync success |
| `Icons.Default.Upload` | `Icons.Default.CloudUpload` or `Icons.Default.UploadFile` | Upload actions |
| `Icons.Default.Download` | `Icons.Default.CloudDownload` or `Icons.Default.DownloadForOffline` | Download actions |

### Commonly Used Valid Icons ✅

#### Cloud & Sync
- `Icons.Default.Cloud` - Generic cloud
- `Icons.Default.CloudQueue` - Cloud sync/online
- `Icons.Default.CloudOff` - Cloud offline
- `Icons.Default.CloudUpload` - Upload to cloud
- `Icons.Default.CloudDownload` - Download from cloud
- `Icons.Default.Sync` - Sync icon

#### Time & Schedule
- `Icons.Default.Schedule` - Clock/time
- `Icons.Default.Timer` - Timer
- `Icons.Default.Alarm` - Alarm
- `Icons.Default.AccessAlarm` - Access alarm
- `Icons.Default.Today` - Today/calendar

#### People & Social
- `Icons.Default.People` - Multiple people
- `Icons.Default.Person` - Single person
- `Icons.Default.PersonAdd` - Add person
- `Icons.Default.PersonRemove` - Remove person

#### File Operations
- `Icons.Default.UploadFile` - Upload file
- `Icons.Default.DownloadForOffline` - Download file
- `Icons.Default.FileOpen` - Open file
- `Icons.Default.FileCopy` - Copy file

#### Common UI
- `Icons.Default.Check` - Checkmark
- `Icons.Default.Close` - Close/X
- `Icons.Default.Delete` - Delete
- `Icons.Default.Edit` - Edit
- `Icons.Default.Share` - Share
- `Icons.Default.Settings` - Settings
- `Icons.Default.Info` - Information
- `Icons.Default.Warning` - Warning
- `Icons.Default.Error` - Error

## How to Verify Icon Existence

### Method 1: IDE Auto-completion
Type `Icons.Default.` and wait for IntelliJ IDEA to show available options.

### Method 2: Check Material Icons Documentation
Visit: https://fonts.google.com/icons?icon.set=Material+Icons

### Method 3: Check androidx.compose.material.icons Package
The icon must exist in the Material Icons library to be used with `Icons.Default.*`

## Import Statement

Always ensure you have the correct import:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
```

## Notes

1. **Filled vs Outlined:** `Icons.Default.*` uses the filled variant. For outlined icons, use `Icons.Outlined.*`
2. **Custom Icons:** If you need an icon not in Material Icons, consider using a custom vector drawable
3. **Icon Naming:** Material Icons often have different names than you might expect - always verify before use

## Updated Files in This Project

The following files have been corrected for icon usage:
- `SocialComponents.kt` - People, Schedule icons
- `DataSyncScreen.kt` - CloudQueue, CloudUpload, CloudDownload icons
- `DataExportScreen.kt` - CloudUpload, CloudDownload icons
- `SecuritySettingsComponents.kt` - CloudDownload icon

## Best Practices

1. ✅ Always test icon display in the UI
2. ✅ Use semantically correct icons (e.g., `CloudUpload` for cloud operations, not generic `Upload`)
3. ✅ Provide `contentDescription` for accessibility
4. ✅ Use consistent icon sizing with `Modifier.size()`
5. ✅ Match icon tint to your theme colors

## Common Patterns in WellTrack

```kotlin
// Standard icon usage
Icon(
    imageVector = Icons.Default.CloudUpload,
    contentDescription = "Upload to cloud",
    tint = MaterialTheme.colorScheme.primary,
    modifier = Modifier.size(16.dp)
)

// Conditional icon based on state
Icon(
    imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
    contentDescription = if (isOnline) "Online" else "Offline",
    tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
)
```

---
Last Updated: 2025-10-03
