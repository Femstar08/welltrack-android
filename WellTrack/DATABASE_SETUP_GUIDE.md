# WellTrack Database Setup Guide

## Overview

Since the MCP server is configured for local Supabase development, I've created SQL scripts that you can run directly in your Supabase dashboard. This approach gives you full control and visibility over your database setup.

## Database Schema Files

I've created a complete database schema for your WellTrack app:

### 1. `01_user_profiles.sql`

- **user_profiles** table with all profile fields
- Row Level Security (RLS) policies
- Automatic timestamp updates
- Performance indexes

### 2. `02_recipes.sql`

- **recipes** table for storing user recipes
- **ingredients** table for ingredient library
- **recipe_ingredients** junction table
- Support for manual entry, URL import, and OCR scanning
- Public/private recipe sharing

### 3. `03_meals.sql`

- **meals** table for meal logging
- **meal_ingredients** for custom meals
- **meal_plans** and **meal_plan_items** for meal planning
- Meal scoring (A-E grades) and status tracking

### 4. `04_health_data.sql`

- **health_metrics** for fitness data
- **supplements** and **user_supplements** for supplement tracking
- **supplement_logs** for intake tracking
- **custom_habits** and **habit_logs** for habit tracking
- **biomarkers** for blood test results

### 5. `05_shopping_pantry.sql`

- **shopping_lists** and **shopping_list_items**
- **pantry_items** with expiry tracking
- **pantry_usage_logs** for inventory management
- **budget_tracking** and **cost_tracking** for expense management

### 6. `06_sample_data.sql` (Optional)

- Common ingredients with nutritional data
- Sample supplements
- Test data to get started quickly

## Setup Instructions

### Step 1: Configure Supabase Authentication

1. Go to your Supabase project dashboard
2. Navigate to **Authentication > Settings**
3. Enable **Email** authentication
4. Configure your site URL (for development: `http://localhost:3000`)
5. Optionally enable social providers (Google, Apple)

### Step 2: Update Your App Configuration

Update `WellTrack/app/src/main/java/com/beaconledger/welltrack/BuildConfig.kt`:

```kotlin
object BuildConfig {
    const val SUPABASE_URL = "https://your-actual-project-id.supabase.co"
    const val SUPABASE_ANON_KEY = "your-actual-anon-key-here"
}
```

### Step 3: Run Database Scripts

1. Go to your Supabase dashboard
2. Navigate to **SQL Editor**
3. Run the scripts **in order**:

   ```sql
   -- 1. First, run the user profiles script
   -- Copy and paste the contents of 01_user_profiles.sql

   -- 2. Then run the recipes script
   -- Copy and paste the contents of 02_recipes.sql

   -- 3. Continue with meals
   -- Copy and paste the contents of 03_meals.sql

   -- 4. Add health data tables
   -- Copy and paste the contents of 04_health_data.sql

   -- 5. Add shopping and pantry tables
   -- Copy and paste the contents of 05_shopping_pantry.sql

   -- 6. Optionally add sample data
   -- Copy and paste the contents of 06_sample_data.sql
   ```

### Step 4: Verify Setup

After running the scripts, you should see these tables in your Supabase **Table Editor**:

**Core Tables:**

- `user_profiles`
- `recipes`, `ingredients`, `recipe_ingredients`
- `meals`, `meal_ingredients`, `meal_plans`, `meal_plan_items`

**Health Tables:**

- `health_metrics`, `supplements`, `user_supplements`, `supplement_logs`
- `custom_habits`, `habit_logs`, `biomarkers`

**Shopping Tables:**

- `shopping_lists`, `shopping_list_items`
- `pantry_items`, `pantry_usage_logs`
- `budget_tracking`, `cost_tracking`

## Key Features Implemented

### 🔒 Security

- **Row Level Security (RLS)** enabled on all tables
- **User-specific policies** - users can only access their own data
- **Public ingredient library** - shared across all users
- **Secure authentication** integration with auth.users

### 📊 Data Structure

- **Normalized database design** with proper relationships
- **JSON fields** for flexible data storage (nutrition info, metadata)
- **Enum constraints** for data integrity
- **Automatic timestamps** with triggers

### 🚀 Performance

- **Strategic indexes** on frequently queried columns
- **Efficient queries** with proper foreign key relationships
- **Optimized for mobile app usage patterns**

### 🔄 Flexibility

- **Multi-user support** with profile switching
- **Extensible design** for future features
- **Import/export friendly** data structure

## Testing Your Setup

### 1. Test Authentication

- Build and run your Android app
- Try signing up with a new email
- Verify you can sign in and see the dashboard

### 2. Test Database Connection

- Check that user profiles can be created
- Verify RLS policies are working (users only see their data)

### 3. Verify Sample Data

If you ran the sample data script:

- Check the `ingredients` table has common foods
- Check the `supplements` table has sample supplements

## Next Steps

With the database setup complete, you can now:

1. **Test the authentication flow** in your app
2. **Implement profile creation** screens
3. **Continue with the next tasks** in your implementation plan
4. **Add more sample data** as needed for testing

## Troubleshooting

### Common Issues

**"relation does not exist" errors:**

- Make sure you ran the scripts in the correct order
- Check that all scripts completed successfully

**RLS policy errors:**

- Verify that RLS is enabled on tables
- Check that policies are created correctly
- Ensure your app is sending the correct auth headers

**Performance issues:**

- Check that indexes were created successfully
- Monitor query performance in Supabase dashboard

### Getting Help

- Check the Supabase documentation for RLS and policies
- Use the Supabase SQL Editor to test queries
- Monitor logs in the Supabase dashboard for errors

## Database Schema Diagram

```
auth.users (Supabase managed)
    ↓
user_profiles
    ↓
├── recipes → recipe_ingredients → ingredients
├── meals → meal_ingredients → ingredients
├── meal_plans → meal_plan_items → meals
├── health_metrics
├── user_supplements → supplements
├── supplement_logs → user_supplements
├── custom_habits → habit_logs
├── biomarkers
├── shopping_lists → shopping_list_items → ingredients
├── pantry_items → ingredients
├── pantry_usage_logs → pantry_items
├── budget_tracking
└── cost_tracking
```

Your WellTrack database is now ready for production use! 🎉
