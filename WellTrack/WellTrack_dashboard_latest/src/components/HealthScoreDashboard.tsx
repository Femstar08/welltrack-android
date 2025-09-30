import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Progress } from "./ui/progress";
import { Badge } from "./ui/badge";
import { 
  TrendingUp, 
  TrendingDown,
  Apple,
  Dumbbell,
  Pill,
  Moon,
  Target
} from "lucide-react";
import { HealthMetrics, TimePeriod } from "./HealthAnalytics";

interface HealthScoreDashboardProps {
  metrics: HealthMetrics;
  timePeriod: TimePeriod;
}

export function HealthScoreDashboard({ metrics, timePeriod }: HealthScoreDashboardProps) {
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
    if (score >= 65) return 'B-';
    if (score >= 60) return 'C+';
    if (score >= 55) return 'C';
    return 'D';
  };

  const getProgressColor = (score: number) => {
    if (score >= 85) return 'bg-green-500';
    if (score >= 70) return 'bg-blue-500';
    if (score >= 55) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  const metricCards = [
    {
      id: 'nutrition',
      title: 'Nutrition',
      score: metrics.nutritionScore,
      icon: Apple,
      color: 'text-green-600',
      bgColor: 'bg-green-100 dark:bg-green-950',
      description: 'Macro & micro balance'
    },
    {
      id: 'fitness',
      title: 'Fitness',
      score: metrics.fitnessScore,
      icon: Dumbbell,
      color: 'text-blue-600',
      bgColor: 'bg-blue-100 dark:bg-blue-950',
      description: 'Activity & recovery'
    },
    {
      id: 'supplements',
      title: 'Supplements',
      score: metrics.supplementScore,
      icon: Pill,
      color: 'text-purple-600',
      bgColor: 'bg-purple-100 dark:bg-purple-950',
      description: 'Adherence & timing'
    },
    {
      id: 'sleep',
      title: 'Sleep',
      score: metrics.sleepScore || 0,
      icon: Moon,
      color: 'text-indigo-600',
      bgColor: 'bg-indigo-100 dark:bg-indigo-950',
      description: 'Quality & duration'
    }
  ];

  const contributingFactors = [
    { factor: 'Nutrition Consistency', impact: 25, trend: 'up' },
    { factor: 'Supplement Adherence', impact: 20, trend: 'up' },
    { factor: 'Fitness Routine', impact: 18, trend: 'stable' },
    { factor: 'Sleep Quality', impact: 15, trend: 'down' },
    { factor: 'Stress Management', impact: 12, trend: 'up' },
    { factor: 'Hydration', impact: 10, trend: 'stable' }
  ];

  return (
    <div className="space-y-6">
      {/* Overall Wellness Score */}
      <Card className="bg-gradient-to-br from-blue-50 to-green-50 dark:from-blue-950 dark:to-green-950 border-blue-200 dark:border-blue-800">
        <CardHeader className="pb-4">
          <CardTitle className="flex items-center justify-between">
            <span className="text-card-foreground">Overall Wellness Score</span>
            <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
              Grade {getScoreGrade(metrics.overallScore)}
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Large Score Display */}
          <div className="flex items-center justify-center">
            <div className="relative">
              <div className="w-32 h-32 rounded-full border-8 border-muted flex items-center justify-center">
                <div className="text-center">
                  <p className={`text-3xl font-bold ${getScoreColor(metrics.overallScore)}`}>
                    {metrics.overallScore}
                  </p>
                  <p className="text-sm text-muted-foreground">out of 100</p>
                </div>
              </div>
              {/* Progress Ring */}
              <div className="absolute inset-0">
                <svg className="w-32 h-32 transform -rotate-90" viewBox="0 0 100 100">
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    stroke="currentColor"
                    strokeWidth="8"
                    fill="none"
                    className="text-muted"
                  />
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    stroke="currentColor"
                    strokeWidth="8"
                    fill="none"
                    strokeDasharray={`${metrics.overallScore * 2.51} 251`}
                    className={getScoreColor(metrics.overallScore)}
                  />
                </svg>
              </div>
            </div>
          </div>

          {/* Week-over-Week Change */}
          <div className="flex items-center justify-center gap-2">
            {metrics.weekOverWeekChange > 0 ? (
              <TrendingUp className="w-4 h-4 text-green-600" />
            ) : (
              <TrendingDown className="w-4 h-4 text-red-600" />
            )}
            <span className={`font-medium ${
              metrics.weekOverWeekChange > 0 ? 'text-green-600' : 'text-red-600'
            }`}>
              {metrics.weekOverWeekChange > 0 ? '+' : ''}{metrics.weekOverWeekChange}% vs last week
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Key Metrics Summary */}
      <div className="grid grid-cols-2 gap-3">
        {metricCards.map((metric) => (
          <Card key={metric.id} className="hover:bg-accent/50 transition-colors">
            <CardContent className="p-4">
              <div className="space-y-3">
                {/* Header */}
                <div className="flex items-center justify-between">
                  <div className={`p-2 rounded-lg ${metric.bgColor}`}>
                    <metric.icon className={`w-4 h-4 ${metric.color}`} />
                  </div>
                  <Badge variant="secondary" className="text-xs">
                    {getScoreGrade(metric.score)}
                  </Badge>
                </div>

                {/* Score */}
                <div>
                  <p className={`text-xl font-semibold ${getScoreColor(metric.score)}`}>
                    {metric.score}
                  </p>
                  <p className="text-sm text-card-foreground">{metric.title}</p>
                  <p className="text-xs text-muted-foreground">{metric.description}</p>
                </div>

                {/* Progress Bar */}
                <Progress value={metric.score} className="h-2" />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Contributing Factors */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Target className="w-4 h-4" />
            Contributing Factors
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {contributingFactors.map((factor, index) => (
            <div key={index} className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-1">
                  {factor.trend === 'up' ? (
                    <TrendingUp className="w-3 h-3 text-green-500" />
                  ) : factor.trend === 'down' ? (
                    <TrendingDown className="w-3 h-3 text-red-500" />
                  ) : (
                    <div className="w-3 h-3 bg-yellow-500 rounded-full" />
                  )}
                </div>
                <span className="text-sm text-card-foreground">{factor.factor}</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-16 bg-muted rounded-full h-2">
                  <div 
                    className="h-2 bg-blue-500 rounded-full" 
                    style={{ width: `${factor.impact}%` }}
                  />
                </div>
                <span className="text-xs text-muted-foreground w-8 text-right">
                  {factor.impact}%
                </span>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}