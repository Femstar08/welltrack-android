import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { 
  Plus, 
  Scan,
  Bell,
  Settings,
  Pill
} from "lucide-react";
import { Supplement } from "./Supplements";

interface SupplementsHeaderProps {
  adherencePercentage: number;
  takenToday: number;
  totalToday: number;
  supplements: Supplement[];
}

export function SupplementsHeader({ 
  adherencePercentage, 
  takenToday, 
  totalToday, 
  supplements 
}: SupplementsHeaderProps) {
  const getAdherenceColor = (percentage: number) => {
    if (percentage >= 90) return 'text-green-600';
    if (percentage >= 70) return 'text-blue-600';
    if (percentage >= 50) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getAdherenceStatus = (percentage: number) => {
    if (percentage >= 90) return 'Excellent';
    if (percentage >= 70) return 'Good';
    if (percentage >= 50) return 'Fair';
    return 'Needs Improvement';
  };

  const lowStockSupplements = supplements.filter(s => s.stockLevel / s.totalStock < 0.3).length;
  const expiringSoon = supplements.filter(s => {
    const expiryDate = new Date(s.expiryDate);
    const thirtyDaysFromNow = new Date();
    thirtyDaysFromNow.setDate(thirtyDaysFromNow.getDate() + 30);
    return expiryDate <= thirtyDaysFromNow;
  }).length;

  return (
    <div className="bg-card border-b border-border px-4 py-4">
      {/* Title and Actions Row */}
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-semibold text-card-foreground">Supplements</h1>
        
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" className="gap-1.5">
            <Scan className="w-4 h-4" />
            Scan
          </Button>
          <Button size="sm" className="gap-1.5 bg-purple-600 hover:bg-purple-700">
            <Plus className="w-4 h-4" />
            Add
          </Button>
        </div>
      </div>

      {/* Today's Progress */}
      <div className="space-y-4">
        {/* Adherence Circle and Stats */}
        <div className="flex items-center justify-between">
          {/* Circular Progress */}
          <div className="flex items-center gap-4">
            <div className="relative">
              <div className="w-16 h-16 rounded-full border-4 border-muted flex items-center justify-center">
                <div className="text-center">
                  <p className={`text-lg font-semibold ${getAdherenceColor(adherencePercentage)}`}>
                    {adherencePercentage}%
                  </p>
                </div>
              </div>
              {/* Progress Ring */}
              <div className="absolute inset-0">
                <svg className="w-16 h-16 transform -rotate-90" viewBox="0 0 100 100">
                  <circle
                    cx="50"
                    cy="50"
                    r="30"
                    stroke="currentColor"
                    strokeWidth="8"
                    fill="none"
                    className="text-muted"
                  />
                  <circle
                    cx="50"
                    cy="50"
                    r="30"
                    stroke="currentColor"
                    strokeWidth="8"
                    fill="none"
                    strokeDasharray={`${adherencePercentage * 1.88} 188`}
                    className={getAdherenceColor(adherencePercentage)}
                  />
                </svg>
              </div>
            </div>
            
            <div>
              <p className="font-medium text-card-foreground">Today's Adherence</p>
              <p className="text-sm text-muted-foreground">
                {takenToday} of {totalToday} supplements taken
              </p>
              <Badge className={`text-xs mt-1 ${getAdherenceColor(adherencePercentage)} bg-transparent border`}>
                {getAdherenceStatus(adherencePercentage)}
              </Badge>
            </div>
          </div>

          {/* Quick Stats */}
          <div className="text-right">
            <div className="flex items-center gap-2 justify-end mb-1">
              <Pill className="w-4 h-4 text-purple-500" />
              <span className="text-sm font-medium text-card-foreground">
                {supplements.length} Active
              </span>
            </div>
            {lowStockSupplements > 0 && (
              <div className="flex items-center gap-1 justify-end">
                <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                <span className="text-xs text-red-600">
                  {lowStockSupplements} low stock
                </span>
              </div>
            )}
            {expiringSoon > 0 && (
              <div className="flex items-center gap-1 justify-end">
                <div className="w-2 h-2 bg-yellow-500 rounded-full"></div>
                <span className="text-xs text-yellow-600">
                  {expiringSoon} expiring soon
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Progress Bar */}
        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Daily Progress</span>
            <span className="font-medium text-card-foreground">
              {takenToday}/{totalToday}
            </span>
          </div>
          <Progress 
            value={(takenToday / totalToday) * 100} 
            className="h-2" 
          />
        </div>

        {/* Quick Action Buttons */}
        <div className="flex gap-2">
          <Button 
            variant="outline" 
            size="sm" 
            className="flex-1 gap-1.5 text-xs"
          >
            <Bell className="w-3 h-3" />
            Set Reminders
          </Button>
          <Button 
            variant="outline" 
            size="sm" 
            className="flex-1 gap-1.5 text-xs"
          >
            <Pill className="w-3 h-3" />
            Mark All Taken
          </Button>
          <Button 
            variant="outline" 
            size="sm" 
            className="gap-1.5 text-xs px-3"
          >
            <Settings className="w-3 h-3" />
          </Button>
        </div>
      </div>
    </div>
  );
}