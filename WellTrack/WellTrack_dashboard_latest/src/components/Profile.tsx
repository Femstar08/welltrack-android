import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { ProfileHeader } from "./ProfileHeader";
import { HealthGoals } from "./HealthGoals";
import { DietaryPreferences } from "./DietaryPreferences";
import { AppSettings } from "./AppSettings";
import { FamilyManagement } from "./FamilyManagement";
import { DataPrivacy } from "./DataPrivacy";

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  avatar: string;
  age: number;
  gender: 'male' | 'female' | 'other';
  height: number; // cm
  weight: number; // kg
  activityLevel: 'sedentary' | 'lightly_active' | 'moderately_active' | 'very_active' | 'extra_active';
  healthScore: number;
  joinDate: string;
  isMainAccount: boolean;
  parentId?: string;
}

export interface HealthGoal {
  id: string;
  type: 'weight_loss' | 'weight_gain' | 'maintain_weight' | 'muscle_gain' | 'improve_fitness' | 'better_sleep' | 'reduce_stress';
  target: number;
  current: number;
  unit: string;
  deadline?: string;
  priority: 'high' | 'medium' | 'low';
  isActive: boolean;
}

export interface NutritionGoals {
  calories: number;
  protein: number; // percentage
  carbs: number; // percentage
  fats: number; // percentage
  fiber: number; // grams
  water: number; // liters
  sodium: number; // mg
  sugar: number; // grams
}

export interface DietaryRestriction {
  id: string;
  name: string;
  type: 'allergy' | 'intolerance' | 'preference' | 'lifestyle';
  severity: 'mild' | 'moderate' | 'severe';
  isActive: boolean;
}

export interface AppPreferences {
  theme: 'nature' | 'calm' | 'energizing' | 'dynamic';
  darkMode: boolean;
  fontSize: 'small' | 'medium' | 'large';
  notifications: {
    meals: boolean;
    supplements: boolean;
    goals: boolean;
    reports: boolean;
    doNotDisturbStart: string;
    doNotDisturbEnd: string;
  };
  units: {
    weight: 'kg' | 'lbs';
    height: 'cm' | 'ft';
    temperature: 'celsius' | 'fahrenheit';
  };
}

export interface HealthIntegration {
  id: string;
  name: string;
  type: 'fitness' | 'health' | 'nutrition' | 'sleep';
  isConnected: boolean;
  lastSync?: string;
  dataTypes: string[];
  autoSync: boolean;
}

export function Profile() {
  const [activeTab, setActiveTab] = useState("profile");
  const [currentUser, setCurrentUser] = useState<UserProfile>({
    id: '1',
    name: 'Sarah Johnson',
    email: 'sarah.johnson@email.com',
    avatar: 'https://images.unsplash.com/photo-1719168773674-1bafa68bf9ca?w=150&h=150&fit=crop&crop=face',
    age: 32,
    gender: 'female',
    height: 165,
    weight: 68,
    activityLevel: 'moderately_active',
    healthScore: 87,
    joinDate: '2024-01-15',
    isMainAccount: true
  });

  // Mock health goals
  const healthGoals: HealthGoal[] = [
    {
      id: '1',
      type: 'weight_loss',
      target: 65,
      current: 68,
      unit: 'kg',
      deadline: '2024-06-01',
      priority: 'high',
      isActive: true
    },
    {
      id: '2',
      type: 'improve_fitness',
      target: 10000,
      current: 7500,
      unit: 'steps/day',
      priority: 'medium',
      isActive: true
    },
    {
      id: '3',
      type: 'better_sleep',
      target: 8,
      current: 6.5,
      unit: 'hours',
      priority: 'high',
      isActive: true
    }
  ];

  // Mock nutrition goals
  const nutritionGoals: NutritionGoals = {
    calories: 1800,
    protein: 25,
    carbs: 45,
    fats: 30,
    fiber: 25,
    water: 2.5,
    sodium: 2300,
    sugar: 50
  };

  // Mock dietary restrictions
  const dietaryRestrictions: DietaryRestriction[] = [
    {
      id: '1',
      name: 'Gluten-Free',
      type: 'intolerance',
      severity: 'moderate',
      isActive: true
    },
    {
      id: '2',
      name: 'Vegetarian',
      type: 'lifestyle',
      severity: 'mild',
      isActive: true
    },
    {
      id: '3',
      name: 'Dairy-Free',
      type: 'preference',
      severity: 'mild',
      isActive: false
    }
  ];

  // Mock app preferences
  const appPreferences: AppPreferences = {
    theme: 'nature',
    darkMode: false,
    fontSize: 'medium',
    notifications: {
      meals: true,
      supplements: true,
      goals: true,
      reports: true,
      doNotDisturbStart: '22:00',
      doNotDisturbEnd: '07:00'
    },
    units: {
      weight: 'kg',
      height: 'cm',
      temperature: 'celsius'
    }
  };

  // Mock health integrations
  const healthIntegrations: HealthIntegration[] = [
    {
      id: '1',
      name: 'Google Health Connect',
      type: 'health',
      isConnected: true,
      lastSync: '2024-11-18T10:30:00Z',
      dataTypes: ['steps', 'heart_rate', 'sleep', 'weight'],
      autoSync: true
    },
    {
      id: '2',
      name: 'Garmin Connect',
      type: 'fitness',
      isConnected: false,
      dataTypes: ['workouts', 'heart_rate', 'sleep', 'stress'],
      autoSync: false
    },
    {
      id: '3',
      name: 'Samsung Health',
      type: 'health',
      isConnected: true,
      lastSync: '2024-11-18T09:15:00Z',
      dataTypes: ['steps', 'weight', 'blood_pressure'],
      autoSync: true
    }
  ];

  // Mock family members
  const familyMembers: UserProfile[] = [
    {
      id: '2',
      name: 'Mike Johnson',
      email: 'mike.johnson@email.com',
      avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face',
      age: 35,
      gender: 'male',
      height: 180,
      weight: 82,
      activityLevel: 'very_active',
      healthScore: 92,
      joinDate: '2024-02-01',
      isMainAccount: false,
      parentId: '1'
    },
    {
      id: '3',
      name: 'Emma Johnson',
      email: 'emma.johnson@email.com',
      avatar: 'https://images.unsplash.com/photo-1544348817-5f2cf14b88c8?w=150&h=150&fit=crop&crop=face',
      age: 12,
      gender: 'female',
      height: 150,
      weight: 45,
      activityLevel: 'moderately_active',
      healthScore: 85,
      joinDate: '2024-03-15',
      isMainAccount: false,
      parentId: '1'
    }
  ];

  return (
    <div className="flex flex-col h-full">
      {/* Profile Header */}
      <ProfileHeader 
        user={currentUser}
        familyMembers={familyMembers}
        healthScore={currentUser.healthScore}
        onUserSwitch={setCurrentUser}
      />

      {/* Main Content */}
      <div className="flex-1 overflow-hidden">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full flex flex-col">
          <TabsList className="grid w-full grid-cols-6 mx-4 mt-2">
            <TabsTrigger value="profile" className="text-xs">Profile</TabsTrigger>
            <TabsTrigger value="goals" className="text-xs">Goals</TabsTrigger>
            <TabsTrigger value="diet" className="text-xs">Diet</TabsTrigger>
            <TabsTrigger value="settings" className="text-xs">Settings</TabsTrigger>
            <TabsTrigger value="family" className="text-xs">Family</TabsTrigger>
            <TabsTrigger value="privacy" className="text-xs">Privacy</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-hidden">
            <TabsContent value="profile" className="h-full m-0">
              <ProfileHeader 
                user={currentUser}
                familyMembers={familyMembers}
                healthScore={currentUser.healthScore}
                onUserSwitch={setCurrentUser}
                showDetailedProfile={true}
              />
            </TabsContent>

            <TabsContent value="goals" className="h-full m-0">
              <HealthGoals 
                healthGoals={healthGoals}
                nutritionGoals={nutritionGoals}
                user={currentUser}
              />
            </TabsContent>

            <TabsContent value="diet" className="h-full m-0">
              <DietaryPreferences 
                dietaryRestrictions={dietaryRestrictions}
                user={currentUser}
              />
            </TabsContent>

            <TabsContent value="settings" className="h-full m-0">
              <AppSettings 
                preferences={appPreferences}
                integrations={healthIntegrations}
              />
            </TabsContent>

            <TabsContent value="family" className="h-full m-0">
              <FamilyManagement 
                mainUser={currentUser}
                familyMembers={familyMembers}
              />
            </TabsContent>

            <TabsContent value="privacy" className="h-full m-0">
              <DataPrivacy 
                user={currentUser}
                integrations={healthIntegrations}
              />
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>
  );
}