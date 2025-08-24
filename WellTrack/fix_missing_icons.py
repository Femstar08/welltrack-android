#!/usr/bin/env python3
"""
Script to fix missing Material Icons in WellTrack Android app
Replaces non-existent icons with available alternatives
"""

import os
import re

# Icon replacements mapping
ICON_REPLACEMENTS = {
    'Icons.Default.Restaurant': 'Icons.Default.Fastfood',
    'Icons.Default.LocalFireDepartment': 'Icons.Default.Whatshot',
    'Icons.Default.LocalDrink': 'Icons.Default.LocalBar',
    'Icons.Default.LocalDrinkDrop': 'Icons.Default.LocalBar',
    'Icons.Default.DirectionsRun': 'Icons.Default.DirectionsWalk',
    'Icons.Default.WbSunny': 'Icons.Default.WbSunny',
    'Icons.Default.FitnessCenter': 'Icons.Default.FitnessCenter',
    'Icons.Default.Bedtime': 'Icons.Default.Hotel',
    'Icons.Default.ChevronLeft': 'Icons.Default.ChevronLeft',
    'Icons.Default.ChevronRight': 'Icons.Default.ChevronRight',
    'Icons.Default.Kitchen': 'Icons.Default.Kitchen',
    'Icons.Default.DinnerDining': 'Icons.Default.Fastfood',
    'Icons.Default.Cookie': 'Icons.Default.Cake',
    'Icons.Default.AutoAwesome': 'Icons.Default.Star',
    'Icons.Default.TipsAndUpdates': 'Icons.Default.Lightbulb',
    'Icons.Default.People': 'Icons.Default.Group',
    'Icons.Default.RadioButtonUnchecked': 'Icons.Default.RadioButtonUnchecked',
    'Icons.Default.MonetizationOn': 'Icons.Default.AttachMoney',
    'Icons.Default.AttachMoney': 'Icons.Default.AttachMoney',
    'Icons.Default.Store': 'Icons.Default.Store',
    'Icons.Default.QrCode2': 'Icons.Default.QrCode',
    'Icons.Default.CameraAlt': 'Icons.Default.CameraAlt',
    'Icons.Default.CameraAltAlt': 'Icons.Default.CameraAlt',
    'Icons.Default.AccessTime': 'Icons.Default.Schedule',
    'Icons.Default.Medication': 'Icons.Default.LocalPharmacy',
    'Icons.Default.TrendingUp': 'Icons.Default.TrendingUp',
    'Icons.Default.Bloodtype': 'Icons.Default.Bloodtype',
    'Icons.Default.Snooze': 'Icons.Default.Snooze',
    'Icons.Default.Cancel': 'Icons.Default.Cancel',
    'Icons.Default.Group': 'Icons.Default.Group',
    'Icons.Default.Cloud': 'Icons.Default.Cloud',
    'Icons.Default.CloudOff': 'Icons.Default.CloudOff',
    'Icons.Default.CloudUpload': 'Icons.Default.CloudUpload',
    'Icons.Default.CloudDownload': 'Icons.Default.CloudDownload',
    'Icons.Default.HealthAndSafety': 'Icons.Default.HealthAndSafety',
    'Icons.Default.Speed': 'Icons.Default.Speed',
    'Icons.Default.SearchOff': 'Icons.Default.SearchOff',
    'Icons.Default.Error': 'Icons.Default.Error',
}

def fix_icons_in_file(file_path):
    """Fix missing icons in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Apply replacements
        for old_icon, new_icon in ICON_REPLACEMENTS.items():
            content = content.replace(old_icon, new_icon)
        
        # Only write if changes were made
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
    """Main function to fix icons in all Kotlin files"""
    app_src_dir = "app/src/main/java"
    
    if not os.path.exists(app_src_dir):
        print(f"Directory {app_src_dir} not found!")
        return
    
    fixed_files = 0
    total_files = 0
    
    # Walk through all Kotlin files
    for root, dirs, files in os.walk(app_src_dir):
        for file in files:
            if file.endswith('.kt'):
                file_path = os.path.join(root, file)
                total_files += 1
                
                if fix_icons_in_file(file_path):
                    fixed_files += 1
    
    print(f"\nProcessed {total_files} Kotlin files")
    print(f"Fixed icons in {fixed_files} files")

if __name__ == "__main__":
    main()