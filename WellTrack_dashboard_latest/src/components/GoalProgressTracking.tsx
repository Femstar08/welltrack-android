import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { Button } from "./ui/button";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, LineChart, Line } from 'recharts';
import { 
  Target,
  TrendingUp,
  TrendingDown,
  Trophy,
  Calendar,
  Zap,
  CheckCircle,
  Clock,
  Plus,
  Edit,
  Flag
} from "lucide-react";
import { GoalProgress, TimePeriod } from "./HealthAnalytics";

interface GoalProgressTrackingProps {
  goals: GoalProgress[];
  timePeriod: TimePeriod;
}

export function GoalProgressTracking({ goals, timePeriod }: GoalProgressTrackingProps) {
  // Mock historical goal data
  const goalHistoryData = [
    { date: '11/01', protein: 78, workouts: 1, supplements: 85, hydration: 70 },
    { date: '11/02', protein: 82, workouts: 1, supplements: 90, hydration: 85 },
    { date: '11/03', protein: 88, workouts: 2, supplements: 95, hydration: 90 },
    { date: '11/04', protein: 75, workouts: 2, supplements: 80, hydration: 75 },
    { date: '11/05', protein: 92, workouts: 3, supplements: 100, hydration: 95 },
    { date: '11/06', protein: 87, workouts: 3, supplements: 85, hydration: 80 },
    { date: '11/07', protein: 85, workouts: 4, supplements: 88, hydration: 85 }
  ];

  // Mock habit streaks
  const habitStreaks = [
    { habit: 'Daily Protein Goal', streak: 12, bestStreak: 18, lastMissed: '3 days ago' },
    { habit: 'Morning Supplements', streak: 8, bestStreak: 15, lastMissed: '1 week ago' },
    { habit: 'Workout Logging', streak: 6, bestStreak: 10, lastMissed: '2 days ago' },
    { habit: 'Meal Planning', streak: 15, bestStreak: 15, lastMissed: 'Never' }
  ];

  // Mock achievement milestones
  const achievements = [
    { id: '1', title: '30-Day Protein Streak', description: 'Hit protein goal for 30 consecutive days', progress: 85, target: 100, completed: false, category: 'nutrition' },
    { id: '2', title: 'Workout Warrior', description: 'Complete 20 workouts this month', progress: 75, target: 100, completed: false, category: 'fitness' },
    { id: '3', title: 'Supplement Consistency', description: 'Take supplements daily for 2 weeks', progress: 100, target: 100, completed: true, category: 'wellness' },
    { id: '4', title: 'Meal Prep Master', description: 'Prep meals 4 weeks in a row', progress: 100, target: 100, completed: true, category: 'nutrition' }
  ];

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'nutrition': return 'text-green-600 bg-green-100 dark:bg-green-950';
      case 'fitness': return 'text-blue-600 bg-blue-100 dark:bg-blue-950';
      case 'wellness': return 'text-purple-600 bg-purple-100 dark:bg-purple-950';
      default: return 'text-muted-foreground bg-muted';
    }
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'up': return <TrendingUp className="w-4 h-4 text-green-500" />;
      case 'down': return <TrendingDown className="w-4 h-4 text-red-500" />;
      default: return <div className="w-4 h-4 bg-yellow-500 rounded-full" />;
    }
  };

  const getCompletionColor = (completion: number) => {
    if (completion >= 90) return 'text-green-600';
    if (completion >= 70) return 'text-blue-600';
    if (completion >= 50) return 'text-yellow-600';
    return 'text-red-600';
  };

  const overallGoalCompletion = Math.round(goals.reduce((sum, goal) => sum + goal.completion, 0) / goals.length);
  const completedGoals = goals.filter(goal => goal.completion >= 100).length;

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Goal Overview */}
      <div className="grid grid-cols-3 gap-3">
        <Card>
          <CardContent className="p-3 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Target className="w-4 h-4 text-blue-600" />
              <p className="text-lg font-semibold text-blue-600">{overallGoalCompletion}%</p>
            </div>
            <p className="text-xs text-muted-foreground">Overall Progress</p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Trophy className="w-4 h-4 text-yellow-600" />
              <p className="text-lg font-semibold text-yellow-600">{completedGoals}</p>
            </div>
            <p className="text-xs text-muted-foreground">Goals Achieved</p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-3 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Zap className="w-4 h-4 text-green-600" />
              <p className="text-lg font-semibold text-green-600">{habitStreaks[0].streak}</p>
            </div>
            <p className="text-xs text-muted-foreground">Current Streak</p>
          </CardContent>
        </Card>
      </div>

      {/* Active Goals */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>Active Goals</CardTitle>
          <Button size="sm" className="gap-2">
            <Plus className="w-4 h-4" />
            Add Goal
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          {goals.map((goal) => (
            <div key={goal.id} className="p-4 bg-muted/30 rounded-lg">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-3">
                  <Target className="w-4 h-4 text-blue-500" />
                  <div>
                    <h4 className="font-medium text-card-foreground">{goal.name}</h4>
                    <div className="flex items-center gap-2 mt-1">
                      <Badge className={`text-xs px-2 py-1 ${getCategoryColor(goal.category)}`}>
                        {goal.category}
                      </Badge>
                      {getTrendIcon(goal.trend)}
                    </div>
                  </div>
                </div>
                <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                  <Edit className="w-4 h-4" />
                </Button>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">
                    {goal.current}{goal.unit} / {goal.target}{goal.unit}
                  </span>
                  <span className={`font-medium ${getCompletionColor(goal.completion)}`}>
                    {goal.completion}%
                  </span>
                </div>
                <Progress value={goal.completion} className="h-2" />
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Goal Progress Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Goal Progress Over Time</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={goalHistoryData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="protein" stroke="#10b981" strokeWidth={2} name="Protein %" />
                <Line type="monotone" dataKey="supplements" stroke="#8b5cf6" strokeWidth={2} name="Supplements %" />
                <Line type="monotone" dataKey="hydration" stroke="#3b82f6" strokeWidth={2} name="Hydration %" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Habit Consistency */}
      <Card>
        <CardHeader>
          <CardTitle>Habit Consistency</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {habitStreaks.map((habit, index) => (
            <div key={index} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
              <div className="flex items-center gap-3">
                <CheckCircle className="w-4 h-4 text-green-500" />
                <div>
                  <h4 className="font-medium text-card-foreground">{habit.habit}</h4>
                  <p className="text-sm text-muted-foreground">
                    Best streak: {habit.bestStreak} days
                  </p>
                </div>
              </div>
              <div className="text-right">
                <div className="flex items-center gap-1">
                  <Zap className="w-3 h-3 text-orange-500" />
                  <p className="font-medium text-card-foreground">{habit.streak} days</p>
                </div>
                <p className="text-xs text-muted-foreground">{habit.lastMissed}</p>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Achievement Milestones */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="w-4 h-4 text-yellow-500" />
            Achievement Milestones
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {achievements.map((achievement) => (
            <div key={achievement.id} className={`p-4 rounded-lg border ${
              achievement.completed 
                ? 'bg-green-50 dark:bg-green-950 border-green-200 dark:border-green-800' 
                : 'bg-muted/30 border-border'
            }`}>
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-start gap-3">
                  {achievement.completed ? (
                    <CheckCircle className="w-5 h-5 text-green-500" />
                  ) : (
                    <Flag className="w-5 h-5 text-muted-foreground" />
                  )}
                  <div>
                    <h4 className={`font-medium ${
                      achievement.completed ? 'text-green-800 dark:text-green-200' : 'text-card-foreground'
                    }`}>
                      {achievement.title}
                    </h4>
                    <p className="text-sm text-muted-foreground mt-1">
                      {achievement.description}
                    </p>
                  </div>
                </div>
                <Badge className={`text-xs px-2 py-1 ${getCategoryColor(achievement.category)}`}>
                  {achievement.category}
                </Badge>
              </div>

              {!achievement.completed && (
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Progress</span>
                    <span className="font-medium">{achievement.progress}%</span>
                  </div>
                  <Progress value={achievement.progress} className="h-2" />
                </div>
              )}
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Weekly Goal Summary */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            This Week's Summary
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Goals on track</span>
              <span className="font-medium text-green-600">
                {goals.filter(g => g.completion >= 70).length}/{goals.length}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Average completion</span>
              <span className="font-medium text-card-foreground">{overallGoalCompletion}%</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Longest streak</span>
              <span className="font-medium text-orange-600">
                {Math.max(...habitStreaks.map(h => h.streak))} days
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Achievements earned</span>
              <span className="font-medium text-yellow-600">
                {achievements.filter(a => a.completed).length}
              </span>
            </div>

            <div className="pt-3 border-t border-border">
              <Button className="w-full gap-2">
                <Target className="w-4 h-4" />
                View Detailed Analytics
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}