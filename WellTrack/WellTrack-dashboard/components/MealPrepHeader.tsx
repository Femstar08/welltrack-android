import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Switch } from "./ui/switch";
import { 
  Clock, 
  Play, 
  Calendar,
  Timer,
  ChefHat
} from "lucide-react";
import { PrepSession } from "./MealPrepStorage";

interface MealPrepHeaderProps {
  activeSession: PrepSession | null;
  weekView: boolean;
  onWeekViewToggle: (enabled: boolean) => void;
}

export function MealPrepHeader({ 
  activeSession, 
  weekView, 
  onWeekViewToggle 
}: MealPrepHeaderProps) {
  const getSessionProgress = (session: PrepSession | null) => {
    if (!session) return 0;
    return Math.round((session.currentStep / session.totalSteps) * 100);
  };

  const getElapsedTime = (session: PrepSession | null) => {
    if (!session) return 0;
    return Math.floor((Date.now() - session.startTime.getTime()) / 1000 / 60);
  };

  return (
    <div className="bg-card border-b border-border px-4 py-3">
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-xl font-semibold text-card-foreground">Meal Prep & Storage</h1>
        
        <div className="flex items-center gap-3">
          {/* Week View Toggle */}
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Week View</span>
            <Switch 
              checked={weekView}
              onCheckedChange={onWeekViewToggle}
              className="scale-75"
            />
          </div>

          {/* Start New Prep Session */}
          {!activeSession && (
            <Button className="bg-green-500 hover:bg-green-600 text-white gap-2" size="sm">
              <Play className="w-4 h-4" />
              Start Prep
            </Button>
          )}
        </div>
      </div>

      {/* Active Session Indicator */}
      {activeSession && (
        <div className="bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg p-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                <ChefHat className="w-4 h-4 text-green-600" />
              </div>
              
              <div>
                <h3 className="font-medium text-green-800 dark:text-green-200">
                  {activeSession.recipeName}
                </h3>
                <div className="flex items-center gap-3 text-sm text-green-700 dark:text-green-300">
                  <span>Step {activeSession.currentStep} of {activeSession.totalSteps}</span>
                  <span>•</span>
                  <span>{getElapsedTime(activeSession)}min elapsed</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <Badge className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                {getSessionProgress(activeSession)}%
              </Badge>
              <div className="flex items-center gap-1 text-green-700 dark:text-green-300">
                <Timer className="w-4 h-4" />
                <span className="text-sm font-mono">
                  {Math.max(0, activeSession.estimatedDuration - getElapsedTime(activeSession))}min left
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Stats */}
      <div className="flex items-center justify-between mt-3">
        <div className="flex items-center gap-4">
          <div className="text-center">
            <p className="text-lg font-semibold text-card-foreground">8</p>
            <p className="text-xs text-muted-foreground">Prepped Meals</p>
          </div>
          <div className="h-8 w-px bg-border"></div>
          <div className="text-center">
            <p className="text-lg font-semibold text-card-foreground">75%</p>
            <p className="text-xs text-muted-foreground">Fridge Full</p>
          </div>
          <div className="h-8 w-px bg-border"></div>
          <div className="text-center">
            <p className="text-lg font-semibold text-card-foreground">3</p>
            <p className="text-xs text-muted-foreground">Expiring Soon</p>
          </div>
        </div>
      </div>
    </div>
  );
}