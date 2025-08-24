import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { Progress } from "./ui/progress";
import { 
  Edit,
  Camera,
  Settings,
  Trophy,
  Target,
  Calendar,
  Users,
  ChevronDown,
  CheckCircle,
  Star,
  Activity,
  Heart,
  Zap
} from "lucide-react";
import { UserProfile } from "./Profile";

interface ProfileHeaderProps {
  user: UserProfile;
  familyMembers: UserProfile[];
  healthScore: number;
  onUserSwitch: (user: UserProfile) => void;
  showDetailedProfile?: boolean;
}

export function ProfileHeader({ 
  user, 
  familyMembers, 
  healthScore, 
  onUserSwitch, 
  showDetailedProfile = false 
}: ProfileHeaderProps) {
  const [showUserSwitcher, setShowUserSwitcher] = useState(false);
  const [showEditProfile, setShowEditProfile] = useState(false);

  const allUsers = [user, ...familyMembers];

  const getHealthScoreColor = (score: number) => {
    if (score >= 90) return 'text-green-600';
    if (score >= 80) return 'text-blue-600';
    if (score >= 70) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getHealthScoreGrade = (score: number) => {
    if (score >= 95) return 'A+';
    if (score >= 90) return 'A';
    if (score >= 85) return 'A-';
    if (score >= 80) return 'B+';
    if (score >= 75) return 'B';
    if (score >= 70) return 'B-';
    if (score >= 65) return 'C+';
    return 'C';
  };

  const getActivityLevelDisplay = (level: string) => {
    switch (level) {
      case 'sedentary': return 'Sedentary';
      case 'lightly_active': return 'Lightly Active';
      case 'moderately_active': return 'Moderately Active';
      case 'very_active': return 'Very Active';
      case 'extra_active': return 'Extra Active';
      default: return 'Unknown';
    }
  };

  const achievements = [
    { id: '1', name: '7-Day Streak', icon: '🔥', earned: true },
    { id: '2', name: 'Goal Crusher', icon: '🎯', earned: true },
    { id: '3', name: 'Meal Prep Master', icon: '👨‍🍳', earned: false },
    { id: '4', name: 'Fitness Enthusiast', icon: '💪', earned: true },
    { id: '5', name: 'Hydration Hero', icon: '💧', earned: false }
  ];

  if (!showDetailedProfile) {
    return (
      <div className="bg-card border-b border-border px-4 py-4">
        {/* Profile Header Row */}
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-xl font-semibold text-card-foreground">Profile</h1>
          <Button variant="outline" size="sm" className="gap-1.5">
            <Edit className="w-4 h-4" />
            Edit
          </Button>
        </div>

        {/* User Profile Card */}
        <div className="flex items-center gap-4">
          {/* Profile Photo */}
          <div className="relative">
            <Avatar className="w-16 h-16">
              <AvatarImage src={user.avatar} alt={user.name} />
              <AvatarFallback>{user.name.split(' ').map(n => n[0]).join('')}</AvatarFallback>
            </Avatar>
            <Button 
              size="sm" 
              className="absolute -bottom-1 -right-1 h-6 w-6 p-0 rounded-full bg-blue-600 hover:bg-blue-700"
            >
              <Camera className="w-3 h-3" />
            </Button>
          </div>

          {/* User Info */}
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <h2 className="font-semibold text-card-foreground">{user.name}</h2>
              {user.isMainAccount && (
                <Badge className="text-xs bg-blue-100 text-blue-800 dark:bg-blue-950">
                  Main Account
                </Badge>
              )}
            </div>
            <p className="text-sm text-muted-foreground mb-2">{user.email}</p>
            
            {/* Quick Stats */}
            <div className="flex items-center gap-4 text-sm text-muted-foreground">
              <span>{user.age} years</span>
              <span>{user.height}cm</span>
              <span>{user.weight}kg</span>
            </div>
          </div>

          {/* Health Score */}
          <div className="text-center">
            <div className={`text-2xl font-semibold ${getHealthScoreColor(healthScore)}`}>
              {healthScore}
            </div>
            <p className="text-xs text-muted-foreground">Health Score</p>
            <Badge className={`text-xs mt-1 ${getHealthScoreColor(healthScore)} bg-transparent border`}>
              Grade {getHealthScoreGrade(healthScore)}
            </Badge>
          </div>
        </div>

        {/* User Switcher */}
        {familyMembers.length > 0 && (
          <div className="mt-4 pt-4 border-t border-border">
            <Button
              variant="outline"
              className="w-full justify-between gap-2"
              onClick={() => setShowUserSwitcher(!showUserSwitcher)}
            >
              <div className="flex items-center gap-2">
                <Users className="w-4 h-4" />
                Switch Account ({allUsers.length})
              </div>
              <ChevronDown className="w-4 h-4" />
            </Button>

            {showUserSwitcher && (
              <div className="mt-2 space-y-2">
                {allUsers.map((member) => (
                  <Button
                    key={member.id}
                    variant={member.id === user.id ? "default" : "ghost"}
                    className="w-full justify-start gap-3 h-auto p-3"
                    onClick={() => {
                      onUserSwitch(member);
                      setShowUserSwitcher(false);
                    }}
                  >
                    <Avatar className="w-8 h-8">
                      <AvatarImage src={member.avatar} alt={member.name} />
                      <AvatarFallback className="text-xs">
                        {member.name.split(' ').map(n => n[0]).join('')}
                      </AvatarFallback>
                    </Avatar>
                    <div className="text-left">
                      <p className="font-medium">{member.name}</p>
                      <p className="text-xs text-muted-foreground">
                        Health Score: {member.healthScore}
                      </p>
                    </div>
                    {member.id === user.id && (
                      <CheckCircle className="w-4 h-4 ml-auto" />
                    )}
                  </Button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    );
  }

  // Detailed profile view
  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Profile Information */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            Personal Information
            <Button variant="outline" size="sm" onClick={() => setShowEditProfile(true)}>
              <Edit className="w-4 h-4 mr-1" />
              Edit
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Profile Photo and Basic Info */}
          <div className="flex items-center gap-6">
            <div className="relative">
              <Avatar className="w-24 h-24">
                <AvatarImage src={user.avatar} alt={user.name} />
                <AvatarFallback className="text-lg">
                  {user.name.split(' ').map(n => n[0]).join('')}
                </AvatarFallback>
              </Avatar>
              <Button 
                size="sm" 
                className="absolute -bottom-2 -right-2 h-8 w-8 p-0 rounded-full bg-blue-600 hover:bg-blue-700"
              >
                <Camera className="w-4 h-4" />
              </Button>
            </div>

            <div className="flex-1 space-y-3">
              <div>
                <h2 className="text-xl font-semibold text-card-foreground">{user.name}</h2>
                <p className="text-muted-foreground">{user.email}</p>
              </div>
              
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">Age</p>
                  <p className="font-medium">{user.age} years</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Gender</p>
                  <p className="font-medium capitalize">{user.gender}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Height</p>
                  <p className="font-medium">{user.height} cm</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Weight</p>
                  <p className="font-medium">{user.weight} kg</p>
                </div>
              </div>
            </div>
          </div>

          {/* Activity Level */}
          <div className="bg-muted/30 rounded-lg p-4">
            <div className="flex items-center gap-2 mb-2">
              <Activity className="w-4 h-4 text-blue-500" />
              <h3 className="font-medium">Activity Level</h3>
            </div>
            <p className="text-sm text-muted-foreground mb-3">
              {getActivityLevelDisplay(user.activityLevel)}
            </p>
            <div className="grid grid-cols-5 gap-1">
              {['sedentary', 'lightly_active', 'moderately_active', 'very_active', 'extra_active'].map((level, index) => (
                <div 
                  key={level}
                  className={`h-2 rounded-full ${
                    index <= ['sedentary', 'lightly_active', 'moderately_active', 'very_active', 'extra_active'].indexOf(user.activityLevel)
                      ? 'bg-blue-500' 
                      : 'bg-muted'
                  }`}
                />
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Health Score Dashboard */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Heart className="w-5 h-5 text-red-500" />
            Health Score Overview
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Main Health Score */}
          <div className="text-center">
            <div className={`text-4xl font-semibold ${getHealthScoreColor(healthScore)} mb-2`}>
              {healthScore}
            </div>
            <Badge className={`${getHealthScoreColor(healthScore)} bg-transparent border text-sm px-3 py-1`}>
              Grade {getHealthScoreGrade(healthScore)}
            </Badge>
            <p className="text-sm text-muted-foreground mt-2">
              Overall Health Performance
            </p>
          </div>

          {/* Health Score Breakdown */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Zap className="w-4 h-4 text-green-500" />
                <span className="text-sm">Nutrition</span>
              </div>
              <div className="flex items-center gap-2">
                <Progress value={85} className="w-20 h-2" />
                <span className="text-sm font-medium w-8">85</span>
              </div>
            </div>
            
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Activity className="w-4 h-4 text-blue-500" />
                <span className="text-sm">Fitness</span>
              </div>
              <div className="flex items-center gap-2">
                <Progress value={92} className="w-20 h-2" />
                <span className="text-sm font-medium w-8">92</span>
              </div>
            </div>
            
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Target className="w-4 h-4 text-purple-500" />
                <span className="text-sm">Goals</span>
              </div>
              <div className="flex items-center gap-2">
                <Progress value={78} className="w-20 h-2" />
                <span className="text-sm font-medium w-8">78</span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4 text-orange-500" />
                <span className="text-sm">Consistency</span>
              </div>
              <div className="flex items-center gap-2">
                <Progress value={90} className="w-20 h-2" />
                <span className="text-sm font-medium w-8">90</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Achievements */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="w-5 h-5 text-yellow-500" />
            Achievements
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-5 gap-3">
            {achievements.map((achievement) => (
              <div 
                key={achievement.id}
                className={`text-center p-3 rounded-lg border transition-all ${
                  achievement.earned 
                    ? 'border-yellow-200 bg-yellow-50 dark:border-yellow-800 dark:bg-yellow-950' 
                    : 'border-muted bg-muted/30'
                }`}
              >
                <div className={`text-2xl mb-2 ${!achievement.earned ? 'grayscale opacity-50' : ''}`}>
                  {achievement.icon}
                </div>
                <p className={`text-xs font-medium ${
                  achievement.earned ? 'text-card-foreground' : 'text-muted-foreground'
                }`}>
                  {achievement.name}
                </p>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Account Information */}
      <Card>
        <CardHeader>
          <CardTitle>Account Details</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-muted-foreground">Member Since</p>
              <p className="font-medium">
                {new Date(user.joinDate).toLocaleDateString('en-US', { 
                  year: 'numeric', 
                  month: 'long', 
                  day: 'numeric' 
                })}
              </p>
            </div>
            <div>
              <p className="text-muted-foreground">Account Type</p>
              <p className="font-medium">
                {user.isMainAccount ? 'Main Account' : 'Family Member'}
              </p>
            </div>
          </div>

          {familyMembers.length > 0 && (
            <div>
              <p className="text-muted-foreground text-sm mb-2">Family Members</p>
              <div className="flex -space-x-2">
                {familyMembers.slice(0, 4).map((member) => (
                  <Avatar key={member.id} className="w-8 h-8 border-2 border-background">
                    <AvatarImage src={member.avatar} alt={member.name} />
                    <AvatarFallback className="text-xs">
                      {member.name.split(' ').map(n => n[0]).join('')}
                    </AvatarFallback>
                  </Avatar>
                ))}
                {familyMembers.length > 4 && (
                  <div className="w-8 h-8 rounded-full bg-muted border-2 border-background flex items-center justify-center">
                    <span className="text-xs font-medium">+{familyMembers.length - 4}</span>
                  </div>
                )}
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}