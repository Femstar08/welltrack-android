import { Card, CardContent } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { 
  Plus,
  Calendar,
  Bell,
  Settings,
  TrendingUp,
  TrendingDown,
  Activity,
  AlertCircle,
  CheckCircle
} from "lucide-react";

interface BiomarkerHeaderProps {
  healthScore: number;
  lastTestDate: string;
  nextTestDate: string;
  onAddResults: () => void;
}

export function BiomarkerHeader({ 
  healthScore, 
  lastTestDate, 
  nextTestDate, 
  onAddResults 
}: BiomarkerHeaderProps) {
  const getScoreColor = (score: number) => {
    if (score >= 85) return 'text-green-600';
    if (score >= 70) return 'text-blue-600';
    if (score >= 55) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getScoreGrade = (score: number) => {
    if (score >= 95) return 'A+';
    if (score >= 90) return 'A';
    if (score >= 85) return 'A-';
    if (score >= 80) return 'B+';
    if (score >= 75) return 'B';
    if (score >= 70) return 'B-';
    if (score >= 65) return 'C+';
    return 'C';
  };

  const getDaysUntilNext = (dateString: string) => {
    const nextDate = new Date(dateString);
    const today = new Date();
    const diffTime = nextDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const daysSinceLastTest = Math.floor((new Date().getTime() - new Date(lastTestDate).getTime()) / (1000 * 60 * 60 * 24));
  const daysUntilNext = getDaysUntilNext(nextTestDate);

  return (
    <div className="bg-card border-b border-border px-4 py-4 space-y-4">
      {/* Header Row */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-card-foreground">Biomarkers</h1>
          <p className="text-sm text-muted-foreground">Track your health metrics over time</p>
        </div>
        
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm">
            <Settings className="w-4 h-4" />
          </Button>
          <Button size="sm" onClick={onAddResults}>
            <Plus className="w-4 h-4 mr-1" />
            Add Results
          </Button>
        </div>
      </div>

      {/* Health Score and Test Info */}
      <div className="grid grid-cols-3 gap-4">
        {/* Health Score */}
        <Card className="bg-gradient-to-br from-blue-50 to-purple-50 dark:from-blue-950 dark:to-purple-950 border-blue-200 dark:border-blue-800">
          <CardContent className="p-4 text-center">
            <div className={`text-2xl font-semibold ${getScoreColor(healthScore)} mb-1`}>
              {healthScore}
            </div>
            <p className="text-xs text-muted-foreground mb-2">Biomarker Score</p>
            <Badge className={`text-xs ${getScoreColor(healthScore)} bg-transparent border`}>
              Grade {getScoreGrade(healthScore)}
            </Badge>
          </CardContent>
        </Card>

        {/* Last Test */}
        <Card>
          <CardContent className="p-4 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              <CheckCircle className="w-4 h-4 text-green-500" />
              <span className="font-semibold">{daysSinceLastTest}</span>
            </div>
            <p className="text-xs text-muted-foreground mb-1">Days since last test</p>
            <p className="text-xs text-muted-foreground">
              {new Date(lastTestDate).toLocaleDateString()}
            </p>
          </CardContent>
        </Card>

        {/* Next Test */}
        <Card>
          <CardContent className="p-4 text-center">
            <div className="flex items-center justify-center gap-1 mb-1">
              {daysUntilNext <= 7 ? (
                <AlertCircle className="w-4 h-4 text-orange-500" />
              ) : (
                <Calendar className="w-4 h-4 text-blue-500" />
              )}
              <span className="font-semibold">{daysUntilNext}</span>
            </div>
            <p className="text-xs text-muted-foreground mb-1">Days until next test</p>
            <p className="text-xs text-muted-foreground">
              {new Date(nextTestDate).toLocaleDateString()}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Quick Stats Overview */}
      <div className="bg-muted/30 rounded-lg p-4">
        <div className="flex items-center justify-between mb-3">
          <h3 className="font-medium text-card-foreground">Quick Overview</h3>
          <div className="flex items-center gap-4 text-sm">
            <div className="flex items-center gap-1">
              <TrendingUp className="w-4 h-4 text-green-500" />
              <span className="text-green-600">3 improving</span>
            </div>
            <div className="flex items-center gap-1">
              <Activity className="w-4 h-4 text-blue-500" />
              <span className="text-blue-600">4 stable</span>
            </div>
            <div className="flex items-center gap-1">
              <TrendingDown className="w-4 h-4 text-orange-500" />
              <span className="text-orange-600">1 declining</span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-4 text-sm">
          <div>
            <p className="text-muted-foreground">In Optimal Range</p>
            <div className="flex items-center gap-2 mt-1">
              <Progress value={62} className="flex-1 h-2" />
              <span className="font-medium">5/8</span>
            </div>
          </div>
          <div>
            <p className="text-muted-foreground">High Priority</p>
            <div className="flex items-center gap-2 mt-1">
              <div className="flex -space-x-1">
                <div className="w-4 h-4 rounded-full bg-red-500 border-2 border-background" />
                <div className="w-4 h-4 rounded-full bg-orange-500 border-2 border-background" />
                <div className="w-4 h-4 rounded-full bg-yellow-500 border-2 border-background" />
              </div>
              <span className="font-medium">3 markers</span>
            </div>
          </div>
          <div>
            <p className="text-muted-foreground">Next Reminder</p>
            <div className="flex items-center gap-1 mt-1">
              <Bell className="w-4 h-4 text-blue-500" />
              <span className="font-medium">In 5 days</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}