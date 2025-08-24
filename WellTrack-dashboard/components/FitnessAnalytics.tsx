import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import { 
  Activity,
  Heart,
  Zap,
  Moon,
  TrendingUp,
  TrendingDown,
  Timer,
  Target
} from "lucide-react";
import { TimePeriod } from "./HealthAnalytics";

interface FitnessAnalyticsProps {
  timePeriod: TimePeriod;
}

export function FitnessAnalytics({ timePeriod }: FitnessAnalyticsProps) {
  // Mock fitness data
  const fitnessData = [
    { date: '11/01', steps: 8500, activeMinutes: 45, calories: 320, hrv: 42, recoveryScore: 78 },
    { date: '11/02', steps: 9200, activeMinutes: 52, calories: 380, hrv: 38, recoveryScore: 72 },
    { date: '11/03', steps: 12000, activeMinutes: 68, calories: 520, hrv: 45, recoveryScore: 85 },
    { date: '11/04', steps: 7800, activeMinutes: 35, calories: 280, hrv: 40, recoveryScore: 68 },
    { date: '11/05', steps: 10500, activeMinutes: 58, calories: 425, hrv: 43, recoveryScore: 82 },
    { date: '11/06', steps: 11200, activeMinutes: 62, calories: 450, hrv: 41, recoveryScore: 79 },
    { date: '11/07', steps: 9800, activeMinutes: 55, calories: 395, hrv: 44, recoveryScore: 81 }
  ];

  const workoutData = [
    { type: 'Strength', sessions: 3, duration: 135, calories: 810, lastSession: '2 days ago' },
    { type: 'Cardio', sessions: 2, duration: 90, calories: 650, lastSession: '1 day ago' },
    { type: 'Yoga', sessions: 2, duration: 60, calories: 200, lastSession: '3 days ago' },
    { type: 'Walking', sessions: 7, duration: 210, calories: 420, lastSession: 'Today' }
  ];

  const biomarkers = [
    { name: 'Resting HR', current: 62, target: '60-65', unit: 'bpm', trend: 'down', status: 'good' },
    { name: 'HRV', current: 42, target: '40-50', unit: 'ms', trend: 'up', status: 'good' },
    { name: 'Recovery', current: 78, target: '75+', unit: '%', trend: 'up', status: 'excellent' },
    { name: 'Sleep Score', current: 73, target: '80+', unit: '%', trend: 'stable', status: 'fair' }
  ];

  const averages = {
    steps: Math.round(fitnessData.reduce((sum, day) => sum + day.steps, 0) / fitnessData.length),
    activeMinutes: Math.round(fitnessData.reduce((sum, day) => sum + day.activeMinutes, 0) / fitnessData.length),
    calories: Math.round(fitnessData.reduce((sum, day) => sum + day.calories, 0) / fitnessData.length),
    hrv: Math.round(fitnessData.reduce((sum, day) => sum + day.hrv, 0) / fitnessData.length),
    recovery: Math.round(fitnessData.reduce((sum, day) => sum + day.recoveryScore, 0) / fitnessData.length)
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'excellent': return 'text-green-600 bg-green-100 dark:bg-green-950';
      case 'good': return 'text-blue-600 bg-blue-100 dark:bg-blue-950';
      case 'fair': return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-950';
      case 'poor': return 'text-red-600 bg-red-100 dark:bg-red-950';
      default: return 'text-muted-foreground bg-muted';
    }
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'up': return <TrendingUp className="w-3 h-3 text-green-500" />;
      case 'down': return <TrendingDown className="w-3 h-3 text-red-500" />;
      default: return <div className="w-3 h-3 bg-yellow-500 rounded-full" />;
    }
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Fitness Summary Cards */}
      <div className="grid grid-cols-2 gap-3">
        <Card>
          <CardContent className="p-3 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Activity className="w-4 h-4 text-blue-600" />
              <p className="text-lg font-semibold text-blue-600">{averages.steps.toLocaleString()}</p>
            </div>
            <p className="text-xs text-muted-foreground">Avg Daily Steps</p>
            <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200 text-xs mt-1">
              Target: 10K
            </Badge>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Timer className="w-4 h-4 text-green-600" />
              <p className="text-lg font-semibold text-green-600">{averages.activeMinutes}</p>
            </div>
            <p className="text-xs text-muted-foreground">Active Minutes</p>
            <Badge className="bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200 text-xs mt-1">
              Target: 60
            </Badge>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Zap className="w-4 h-4 text-orange-600" />
              <p className="text-lg font-semibold text-orange-600">{averages.calories}</p>
            </div>
            <p className="text-xs text-muted-foreground">Calories Burned</p>
            <Badge variant="secondary" className="text-xs mt-1">
              Daily Avg
            </Badge>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Heart className="w-4 h-4 text-red-600" />
              <p className="text-lg font-semibold text-red-600">{averages.recovery}%</p>
            </div>
            <p className="text-xs text-muted-foreground">Recovery Score</p>
            <div className="flex items-center justify-center mt-1">
              <TrendingUp className="w-3 h-3 text-green-500" />
              <span className="text-xs text-green-500 ml-1">+2.3%</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Activity Trends */}
      <Card>
        <CardHeader>
          <CardTitle>Activity Trends ({timePeriod})</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={fitnessData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis yAxisId="steps" orientation="left" />
                <YAxis yAxisId="minutes" orientation="right" />
                <Tooltip />
                <Line 
                  yAxisId="steps"
                  type="monotone" 
                  dataKey="steps" 
                  stroke="#3b82f6" 
                  strokeWidth={2}
                  name="Steps"
                />
                <Line 
                  yAxisId="minutes"
                  type="monotone" 
                  dataKey="activeMinutes" 
                  stroke="#10b981" 
                  strokeWidth={2}
                  name="Active Minutes"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Workout Analysis */}
      <Card>
        <CardHeader>
          <CardTitle>Workout Analysis</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {workoutData.map((workout) => (
              <div key={workout.type} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
                <div>
                  <h4 className="font-medium text-card-foreground">{workout.type}</h4>
                  <p className="text-sm text-muted-foreground">
                    {workout.sessions} sessions • {workout.duration}min total
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-medium text-card-foreground">{workout.calories} cal</p>
                  <p className="text-xs text-muted-foreground">{workout.lastSession}</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Recovery & HRV Trends */}
      <Card>
        <CardHeader>
          <CardTitle>Recovery & HRV Trends</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={fitnessData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis yAxisId="hrv" orientation="left" domain={[30, 50]} />
                <YAxis yAxisId="recovery" orientation="right" domain={[60, 90]} />
                <Tooltip />
                <Line 
                  yAxisId="hrv"
                  type="monotone" 
                  dataKey="hrv" 
                  stroke="#8b5cf6" 
                  strokeWidth={2}
                  name="HRV (ms)"
                />
                <Line 
                  yAxisId="recovery"
                  type="monotone" 
                  dataKey="recoveryScore" 
                  stroke="#f59e0b" 
                  strokeWidth={2}
                  name="Recovery %"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Biomarker Status */}
      <Card>
        <CardHeader>
          <CardTitle>Health Biomarkers</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {biomarkers.map((marker) => (
            <div key={marker.name} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
              <div className="flex items-center gap-3">
                <Heart className="w-4 h-4 text-red-500" />
                <div>
                  <h4 className="font-medium text-card-foreground">{marker.name}</h4>
                  <p className="text-sm text-muted-foreground">Target: {marker.target}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <p className="font-medium text-card-foreground">
                    {marker.current}{marker.unit}
                  </p>
                  <Badge className={`text-xs px-2 py-1 ${getStatusColor(marker.status)}`}>
                    {marker.status}
                  </Badge>
                </div>
                {getTrendIcon(marker.trend)}
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Weekly Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Weekly Fitness Summary</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Total workouts</span>
              <span className="font-medium">14 sessions</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Active days</span>
              <span className="font-medium text-green-600">7/7 days</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Total calories burned</span>
              <span className="font-medium">{(averages.calories * 7).toLocaleString()} cal</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Recovery average</span>
              <span className="font-medium text-blue-600">{averages.recovery}%</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}