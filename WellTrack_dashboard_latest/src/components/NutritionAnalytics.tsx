import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar } from 'recharts';
import { 
  TrendingUp, 
  TrendingDown,
  Apple,
  Zap,
  Droplet,
  Leaf
} from "lucide-react";
import { NutritionData, TimePeriod } from "./HealthAnalytics";

interface NutritionAnalyticsProps {
  data: NutritionData[];
  timePeriod: TimePeriod;
}

export function NutritionAnalytics({ data, timePeriod }: NutritionAnalyticsProps) {
  // Mock micronutrient data
  const micronutrientData = [
    { nutrient: 'Vitamin D', current: 85, target: 100, unit: 'IU' },
    { nutrient: 'Vitamin B12', current: 95, target: 100, unit: 'mcg' },
    { nutrient: 'Iron', current: 70, target: 100, unit: 'mg' },
    { nutrient: 'Calcium', current: 90, target: 100, unit: 'mg' },
    { nutrient: 'Magnesium', current: 65, target: 100, unit: 'mg' },
    { nutrient: 'Zinc', current: 80, target: 100, unit: 'mg' },
    { nutrient: 'Omega-3', current: 75, target: 100, unit: 'g' },
    { nutrient: 'Folate', current: 88, target: 100, unit: 'mcg' }
  ];

  // Prepare chart data
  const macroTrendData = data.map((day, index) => ({
    date: new Date(day.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    protein: day.protein,
    carbs: day.carbs,
    fats: day.fats,
    calories: day.calories,
    score: day.score
  }));

  // Macro distribution for current period
  const avgMacros = {
    protein: Math.round(data.reduce((sum, day) => sum + day.protein, 0) / data.length),
    carbs: Math.round(data.reduce((sum, day) => sum + day.carbs, 0) / data.length),
    fats: Math.round(data.reduce((sum, day) => sum + day.fats, 0) / data.length)
  };

  const macroDistributionData = [
    { name: 'Protein', value: avgMacros.protein, color: '#10b981', target: 100 },
    { name: 'Carbs', value: avgMacros.carbs, color: '#3b82f6', target: 55 },
    { name: 'Fats', value: avgMacros.fats, color: '#f59e0b', target: 35 }
  ];

  // Meal timing analysis (mock data)
  const mealTimingData = [
    { meal: 'Breakfast', avgTime: '7:30 AM', calories: 450, score: 85 },
    { meal: 'Lunch', avgTime: '12:45 PM', calories: 520, score: 78 },
    { meal: 'Snack', avgTime: '3:15 PM', calories: 180, score: 82 },
    { meal: 'Dinner', avgTime: '7:00 PM', calories: 650, score: 80 }
  ];

  const getScoreColor = (score: number) => {
    if (score >= 85) return 'text-green-600';
    if (score >= 70) return 'text-blue-600';
    if (score >= 55) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getScoreGrade = (score: number) => {
    if (score >= 90) return 'A+';
    if (score >= 85) return 'A';
    if (score >= 80) return 'A-';
    if (score >= 75) return 'B+';
    if (score >= 70) return 'B';
    return 'C';
  };

  const getDeficiencyRisk = (current: number, target: number) => {
    const percentage = (current / target) * 100;
    if (percentage >= 90) return { level: 'Low', color: 'text-green-600', bg: 'bg-green-100 dark:bg-green-950' };
    if (percentage >= 70) return { level: 'Medium', color: 'text-yellow-600', bg: 'bg-yellow-100 dark:bg-yellow-950' };
    return { level: 'High', color: 'text-red-600', bg: 'bg-red-100 dark:bg-red-950' };
  };

  // Calculate trends
  const proteinTrend = data.length > 1 ? 
    ((data[data.length - 1].protein - data[0].protein) / data[0].protein * 100) : 0;
  const scoreTrend = data.length > 1 ? 
    ((data[data.length - 1].score - data[0].score) / data[0].score * 100) : 0;

  return (
    <div className="h-full overflow-y-auto p-4">
      <Tabs defaultValue="macros" className="space-y-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="macros">Macros</TabsTrigger>
          <TabsTrigger value="micros">Micros</TabsTrigger>
          <TabsTrigger value="timing">Timing</TabsTrigger>
        </TabsList>

        <TabsContent value="macros" className="space-y-4">
          {/* Macro Summary Cards */}
          <div className="grid grid-cols-3 gap-3">
            <Card>
              <CardContent className="p-3 text-center">
                <div className="flex items-center justify-center gap-1 mb-1">
                  <Apple className="w-4 h-4 text-green-600" />
                  <p className="text-lg font-semibold text-green-600">{avgMacros.protein}g</p>
                </div>
                <p className="text-xs text-muted-foreground">Avg Protein</p>
                <div className="flex items-center justify-center gap-1 mt-1">
                  {proteinTrend > 0 ? (
                    <TrendingUp className="w-3 h-3 text-green-500" />
                  ) : (
                    <TrendingDown className="w-3 h-3 text-red-500" />
                  )}
                  <span className={`text-xs ${proteinTrend > 0 ? 'text-green-500' : 'text-red-500'}`}>
                    {Math.abs(proteinTrend).toFixed(1)}%
                  </span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-3 text-center">
                <div className="flex items-center justify-center gap-1 mb-1">
                  <Zap className="w-4 h-4 text-blue-600" />
                  <p className="text-lg font-semibold text-blue-600">{avgMacros.carbs}g</p>
                </div>
                <p className="text-xs text-muted-foreground">Avg Carbs</p>
                <Badge variant="secondary" className="text-xs mt-1">
                  Target Range
                </Badge>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-3 text-center">
                <div className="flex items-center justify-center gap-1 mb-1">
                  <Droplet className="w-4 h-4 text-yellow-600" />
                  <p className="text-lg font-semibold text-yellow-600">{avgMacros.fats}g</p>
                </div>
                <p className="text-xs text-muted-foreground">Avg Fats</p>
                <Badge className="bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200 text-xs mt-1">
                  Optimal
                </Badge>
              </CardContent>
            </Card>
          </div>

          {/* Macro Trends Chart */}
          <Card>
            <CardHeader>
              <CardTitle>Macro Trends ({timePeriod})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={macroTrendData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="protein" stroke="#10b981" strokeWidth={2} />
                    <Line type="monotone" dataKey="carbs" stroke="#3b82f6" strokeWidth={2} />
                    <Line type="monotone" dataKey="fats" stroke="#f59e0b" strokeWidth={2} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>

          {/* Macro Distribution */}
          <Card>
            <CardHeader>
              <CardTitle>Average Macro Distribution</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-48">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={macroDistributionData} layout="horizontal">
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" />
                    <YAxis dataKey="name" type="category" />
                    <Tooltip />
                    <Bar dataKey="value" fill="#8884d8" />
                    <Bar dataKey="target" fill="#82ca9d" opacity={0.6} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
              <div className="flex justify-center gap-4 mt-4">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-blue-500 rounded"></div>
                  <span className="text-xs text-muted-foreground">Current</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-green-500 rounded opacity-60"></div>
                  <span className="text-xs text-muted-foreground">Target</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Nutrition Score Trend */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span>Nutrition Score Trend</span>
                <div className="flex items-center gap-2">
                  <Badge className={`${getScoreColor(data[data.length - 1]?.score || 0)} bg-transparent border`}>
                    {getScoreGrade(data[data.length - 1]?.score || 0)}
                  </Badge>
                  <div className="flex items-center gap-1">
                    {scoreTrend > 0 ? (
                      <TrendingUp className="w-4 h-4 text-green-500" />
                    ) : (
                      <TrendingDown className="w-4 h-4 text-red-500" />
                    )}
                    <span className={`text-sm ${scoreTrend > 0 ? 'text-green-500' : 'text-red-500'}`}>
                      {Math.abs(scoreTrend).toFixed(1)}%
                    </span>
                  </div>
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-48">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={macroTrendData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis domain={[0, 100]} />
                    <Tooltip />
                    <Line 
                      type="monotone" 
                      dataKey="score" 
                      stroke="#8b5cf6" 
                      strokeWidth={3}
                      dot={{ fill: '#8b5cf6', strokeWidth: 2, r: 4 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="micros" className="space-y-4">
          {/* Micronutrient Radar Chart */}
          <Card>
            <CardHeader>
              <CardTitle>Micronutrient Profile</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <RadarChart data={micronutrientData}>
                    <PolarGrid />
                    <PolarAngleAxis dataKey="nutrient" />
                    <PolarRadiusAxis angle={90} domain={[0, 100]} />
                    <Radar
                      name="Current"
                      dataKey="current"
                      stroke="#8b5cf6"
                      fill="#8b5cf6"
                      fillOpacity={0.3}
                    />
                    <Radar
                      name="Target"
                      dataKey="target"
                      stroke="#10b981"
                      fill="transparent"
                      strokeDasharray="5 5"
                    />
                  </RadarChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>

          {/* Micronutrient Details */}
          <div className="grid grid-cols-1 gap-3">
            {micronutrientData.map((nutrient) => {
              const risk = getDeficiencyRisk(nutrient.current, nutrient.target);
              const percentage = (nutrient.current / nutrient.target) * 100;
              
              return (
                <Card key={nutrient.nutrient}>
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <Leaf className="w-4 h-4 text-green-600" />
                        <span className="font-medium">{nutrient.nutrient}</span>
                      </div>
                      <Badge className={`text-xs px-2 py-1 ${risk.bg} ${risk.color}`}>
                        {risk.level} Risk
                      </Badge>
                    </div>
                    
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">
                          {nutrient.current}{nutrient.unit} / {nutrient.target}{nutrient.unit}
                        </span>
                        <span className={percentage >= 90 ? 'text-green-600' : percentage >= 70 ? 'text-yellow-600' : 'text-red-600'}>
                          {percentage.toFixed(0)}%
                        </span>
                      </div>
                      
                      <div className="w-full bg-muted rounded-full h-2">
                        <div 
                          className={`h-2 rounded-full ${
                            percentage >= 90 ? 'bg-green-500' : 
                            percentage >= 70 ? 'bg-yellow-500' : 'bg-red-500'
                          }`}
                          style={{ width: `${Math.min(percentage, 100)}%` }}
                        />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </TabsContent>

        <TabsContent value="timing" className="space-y-4">
          {/* Meal Timing Analysis */}
          <Card>
            <CardHeader>
              <CardTitle>Daily Meal Pattern</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {mealTimingData.map((meal) => (
                <div key={meal.meal} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
                  <div className="flex items-center gap-3">
                    <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                    <div>
                      <p className="font-medium text-card-foreground">{meal.meal}</p>
                      <p className="text-sm text-muted-foreground">{meal.avgTime}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-card-foreground">{meal.calories} cal</p>
                    <Badge className={`text-xs ${getScoreColor(meal.score)} bg-transparent border`}>
                      {getScoreGrade(meal.score)}
                    </Badge>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>

          {/* Eating Window Analysis */}
          <Card>
            <CardHeader>
              <CardTitle>Eating Window Analysis</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">First meal</span>
                  <span className="font-medium">7:30 AM</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">Last meal</span>
                  <span className="font-medium">7:00 PM</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">Eating window</span>
                  <span className="font-medium text-green-600">11.5 hours</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">Fasting period</span>
                  <span className="font-medium text-blue-600">12.5 hours</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}