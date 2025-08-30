#!/usr/bin/env python3

import os
import re

# Icon replacements mapping - Replace missing icons with available alternatives
icon_replacements = {
    # Missing icons from compilation errors - replace with available alternatives
    'Icons.Default.Fastfood': 'Icons.Default.Restaurant',
    'Icons.Default.LocalFireDepartment': 'Icons.Default.Whatshot',
    'Icons.Default.WaterDrop': 'Icons.Default.LocalDrink',
    'Icons.Default.DirectionsWalk': 'Icons.Default.DirectionsRun',
    'Icons.Default.TipsAndUpdates': 'Icons.Default.Lightbulb',
    'Icons.Default.MonetizationOn': 'Icons.Default.AttachMoney',
    'Icons.Default.Restaurant': 'Icons.Default.Restaurant',
    'Icons.Default.AttachMoney': 'Icons.Default.AttachMoney',
    'Icons.Default.Store': 'Icons.Default.Store',
    'Icons.Default.QrCode2': 'Icons.Default.QrCode',
    'Icons.Default.WbSunny': 'Icons.Default.WbSunny',
    'Icons.Default.FitnessCenter': 'Icons.Default.FitnessCenter',
    'Icons.Default.Bedtime': 'Icons.Default.NightsStay',
    'Icons.Default.ChevronLeft': 'Icons.Default.KeyboardArrowLeft',
    'Icons.Default.ChevronRight': 'Icons.Default.KeyboardArrowRight',
    'Icons.Default.People': 'Icons.Default.Person',
    'Icons.Default.RadioButtonUnchecked': 'Icons.Default.RadioButtonUnchecked',
    'Icons.Default.Error': 'Icons.Default.Error',
    'Icons.Default.SearchOff': 'Icons.Default.SearchOff',
    'Icons.Default.LocalDrink': 'Icons.Default.LocalDrink',
    'Icons.Default.Speed': 'Icons.Default.Speed',
    'Icons.Default.HealthAndSafety': 'Icons.Default.HealthAndSafety',
    'Icons.Default.AutoAwesome': 'Icons.Default.Star',
    'Icons.Default.Kitchen': 'Icons.Default.Kitchen',
    'Icons.Default.DinnerDining': 'Icons.Default.Restaurant',
    'Icons.Default.Cookie': 'Icons.Default.Cookie',
    'Icons.Default.Medication': 'Icons.Default.LocalPharmacy',
    'Icons.Default.TrendingUp': 'Icons.Default.TrendingUp',
    'Icons.Default.LocalDrinkDrop': 'Icons.Default.LocalDrink',
    'Icons.Default.Bloodtype': 'Icons.Default.Bloodtype',
    'Icons.Default.AccessTime': 'Icons.Default.AccessTime',
    'Icons.Default.Snooze': 'Icons.Default.Snooze',
    'Icons.Default.Cancel': 'Icons.Default.Cancel',
    'Icons.Default.CameraAltAlt': 'Icons.Default.CameraAlt',
    'Icons.Default.CameraAlt': 'Icons.Default.CameraAlt',
    'Icons.Default.Group': 'Icons.Default.Group',
    'Icons.Default.Cloud': 'Icons.Default.Cloud',
    'Icons.Default.CloudOff': 'Icons.Default.CloudOff',
    'Icons.Default.CloudUpload': 'Icons.Default.CloudUpload',
    'Icons.Default.CloudDownload': 'Icons.Default.CloudDownload',
    
    # Additional missing icons from compilation errors
    'Icons.Default.Whatshot': 'Icons.Default.LocalFireDepartment',
    'Icons.Default.NightsStay': 'Icons.Default.Bedtime',
    'Icons.Default.LocalBar': 'Icons.Default.LocalDrink',
    'Icons.Default.LocalPharmacy': 'Icons.Default.Medication',
    'Icons.Default.Schedule': 'Icons.Default.AccessTime',
    'Icons.Default.Cake': 'Icons.Default.Restaurant',
    'Icons.Default.EmojiObjects': 'Icons.Default.Lightbulb',
    'Icons.Default.MealStatus': 'Icons.Default.Restaurant',
    
    # Fix non-icon references that are being treated as icons
    'OutlinedButtonDefaults': 'ButtonDefaults',
    '.background': '.background',
    'CircleShape': 'CircleShape'
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