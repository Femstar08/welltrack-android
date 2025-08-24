import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { TrendingUp, Bell, Target, ChevronDown, ChevronUp } from "lucide-react";
import { useState } from "react";

export function HealthInsights() {
  const [trendOpen, setTrendOpen] = useState(false);
  const [remindersOpen, setRemindersOpen] = useState(true);
  const [fitnessOpen, setFitnessOpen] = useState(true);
  
  const weeklyData = [
    { day: "Mon", calories: 1800 },
    { day: "Tue", calories: 2100 },
    { day: "Wed", calories: 1950 },
    { day: "Thu", calories: 2200 },
    { day: "Fri", calories: 1750 },
    { day: "Sat", calories: 2300 },
    { day: "Sun", calories: 1900 }
  ];

  const maxCalories = Math.max(...weeklyData.map(d => d.calories));
  const target = 2000;

  const upcomingReminders = [
    { time: "2:30 PM", task: "Afternoon snack" },
    { time: "6:00 PM", task: "Take vitamin D" },
    { time: "7:30 PM", task: "Log dinner" }
  ];

  return (
    <div className="space-y-4">
      {/* Weekly Trend Chart */}
      <Card className="bg-card shadow-sm border border-border">
        <Collapsible open={trendOpen} onOpenChange={setTrendOpen}>
          <CollapsibleTrigger className="w-full">
            <CardHeader className="pb-3 px-4 py-4">
              <CardTitle className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <TrendingUp className="w-6 h-6 text-green-600" />
                  <span className="text-lg text-card-foreground">Weekly Trend</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Badge variant="secondary" className="bg-green-100 text-green-700 px-3 py-1 dark:bg-green-900 dark:text-green-300">
                    Avg: 1,971 cal
                  </Badge>
                  {trendOpen ? (
                    <ChevronUp className="w-5 h-5 text-muted-foreground" />
                  ) : (
                    <ChevronDown className="w-5 h-5 text-muted-foreground" />
                  )}
                </div>
              </CardTitle>
            </CardHeader>
          </CollapsibleTrigger>
          
          <CollapsibleContent>
            <CardContent className="px-4 pb-4">
              <div className="flex items-end justify-between space-x-2 h-32">
                {weeklyData.map((day, index) => (
                  <div key={index} className="flex flex-col items-center space-y-2 flex-1">
                    <div className="flex flex-col items-center justify-end h-24">
                      <div
                        className={`w-6 rounded-t ${
                          day.calories >= target ? 'bg-green-500' : 'bg-orange-400'
                        }`}
                        style={{
                          height: `${(day.calories / maxCalories) * 80}px`
                        }}
                      />
                    </div>
                    <span className="text-xs text-muted-foreground">{day.day}</span>
                  </div>
                ))}
              </div>
              <div className="mt-4 flex items-center justify-center space-x-4 text-xs text-muted-foreground">
                <div className="flex items-center space-x-1">
                  <div className="w-3 h-3 bg-green-500 rounded" />
                  <span>Target met</span>
                </div>
                <div className="flex items-center space-x-1">
                  <div className="w-3 h-3 bg-orange-400 rounded" />
                  <span>Below target</span>
                </div>
              </div>
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>

      {/* Upcoming Reminders */}
      <Card className="bg-card shadow-sm border border-border">
        <Collapsible open={remindersOpen} onOpenChange={setRemindersOpen}>
          <CollapsibleTrigger className="w-full">
            <CardHeader className="pb-3 px-4 py-4">
              <CardTitle className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <Bell className="w-6 h-6 text-blue-600" />
                  <span className="text-lg text-card-foreground">Upcoming</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Badge variant="secondary" className="bg-blue-100 text-blue-700 px-3 py-1 dark:bg-blue-900 dark:text-blue-300">
                    {upcomingReminders.length}
                  </Badge>
                  {remindersOpen ? (
                    <ChevronUp className="w-5 h-5 text-muted-foreground" />
                  ) : (
                    <ChevronDown className="w-5 h-5 text-muted-foreground" />
                  )}
                </div>
              </CardTitle>
            </CardHeader>
          </CollapsibleTrigger>
          
          <CollapsibleContent>
            <CardContent className="px-4 pb-4 space-y-4">
              {upcomingReminders.map((reminder, index) => (
                <div key={index} className="flex items-center justify-between py-3 px-2 rounded-lg hover:bg-accent transition-colors">
                  <div className="flex items-center space-x-4">
                    <div className="w-3 h-3 bg-blue-500 rounded-full flex-shrink-0" />
                    <span className="text-base text-card-foreground">{reminder.task}</span>
                  </div>
                  <span className="text-sm text-muted-foreground font-medium">{reminder.time}</span>
                </div>
              ))}
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>

      {/* Fitness Integration */}
      <Card className="bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-sm">
        <Collapsible open={fitnessOpen} onOpenChange={setFitnessOpen}>
          <CollapsibleTrigger className="w-full">
            <CardHeader className="pb-3 px-4 py-4">
              <CardTitle className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <Target className="w-6 h-6" />
                  <span className="text-lg">Fitness</span>
                </div>
                {fitnessOpen ? (
                  <ChevronUp className="w-5 h-5 text-white/70" />
                ) : (
                  <ChevronDown className="w-5 h-5 text-white/70" />
                )}
              </CardTitle>
            </CardHeader>
          </CollapsibleTrigger>
          
          <CollapsibleContent>
            <CardContent className="px-4 pb-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div>
                    <p className="text-sm opacity-90">Today's Steps</p>
                    <p className="text-xl">8,247 / 10,000</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-sm opacity-90">Active Minutes</p>
                  <p className="text-xl">42 min</p>
                </div>
              </div>
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>
    </div>
  );
}