# WellTrack Database Table Names

All WellTrack database tables now use the `wt_` prefix for better organization and to avoid naming conflicts.

## Updated Table Names

### User Profiles (01_user_profiles.sql)
- `wt_user_profiles` - User profile information

### Recipes (02_recipes.sql)
- `wt_recipes` - Recipe information
- `wt_ingredients` - Master ingredient list
- `wt_recipe_ingredients` - Junction table for recipe ingredients

### Meals (03_meals.sql)
- `wt_meals` - Individual meal records
- `wt_meal_ingredients` - Custom meal ingredients (not from recipes)
- `wt_meal_plans` - Meal planning schedules
- `wt_meal_plan_items` - Individual items in meal plans

### Health Data (04_health_data.sql)
- `wt_health_metrics` - Health measurements (weight, heart rate, etc.)
- `wt_supplements` - Master supplement list
- `wt_user_supplements` - User's supplement library
- `wt_supplement_logs` - Supplement intake tracking
- `wt_custom_habits` - User-defined habits
- `wt_habit_logs` - Habit completion tracking
- `wt_biomarkers` - Lab test results and biomarkers

### Shopping & Pantry (05_shopping_pantry.sql)
- `wt_shopping_lists` - Shopping list management
- `wt_shopping_list_items` - Items in shopping lists
- `wt_pantry_items` - Pantry inventory
- `wt_pantry_usage_logs` - Pantry item usage tracking
- `wt_budget_tracking` - Budget management
- `wt_cost_tracking` - Cost tracking for items

## Benefits of the `wt_` Prefix

1. **Namespace Organization**: Clearly identifies WellTrack tables in shared databases
2. **Conflict Prevention**: Avoids naming conflicts with other applications
3. **Easy Identification**: Makes it easy to identify WellTrack tables in database tools
4. **Professional Structure**: Follows enterprise database naming conventions
5. **Scalability**: Allows for easy expansion without naming conflicts

## Migration Notes

If you have existing tables without the `wt_` prefix, you'll need to:

1. **Backup your data** before running the new schema
2. **Drop existing tables** or rename them
3. **Run the updated SQL files** in order (01 through 06)
4. **Migrate your data** to the new table structure if needed

## Usage in Application Code

When updating your application code, remember to update all table references:

```sql
-- Old
SELECT * FROM user_profiles WHERE id = $1;

-- New
SELECT * FROM wt_user_profiles WHERE id = $1;
```

All database queries, DAOs, and repository implementations should be updated to use the new table names.