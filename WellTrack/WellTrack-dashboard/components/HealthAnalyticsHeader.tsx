import { Button } from "./ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { Badge } from "./ui/badge";
import { 
  Download, 
  RefreshCw, 
  User,
  Calendar,
  TrendingUp
} from "lucide-react";
import { TimePeriod } from "./HealthAnalytics";

interface HealthAnalyticsHeaderProps {
  timePeriod: TimePeriod;
  onTimePeriodChange: (period: TimePeriod) => void;
  selectedProfile: string;
  onProfileChange: (profile: string) => void;
}

export function HealthAnalyticsHeader({ 
  timePeriod, 
  onTimePeriodChange, 
  selectedProfile, 
  onProfileChange 
}: HealthAnalyticsHeaderProps) {
  const timePeriodOptions = [
    { value: '7d', label: '7 Days', description: 'Last week' },
    { value: '30d', label: '30 Days', description: 'Last month' },
    { value: '3m', label: '3 Months', description: 'Last quarter' },
    { value: '1y', label: '1 Year', description: 'Annual view' }
  ];

  const profiles = [
    { id: 'main', name: 'Your Profile', role: 'Primary User' },
    { id: 'partner', name: 'Partner', role: 'Secondary User' },
    { id: 'family', name: 'Family View', role: 'Household' }
  ];

  const currentPeriod = timePeriodOptions.find(option => option.value === timePeriod);
  const currentProfile = profiles.find(profile => profile.id === selectedProfile);

  return (
    <div className="bg-card border-b border-border px-4 py-3">
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-xl font-semibold text-card-foreground">Health Insights</h1>
        
        <div className="flex items-center gap-2">
          {/* Refresh Button */}
          <Button variant="outline" size="sm" className="gap-1.5">
            <RefreshCw className="w-4 h-4" />
            Sync
          </Button>

          {/* Export Data */}
          <Button variant="outline" size="sm" className="gap-1.5">
            <Download className="w-4 h-4" />
            Export
          </Button>
        </div>
      </div>

      {/* Controls Row */}
      <div className="flex items-center justify-between gap-4">
        {/* Profile Selector */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="h-auto p-0 justify-start">
              <div className="flex items-center gap-2">
                <User className="w-4 h-4 text-blue-600" />
                <div className="text-left">
                  <span className="font-medium text-card-foreground">{currentProfile?.name}</span>
                  <p className="text-xs text-muted-foreground">{currentProfile?.role}</p>
                </div>
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start">
            {profiles.map((profile) => (
              <DropdownMenuItem
                key={profile.id}
                onClick={() => onProfileChange(profile.id)}
                className={`${selectedProfile === profile.id ? 'bg-accent' : ''}`}
              >
                <div className="flex flex-col">
                  <span className="font-medium">{profile.name}</span>
                  <span className="text-xs text-muted-foreground">{profile.role}</span>
                </div>
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>

        {/* Time Period Selector */}
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-muted-foreground" />
          <Select value={timePeriod} onValueChange={(value) => onTimePeriodChange(value as TimePeriod)}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {timePeriodOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  <div className="flex flex-col">
                    <span>{option.label}</span>
                    <span className="text-xs text-muted-foreground">{option.description}</span>
                  </div>
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="flex items-center justify-between mt-3">
        <div className="flex items-center gap-4">
          <div className="text-center">
            <p className="text-lg font-semibold text-card-foreground">78</p>
            <p className="text-xs text-muted-foreground">Health Score</p>
          </div>
          <div className="h-8 w-px bg-border"></div>
          <div className="text-center">
            <p className="text-lg font-semibold text-green-600">+3.2%</p>
            <p className="text-xs text-muted-foreground">vs Last Week</p>
          </div>
          <div className="h-8 w-px bg-border"></div>
          <div className="text-center">
            <p className="text-lg font-semibold text-card-foreground">A-</p>
            <p className="text-xs text-muted-foreground">Grade</p>
          </div>
        </div>

        <Badge className="bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200 gap-1">
          <TrendingUp className="w-3 h-3" />
          Trending Up
        </Badge>
      </div>
    </div>
  );
}