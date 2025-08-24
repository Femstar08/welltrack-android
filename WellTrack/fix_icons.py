#!/usr/bin/env python3

import os
import re

# Icon replacements mapping
icon_replacements = {
    'Icons.Default.Fastfood': 'Icons.Default.Restaurant',
    'Icons.Default.Whatshot': 'Icons.Default.LocalFireDepartment',
    'Icons.Default.Water': 'Icons.Default.LocalDrink',
    'Icons.Default.DirectionsRun': 'Icons.Default.DirectionsWalk',
    'Icons.Default.Lightbulb': 'Icons.Default.TipsAndUpdates',
    'Icons.Default.ExpandLess': 'Icons.Default.KeyboardArrowUp',
    'Icons.Default.ExpandMore': 'Icons.Default.KeyboardArrowDown',
    'Icons.Default.Remove': 'Icons.Default.Delete',
    'Icons.Default.CameraEnhance': 'Icons.Default.CameraAlt',
    'Icons.Default.Link': 'Icons.Default.Link',
    'Icons.Default.ActivityLevel': 'Icons.Default.FitnessCenter',
    'Icons.Default.AttachMoney': 'Icons.Default.AttachMoney',
    'Icons.Default.Store': 'Icons.Default.Store',
    'Icons.Default.QrCode': 'Icons.Default.QrCode2',
    'Icons.Default.WbSunny': 'Icons.Default.WbSunny',
    'Icons.Default.FitnessCenter': 'Icons.Default.FitnessCenter',
    'Icons.Default.Bedtime': 'Icons.Default.Bedtime',
    'Icons.Default.ChevronLeft': 'Icons.Default.ChevronLeft',
    'Icons.Default.ChevronRight': 'Icons.Default.ChevronRight',
    'Icons.Default.People': 'Icons.Default.People',
    'Icons.Default.RadioButtonUnchecked': 'Icons.Default.RadioButtonUnchecked',
    'Icons.Default.Error': 'Icons.Default.Error',
    'Icons.Default.SearchOff': 'Icons.Default.SearchOff',
    'Icons.Default.LocalFireDepartment': 'Icons.Default.LocalFireDepartment',
    'Icons.Default.LocalDrink': 'Icons.Default.LocalDrink',
    'Icons.Default.Speed': 'Icons.Default.Speed',
    'Icons.Default.HealthAndSafety': 'Icons.Default.HealthAndSafety',
    'Icons.Default.AutoAwesome': 'Icons.Default.AutoAwesome',
    'Icons.Default.Kitchen': 'Icons.Default.Kitchen',
    'Icons.Default.DinnerDining': 'Icons.Default.DinnerDining',
    'Icons.Default.Cookie': 'Icons.Default.Cookie',
    'Icons.Default.MenuBook': 'Icons.Default.MenuBook',
    'Icons.Default.CalendarMonth': 'Icons.Default.CalendarMonth',
    'Icons.Default.Medication': 'Icons.Default.Medication',
    'Icons.Default.MonitorHeart': 'Icons.Default.MonitorHeart',
    'Icons.Default.Analytics': 'Icons.Default.Analytics',
    'Icons.Default.TrendingUp': 'Icons.Default.TrendingUp',
    'Icons.Default.WaterDrop': 'Icons.Default.WaterDrop',
    'Icons.Default.Bloodtype': 'Icons.Default.Bloodtype',
    'Icons.Default.AccessTime': 'Icons.Default.AccessTime',
    'Icons.Default.Snooze': 'Icons.Default.Snooze',
    'Icons.Default.Cancel': 'Icons.Default.Cancel',
    'Icons.Default.Camera': 'Icons.Default.CameraAlt',
    'Icons.Default.Group': 'Icons.Default.Group',
    'Icons.Default.Cloud': 'Icons.Default.Cloud',
    'Icons.Default.CloudOff': 'Icons.Default.CloudOff',
    'Icons.Default.CloudUpload': 'Icons.Default.CloudUpload',
    'Icons.Default.CloudDownload': 'Icons.Default.CloudDownload'
}

def fix_icons_in_file(file_path):
    """Fix icon references in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Apply icon replacements
        for old_icon, new_icon in icon_replacements.items():
            content = content.replace(old_icon, new_icon)
        
        # Only write if content changed
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
    app_dir = "app/src/main/java"
    
    if not os.path.exists(app_dir):
        print(f"Directory {app_dir} not found")
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
    
    print(f"\nProcessed {total_files} Kotlin files")
    print(f"Fixed icons in {fixed_files} files")

if __name__ == "__main__":
    main()