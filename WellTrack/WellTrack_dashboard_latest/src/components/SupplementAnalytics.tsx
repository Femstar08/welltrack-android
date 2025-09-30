import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar } from 'recharts';
import { 
  TrendingUp,
  TrendingDown,
  Target,
  Calendar,
  Pill,
  Star,
  Zap,
  CheckCircle,
  XCircle,
  Clock
} from "lucide-react";
import { Supplement, SupplementIntake, SupplementSchedule } from "./Supplements";

interface SupplementAnalyticsProps {
  supplements: Supplement[];
  intakes: SupplementIntake[];
  schedules: SupplementSchedule[];
}

export function SupplementAnalytics({ supplements, intakes, schedules }: SupplementAnalyticsProps) {
  // Mock weekly adherence data
  const weeklyAdherenceData = [
    { date: 'Mon', adherence: 95, taken: 19, scheduled: 20 },
    { date: 'Tue', adherence: 85, taken: 17, scheduled: 20 },
    { date: 'Wed', adherence: 100, taken: 20, scheduled: 20 },
    { date: 'Thu', adherence: 90, taken: 18, scheduled: 20 },
    { date: 'Fri', adherence: 75, taken: 15, scheduled: 20 },
    { date: 'Sat', adherence: 65, taken: 13, scheduled: 20 },
    { date: 'Sun', adherence: 80, taken: 16, scheduled: 20 }
  ];

  // Mock effectiveness correlation data
  const effectivenessData = supplements.map(supplement => ({
    name: supplement.name.split(' ')[0],
    effectiveness: supplement.effectiveness,
    adherence: Math.floor(Math.random() * 30) + 70, // 70-100%
    cost: supplement.cost,
    satisfaction: supplement.effectiveness
  }));

  // Calculate streaks and patterns
  const calculateStreaks = () => {
    return supplements.map(supplement => {
      const supplementIntakes = intakes.filter(intake => intake.supplementId === supplement.id);
      const takenCount = supplementIntakes.filter(intake => intake.status === 'taken').length;
      const missedCount = supplementIntakes.filter(intake => intake.status === 'missed').length;
      const totalScheduled = supplementIntakes.length;
      const adherenceRate = totalScheduled > 0 ? (takenCount / totalScheduled) * 100 : 0;

      return {
        supplement,
        currentStreak: Math.floor(Math.random() * 15) + 1,
        bestStreak: Math.floor(Math.random() * 25) + 10,
        adherenceRate,
        totalTaken: takenCount,
        totalMissed: missedCount,
        lastMissed: Math.floor(Math.random() * 7) + 1
      };
    });
  };

  const streakData = calculateStreaks();

  // Calculate time-based patterns
  const timePatterns = [
    { time: 'Morning', success: 92, total: 35, bestDay: 'Wednesday' },
    { time: 'Pre-workout', success: 88, total: 20, bestDay: 'Monday' },
    { time: 'Evening', success: 78, total: 28, bestDay: 'Tuesday' },
    { time: 'Bedtime', success: 85, total: 25, bestDay: 'Sunday' }
  ];

  const getAdherenceColor = (rate: number) => {
    if (rate >= 90) return 'text-green-600';
    if (rate >= 70) return 'text-blue-600';
    if (rate >= 50) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getAdherenceGrade = (rate: number) => {
    if (rate >= 95) return 'A+';
    if (rate >= 90) return 'A';
    if (rate >= 85) return 'A-';
    if (rate >= 80) return 'B+';
    if (rate >= 75) return 'B';
    if (rate >= 70) return 'B-';
    if (rate >= 65) return 'C+';
    return 'C';
  };

  const weeklyAverage = weeklyAdherenceData.reduce((sum, day) => sum + day.adherence, 0) / weeklyAdherenceData.length;
  const totalTaken = weeklyAdherenceData.reduce((sum, day) => sum + day.taken, 0);
  const totalScheduled = weeklyAdherenceData.reduce((sum, day) => sum + day.scheduled, 0);

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Overview Cards */}
      <div className="grid grid-cols-2 gap-3">
        <Card>
          <CardContent className="p-4 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Target className="w-4 h-4 text-blue-600" />
              <p className={`text-lg font-semibold ${getAdherenceColor(weeklyAverage)}`}>
                {weeklyAverage.toFixed(0)}%
              </p>
            </div>
            <p className="text-sm text-muted-foreground">Weekly Average</p>
            <Badge className={`text-xs mt-1 ${getAdherenceColor(weeklyAverage)} bg-transparent border`}>
              Grade {getAdherenceGrade(weeklyAverage)}
            </Badge>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Zap className="w-4 h-4 text-green-600" />
              <p className="text-lg font-semibold text-green-600">
                {Math.max(...streakData.map(s => s.currentStreak))}
              </p>
            </div>
            <p className="text-sm text-muted-foreground">Current Best Streak</p>
            <p className="text-xs text-muted-foreground mt-1">
              {streakData.find(s => s.currentStreak === Math.max(...streakData.map(s => s.currentStreak)))?.supplement.name.split(' ')[0]}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <CheckCircle className="w-4 h-4 text-purple-600" />
              <p className="text-lg font-semibold text-purple-600">{totalTaken}</p>
            </div>
            <p className="text-sm text-muted-foreground">Total Taken</p>
            <p className="text-xs text-muted-foreground mt-1">This week</p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <Star className="w-4 h-4 text-yellow-600" />
              <p className="text-lg font-semibold text-yellow-600">
                {(effectivenessData.reduce((sum, item) => sum + item.effectiveness, 0) / effectivenessData.length).toFixed(1)}
              </p>
            </div>
            <p className="text-sm text-muted-foreground">Avg Effectiveness</p>
            <p className="text-xs text-muted-foreground mt-1">Out of 5 stars</p>
          </CardContent>
        </Card>
      </div>

      {/* Weekly Adherence Trend */}
      <Card>
        <CardHeader>
          <CardTitle>Weekly Adherence Trend</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={weeklyAdherenceData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis domain={[0, 100]} />
                <Tooltip />
                <Line 
                  type="monotone" 
                  dataKey="adherence" 
                  stroke="#8b5cf6" 
                  strokeWidth={3}
                  dot={{ fill: '#8b5cf6', strokeWidth: 2, r: 4 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Supplement Performance */}
      <Card>
        <CardHeader>
          <CardTitle>Individual Supplement Performance</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {streakData.map((item) => (
            <div key={item.supplement.id} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
              <div className="flex items-center gap-3">
                <Pill className="w-4 h-4 text-purple-500" />
                <div>
                  <h4 className="font-medium text-card-foreground">
                    {item.supplement.name}
                  </h4>
                  <div className="flex items-center gap-4 mt-1">
                    <span className="text-xs text-muted-foreground">
                      Current streak: {item.currentStreak} days
                    </span>
                    <span className="text-xs text-muted-foreground">
                      Best: {item.bestStreak} days
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="text-right">
                <p className={`text-lg font-semibold ${getAdherenceColor(item.adherenceRate)}`}>
                  {item.adherenceRate.toFixed(0)}%
                </p>
                <p className="text-xs text-muted-foreground">
                  {item.totalTaken} taken, {item.totalMissed} missed
                </p>
                <div className="w-20 bg-muted rounded-full h-1.5 mt-1">
                  <div 
                    className={`h-1.5 rounded-full ${
                      item.adherenceRate >= 90 ? 'bg-green-500' :
                      item.adherenceRate >= 70 ? 'bg-blue-500' :
                      item.adherenceRate >= 50 ? 'bg-yellow-500' : 'bg-red-500'
                    }`}
                    style={{ width: `${item.adherenceRate}%` }}
                  />
                </div>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Time-Based Patterns */}
      <Card>
        <CardHeader>
          <CardTitle>Time-Based Success Patterns</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {timePatterns.map((pattern) => (
            <div key={pattern.time} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
              <div className="flex items-center gap-3">
                <Clock className="w-4 h-4 text-blue-500" />
                <div>
                  <h4 className="font-medium text-card-foreground">{pattern.time}</h4>
                  <p className="text-xs text-muted-foreground">
                    Best day: {pattern.bestDay}
                  </p>
                </div>
              </div>
              
              <div className="text-right">
                <p className={`font-semibold ${getAdherenceColor(pattern.success)}`}>
                  {pattern.success}%
                </p>
                <p className="text-xs text-muted-foreground">
                  {Math.round(pattern.total * pattern.success / 100)} / {pattern.total} taken
                </p>
                <div className="w-16 bg-muted rounded-full h-1.5 mt-1">
                  <div 
                    className="h-1.5 bg-blue-500 rounded-full"
                    style={{ width: `${pattern.success}%` }}
                  />
                </div>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Effectiveness vs Adherence */}
      <Card>
        <CardHeader>
          <CardTitle>Effectiveness vs Adherence Correlation</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={effectivenessData}>
                <PolarGrid />
                <PolarAngleAxis dataKey="name" />
                <PolarRadiusAxis angle={90} domain={[0, 100]} />
                <Radar
                  name="Adherence"
                  dataKey="adherence"
                  stroke="#3b82f6"
                  fill="#3b82f6"
                  fillOpacity={0.2}
                />
                <Radar
                  name="Effectiveness"
                  dataKey="effectiveness"
                  stroke="#10b981"
                  fill="#10b981"
                  fillOpacity={0.2}
                  dataKey="satisfaction"
                />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Weekly Summary */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            Weekly Summary Insights
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">Most consistent supplement</span>
            <span className="font-medium text-green-600">
              {streakData.reduce((best, current) => 
                current.adherenceRate > best.adherenceRate ? current : best
              ).supplement.name}
            </span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">Needs attention</span>
            <span className="font-medium text-red-600">
              {streakData.reduce((worst, current) => 
                current.adherenceRate < worst.adherenceRate ? current : worst
              ).supplement.name}
            </span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">Best time slot</span>
            <span className="font-medium text-blue-600">
              {timePatterns.reduce((best, current) => 
                current.success > best.success ? current : best
              ).time}
            </span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">Overall improvement</span>
            <div className="flex items-center gap-1">
              <TrendingUp className="w-4 h-4 text-green-500" />
              <span className="font-medium text-green-600">+5.2% vs last week</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}