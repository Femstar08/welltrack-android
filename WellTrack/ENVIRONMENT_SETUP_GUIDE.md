# WellTrack Environment Setup Guide

This guide will help you configure the WellTrack app with all necessary API keys and environment variables.

## 📋 Prerequisites

Before setting up the environment, ensure you have:

- Android Studio installed
- A Supabase account and project
- Developer accounts for external integrations (optional)

## 🔧 Environment Configuration

### Step 1: Copy Environment Template

1. Navigate to the `WellTrack` directory
2. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

### Step 2: Configure Required Services

#### 🗄️ Supabase (Required)

1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Create a new project or select existing one
3. Go to Settings → API
4. Copy the following values to your `.env` file:
   ```env
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your_supabase_anon_key_here
   SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key_here
   ```

#### ⌚ Garmin Connect Integration (Optional)

1. Register at [Garmin Developer Portal](https://developer.garmin.com/)
2. Create a new app in the Connect IQ section
3. Configure OAuth settings:
   - Redirect URI: `welltrack://garmin/callback`
   - Scopes: `ghs-read` (Garmin Health Snapshot)
4. Add to `.env`:
   ```env
   GARMIN_CLIENT_ID=your_garmin_client_id_here
   GARMIN_CLIENT_SECRET=your_garmin_client_secret_here
   ```

#### 📱 Samsung Health Integration (Optional)

1. Register at [Samsung Developers](https://developer.samsung.com/health)
2. Create a new Health app
3. Configure permissions for health data access
4. Add to `.env`:
   ```env
   SAMSUNG_HEALTH_APP_ID=your_samsung_health_app_id_here
   SAMSUNG_HEALTH_CLIENT_SECRET=your_samsung_health_client_secret_here
   ```

#### 🤖 OpenAI Integration (Optional)

1. Go to [OpenAI Platform](https://platform.openai.com/)
2. Create an API key
3. Add to `.env`:
   ```env
   OPENAI_API_KEY=your_openai_api_key_here
   OPENAI_ORGANIZATION_ID=your_openai_org_id_here
   ```

### Step 3: Security Configuration

Generate secure keys for encryption:

```bash
# Generate a 32-character encryption key
openssl rand -base64 32

# Generate a JWT secret
openssl rand -base64 64
```

Add to `.env`:

```env
ENCRYPTION_KEY=your_generated_32_character_key
JWT_SECRET=your_generated_jwt_secret
```

### Step 4: Optional External APIs

#### 🍽️ Recipe and Nutrition APIs

**Spoonacular API:**

1. Register at [Spoonacular](https://spoonacular.com/food-api)
2. Get your API key
3. Add to `.env`:
   ```env
   SPOONACULAR_API_KEY=your_spoonacular_api_key_here
   ```

**Edamam API:**

1. Register at [Edamam](https://developer.edamam.com/)
2. Get your App ID and App Key
3. Add to `.env`:
   ```env
   EDAMAM_APP_ID=your_edamam_app_id_here
   EDAMAM_APP_KEY=your_edamam_app_key_here
   ```

## 🏗️ Build Configuration

### Environment Types

The app supports three build environments:

1. **Development** (`debug`)

   - Full logging enabled
   - Debug features available
   - Uses `.debug` app ID suffix

2. **Staging** (`staging`)

   - Limited logging
   - Testing features enabled
   - Uses `.staging` app ID suffix

3. **Production** (`release`)
   - Minimal logging
   - Optimized performance
   - Production app ID

### Building the App

```bash
# Debug build
./gradlew assembleDebug

# Staging build
./gradlew assembleStaging

# Release build
./gradlew assembleRelease
```

## 🔒 Security Best Practices

### Environment File Security

1. **Never commit `.env` to version control**

   - The `.env` file is already in `.gitignore`
   - Use `.env.example` as a template

2. **Use different keys for different environments**

   - Development keys for testing
   - Production keys for release

3. **Rotate keys regularly**
   - Change API keys periodically
   - Update encryption keys for major releases

### Key Storage

- API keys are stored in `BuildConfig` during build
- Sensitive tokens are encrypted using Android Keystore
- User data is encrypted before storage

## 🧪 Testing Configuration

### Validate Your Setup

1. Open the app
2. Navigate to Settings → Configuration
3. Check the configuration status
4. Resolve any issues shown

### Manual Testing

```bash
# Run configuration tests
./gradlew testDebugUnitTest --tests="*ConfigurationTest*"

# Run security tests
./gradlew testDebugUnitTest --tests="*SecurityTest*"
```

## 🚨 Troubleshooting

### Common Issues

#### "Supabase connection failed"

- Verify `SUPABASE_URL` and `SUPABASE_ANON_KEY`
- Check network connectivity
- Ensure Supabase project is active

#### "Garmin authentication failed"

- Verify `GARMIN_CLIENT_ID` and redirect URI
- Check Garmin Developer Portal app status
- Ensure OAuth scopes are correct

#### "Security validation failed"

- Check Android Keystore availability
- Verify device security settings
- Clear app data and retry

#### "Build configuration errors"

- Ensure `.env` file exists in project root
- Check for syntax errors in `.env`
- Verify all required variables are set

### Getting Help

1. Check the [Configuration Screen] in the app
2. Review build logs for specific errors
3. Validate environment variables format
4. Test with minimal configuration first

## 📚 Additional Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Garmin Connect IQ Developer Guide](https://developer.garmin.com/connect-iq/)
- [Samsung Health SDK Documentation](https://developer.samsung.com/health)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)

## 🔄 Environment Updates

When updating environment configuration:

1. Update `.env` file with new values
2. Clean and rebuild the project:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```
3. Test configuration in the app
4. Update `.env.example` if adding new variables

---

**Note:** Keep your `.env` file secure and never share API keys publicly. Use environment-specific configurations for different deployment stages.
