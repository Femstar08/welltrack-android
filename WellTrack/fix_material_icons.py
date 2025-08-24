#!/usr/bin/env python3
"""
Script to fix Material Icons issues by replacing unavailable icons with available alternatives
"""

import os
import re

# Icon replacements mapping unavailable icons to available ones
ICON_REPLACEMENTS = {
    'Restaurant': 'Fastfood',
    'LocalFireDepartment': 'Whatshot',
    'WaterDrop': 'Water',
    'DirectionsWalk': 'DirectionsWalk',  # This should be available
    'DirectionsRun': 'DirectionsWalk',
    'Bedtime': 'NightlightRound',
    'LocalDrink': 'LocalBar',
    'Speed': 'Speed',  # This should be available
    'FitnessCenter': 'FitnessCenter',  # This should be available
    'HealthAndSafety': 'HealthAndSafety',  # This should be available
    'TrendingUp': 'TrendingUp',  # This should be available
    'Medication': 'MedicalServices',
    'Bloodtype': 'Bloodtype',  # This should be available
    'AccessTime': 'AccessTime',  # This should be available
    'Snooze': 'Snooze',  # This should be available
    'Cancel': 'Cancel',  # This should be available
    'CameraEnhance': 'CameraAlt',
    'Camera': 'CameraAlt',
    'CameraAltAlt': 'CameraAlt',
    'AutoAwesome': 'Star',
    'Kitchen': 'Kitchen',  # This should be available
    'WbSunny': 'WbSunny',  # This should be available
    'DinnerDining': 'Restaurant',
    'Cookie': 'Cookie',  # This should be available
    'ChevronLeft': 'ChevronLeft',  # This should be available
    'ChevronRight': 'ChevronRight',  # This should be available
    'People': 'People',  # This should be available
    'RadioButtonUnchecked': 'RadioButtonUnchecked',  # This should be available
    'Lightbulb': 'EmojiObjects',
    'TipsAndUpdates': 'EmojiObjects',
    'MonetizationOn': 'AttachMoney',
    'AttachMoney': 'AttachMoney',  # This should be available
    'Store': 'Store',  # This should be available
    'QrCode': 'QrCode2',
    'QrCode2': 'QrCode2',  # This should be available
    'Group': 'Group',  # This should be available
    'Cloud': 'Cloud',  # This should be available
    'CloudOff': 'CloudOff',  # This should be available
    'CloudUpload': 'CloudUpload',  # This should be available
    'CloudDownload': 'CloudDownload',  # This should be available
    'SearchOff': 'SearchOff',  # This should be available
    'Error': 'Error',  # This should be available
    'Remove': 'Remove',  # This should be available
}

def fix_icons_in_file(file_path):
    """Fix icon references in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Replace icon references
        for old_icon, new_icon in ICON_REPLACEMENTS.items():
            # Pattern to match Icons.Default.IconName
            pattern = f'Icons\\.Default\\.{old_icon}'
            replacement = f'Icons.Default.{new_icon}'
            content = re.sub(pattern, replacement, content)
            
            # Pattern to match Icons.Filled.IconName
            pattern = f'Icons\\.Filled\\.{old_icon}'
            replacement = f'Icons.Filled.{new_icon}'
            content = re.sub(pattern, replacement, content)
        
        # Write back if changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed icons in: {file_path}")
            return True
        return False
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False

def main():
    """Main function to fix all icon issues"""
    app_dir = "app/src/main/java/com/beaconledger/welltrack"
    
    if not os.path.exists(app_dir):
        print(f"Directory {app_dir} not found!")
        return
    
    fixed_files = 0
    total_files = 0
    
    # Walk through all Kotlin files
    for root, dirs, files in os.walk(app_dir):
        for file in files:
            if file.endswith('.kt'):
                file_path = os.path.join(root, file)
                total_files += 1
                if fix_icons_in_file(file_path):
                    fixed_files += 1
    
    print(f"\nIcon fix complete!")
    print(f"Processed {total_files} Kotlin files")
    print(f"Fixed icons in {fixed_files} files")

if __name__ == "__main__":
    main()