import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { HealthAnalyticsHeader } from "./HealthAnalyticsHeader";
import { HealthScoreDashboard } from "./HealthScoreDashboard";
import { AIInsightsPanel } from "./AIInsightsPanel";
import { NutritionAnalytics } from "./NutritionAnalytics";
import { FitnessAnalytics } from "./FitnessAnalytics";
import { GoalProgressTracking } from "./GoalProgressTracking";

export type TimePeriod = '7d' | '30d' | '3m' | '1y';

export interface HealthMetrics {
  overallScore: number;
  nutritionScore: number;
  fitnessScore: number;
  supplementScore: number;
  sleepScore?: number;
  weekOverWeekChange: number;
}

export interface NutritionData {
  date: string;
  protein: number;
  carbs: number;
  fats: number;
  calories: number;
  fiber: number;
  score: number;
}

export interface GoalProgress {
  id: string;
  name: string;
  current: number;
  target: number;
  unit: string;
  category: 'nutrition' | 'fitness' | 'wellness';
  trend: 'up' | 'down' | 'stable';
  completion: number;
}

export function HealthAnalytics() {
  const [activeTab, setActiveTab] = useState("overview");
  const [timePeriod, setTimePeriod] = useState<TimePeriod>('30d');
  const [selectedProfile, setSelectedProfile] = useState("main");

  // Mock health metrics data
  const healthMetrics: HealthMetrics = {
    overallScore: 78,
    nutritionScore: 82,
    fitnessScore: 75,
    supplementScore: 88,
    sleepScore: 73,
    weekOverWeekChange: 3.2
  };

  // Mock nutrition data for the selected time period
  const nutritionData: NutritionData[] = [
    { date: '2024-11-01', protein: 85, carbs: 45, fats: 30, calories: 1850, fiber: 28, score: 85 },
    { date: '2024-11-02', protein: 78, carbs: 52, fats: 28, calories: 1920, fiber: 25, score: 78 },
    { date: '2024-11-03', protein: 92, carbs: 48, fats: 35, calories: 2010, fiber: 32, score: 88 },
    { date: '2024-11-04', protein: 76, carbs: 55, fats: 25, calories: 1780, fiber: 22, score: 72 },
    { date: '2024-11-05', protein: 88, carbs: 42, fats: 32, calories: 1950, fiber: 30, score: 86 },
    { date: '2024-11-06', protein: 82, carbs: 50, fats: 28, calories: 1880, fiber: 27, score: 81 },
    { date: '2024-11-07', protein: 95, carbs: 44, fats: 38, calories: 2080, fiber: 35, score: 92 }
  ];

  const goals: GoalProgress[] = [
    {
      id: '1',
      name: 'Daily Protein Goal',
      current: 85,
      target: 100,
      unit: 'g',
      category: 'nutrition',
      trend: 'up',
      completion: 85
    },
    {
      id: '2', 
      name: 'Weekly Workouts',
      current: 4,
      target: 5,
      unit: 'sessions',
      category: 'fitness',
      trend: 'stable',
      completion: 80
    },
    {
      id: '3',
      name: 'Supplement Consistency',
      current: 88,
      target: 95,
      unit: '%',
      category: 'wellness',
      trend: 'up',
      completion: 93
    }
  ];

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <HealthAnalyticsHeader 
        timePeriod={timePeriod}
        onTimePeriodChange={setTimePeriod}
        selectedProfile={selectedProfile}
        onProfileChange={setSelectedProfile}
      />

      {/* Main Content */}
      <div className="flex-1 overflow-hidden">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full flex flex-col">
          <TabsList className="grid w-full grid-cols-4 mx-4 mt-2">
            <TabsTrigger value="overview" className="text-xs">Overview</TabsTrigger>
            <TabsTrigger value="nutrition" className="text-xs">Nutrition</TabsTrigger>
            <TabsTrigger value="fitness" className="text-xs">Fitness</TabsTrigger>
            <TabsTrigger value="goals" className="text-xs">Goals</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-hidden">
            <TabsContent value="overview" className="h-full m-0">
              <div className="h-full overflow-y-auto p-4 space-y-6">
                {/* Health Score Dashboard */}
                <HealthScoreDashboard 
                  metrics={healthMetrics}
                  timePeriod={timePeriod}
                />

                {/* AI Insights Panel */}
                <AIInsightsPanel 
                  metrics={healthMetrics}
                  nutritionData={nutritionData}
                  timePeriod={timePeriod}
                />
              </div>
            </TabsContent>

            <TabsContent value="nutrition" className="h-full m-0">
              <NutritionAnalytics 
                data={nutritionData}
                timePeriod={timePeriod}
              />
            </TabsContent>

            <TabsContent value="fitness" className="h-full m-0">
              <FitnessAnalytics 
                timePeriod={timePeriod}
              />
            </TabsContent>

            <TabsContent value="goals" className="h-full m-0">
              <GoalProgressTracking 
                goals={goals}
                timePeriod={timePeriod}
              />
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>
  );
}