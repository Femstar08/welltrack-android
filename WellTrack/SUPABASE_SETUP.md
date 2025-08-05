# Supabase Setup for WellTrack

## Prerequisites

1. Create a Supabase account at [supabase.com](https://supabase.com)
2. Create a new project in your Supabase dashboard

## Configuration Steps

### 1. Get Your Supabase Credentials

1. Go to your Supabase project dashboard
2. Navigate to Settings > API
3. Copy your:
   - Project URL (looks like: `https://your-project-id.supabase.co`)
   - Anon/Public key (starts with `eyJ...`)

### 2. Update BuildConfig.kt

Replace the placeholder values in `app/src/main/java/com/beaconledger/welltrack/BuildConfig.kt`:

```kotlin
object BuildConfig {
    const val SUPABASE_URL = "https://your-actual-project-id.supabase.co"
    const val SUPABASE_ANON_KEY = "your-actual-anon-key-here"
}
```

### 3. Set Up Authentication

1. In your Supabase dashboard, go to Authentication > Settings
2. Configure your site URL (for development: `http://localhost:3000`)
3. Enable email authentication
4. Optionally enable social providers (Google, Apple, etc.)

### 4. Database Setup (Optional)

The app will work with just authentication, but for full functionality, you can set up these tables:

```sql
-- User profiles table
CREATE TABLE user_profiles (
    id UUID REFERENCES auth.users(id) PRIMARY KEY,
    name TEXT NOT NULL,
    age INTEGER,
    height REAL,
    weight REAL,
    activity_level TEXT,
    fitness_goals TEXT,
    dietary_restrictions TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;

-- Policy to allow users to see only their own profile
CREATE POLICY "Users can view own profile" ON user_profiles
    FOR SELECT USING (auth.uid() = id);

-- Policy to allow users to update their own profile
CREATE POLICY "Users can update own profile" ON user_profiles
    FOR UPDATE USING (auth.uid() = id);

-- Policy to allow users to insert their own profile
CREATE POLICY "Users can insert own profile" ON user_profiles
    FOR INSERT WITH CHECK (auth.uid() = id);
```

## Testing

1. Build and run the app
2. Try signing up with a new email
3. Check your email for verification (if email confirmation is enabled)
4. Try signing in with your credentials
5. You should see the authenticated dashboard

## Troubleshooting

- **"Invalid credentials" error**: Check that your Supabase URL and anon key are correct
- **Network errors**: Ensure your device/emulator has internet access
- **Email not received**: Check spam folder, or disable email confirmation in Supabase Auth settings for testing
- **Build errors**: Make sure all Supabase dependencies are properly installed

## Next Steps

Once authentication is working:

1. Set up the database tables for recipes, meals, etc.
2. Implement profile creation flow
3. Continue with the remaining tasks in your implementation plan
