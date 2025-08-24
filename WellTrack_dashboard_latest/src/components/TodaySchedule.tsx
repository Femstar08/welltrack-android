import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { 
  Clock,
  CheckCircle,
  AlertTriangle,
  Timer,
  Coffee,
  Utensils,
  Moon,
  Dumbbell,
  Pill
} from "lucide-react";
import { Supplement, SupplementSchedule, SupplementIntake } from "./Supplements";

interface TodayScheduleProps {
  supplements: Supplement[];
  schedules: SupplementSchedule[];
  intakes: SupplementIntake[];
  currentTime: Date;
}

export function TodaySchedule({ 
  supplements, 
  schedules, 
  intakes, 
  currentTime 
}: TodayScheduleProps) {
  const [selectedTimeSlot, setSelectedTimeSlot] = useState<string | null>(null);

  // Time slots configuration
  const timeSlots = [
    { 
      id: 'morning', 
      label: 'Morning', 
      icon: Coffee, 
      time: '8:00 AM', 
      timeRange: '7:00-9:00 AM',
      color: 'text-orange-600 bg-orange-100 dark:bg-orange-950' 
    },
    { 
      id: 'preWorkout', 
      label: 'Pre-Workout', 
      icon: Dumbbell, 
      time: '4:00 PM', 
      timeRange: '3:00-5:00 PM',
      color: 'text-blue-600 bg-blue-100 dark:bg-blue-950' 
    },
    { 
      id: 'afternoon', 
      label: 'Afternoon', 
      icon: Utensils, 
      time: '12:30 PM', 
      timeRange: '12:00-2:00 PM',
      color: 'text-green-600 bg-green-100 dark:bg-green-950' 
    },
    { 
      id: 'evening', 
      label: 'Evening', 
      icon: Utensils, 
      time: '7:00 PM', 
      timeRange: '6:00-8:00 PM',
      color: 'text-purple-600 bg-purple-100 dark:bg-purple-950' 
    },
    { 
      id: 'bedtime', 
      label: 'Bedtime', 
      icon: Moon, 
      time: '10:00 PM', 
      timeRange: '9:30-11:00 PM',
      color: 'text-indigo-600 bg-indigo-100 dark:bg-indigo-950' 
    }
  ];

  // Get current time slot
  const getCurrentTimeSlot = () => {
    const hour = currentTime.getHours();
    if (hour >= 7 && hour < 11) return 'morning';
    if (hour >= 11 && hour < 14) return 'afternoon';
    if (hour >= 14 && hour < 17) return 'preWorkout';
    if (hour >= 17 && hour < 20) return 'evening';
    if (hour >= 20 || hour < 7) return 'bedtime';
    return 'morning';
  };

  const currentTimeSlot = getCurrentTimeSlot();

  // Get supplements for each time slot
  const getSupplementsForTimeSlot = (timeSlot: string) => {
    const relevantSchedules = schedules.filter(schedule => 
      schedule.active && schedule.timeSlots[timeSlot as keyof typeof schedule.timeSlots]
    );
    
    return relevantSchedules.map(schedule => {
      const supplement = supplements.find(s => s.id === schedule.supplementId);
      const intake = intakes.find(i => i.supplementId === schedule.supplementId);
      
      return {
        schedule,
        supplement,
        intake,
        isOverdue: !intake && timeSlot === 'morning' && currentTimeSlot !== 'morning',
        isDue: timeSlot === currentTimeSlot && !intake
      };
    }).filter(item => item.supplement);
  };

  // Get due now supplements
  const dueNowSupplements = getSupplementsForTimeSlot(currentTimeSlot).filter(item => 
    !item.intake || item.intake.status !== 'taken'
  );

  // Get overdue supplements
  const overdueSupplements = timeSlots.reduce((acc, slot) => {
    if (slot.id !== currentTimeSlot) {
      const supplements = getSupplementsForTimeSlot(slot.id).filter(item => 
        item.isOverdue || (item.intake && item.intake.status === 'missed')
      );
      return [...acc, ...supplements];
    }
    return acc;
  }, [] as any[]);

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'taken': return 'text-green-600 bg-green-100 dark:bg-green-950';
      case 'missed': return 'text-red-600 bg-red-100 dark:bg-red-950';
      case 'skipped': return 'text-gray-600 bg-gray-100 dark:bg-gray-950';
      default: return 'text-blue-600 bg-blue-100 dark:bg-blue-950';
    }
  };

  const handleTakeSupplement = (supplementId: string) => {
    // This would integrate with the state management system
    console.log('Taking supplement:', supplementId);
  };

  const handleSnoozeSupplement = (supplementId: string, minutes: number) => {
    console.log('Snoozing supplement:', supplementId, 'for', minutes, 'minutes');
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Current Time Indicator */}
      <Card className="bg-gradient-to-r from-blue-50 to-purple-50 dark:from-blue-950 dark:to-purple-950 border-blue-200 dark:border-blue-800">
        <CardContent className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Clock className="w-5 h-5 text-blue-600" />
              <div>
                <p className="font-medium text-card-foreground">
                  Current Time: {currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </p>
                <p className="text-sm text-muted-foreground">
                  {timeSlots.find(slot => slot.id === currentTimeSlot)?.label} period
                </p>
              </div>
            </div>
            <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200">
              {dueNowSupplements.length} due now
            </Badge>
          </div>
        </CardContent>
      </Card>

      {/* Due Now / Overdue Section */}
      {(dueNowSupplements.length > 0 || overdueSupplements.length > 0) && (
        <div className="space-y-3">
          <h3 className="font-medium text-card-foreground">Action Required</h3>
          
          {dueNowSupplements.map((item) => (
            <Card key={`due-${item.supplement?.id}`} className="border-blue-200 dark:border-blue-800">
              <CardContent className="p-4">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div className="w-3 h-3 bg-blue-500 rounded-full animate-pulse"></div>
                    <div>
                      <p className="font-medium text-card-foreground">
                        {item.supplement?.name}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {item.supplement?.dosage}{item.supplement?.unit} • {item.supplement?.brand}
                      </p>
                    </div>
                  </div>
                  <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200 text-xs">
                    Due Now
                  </Badge>
                </div>

                <div className="flex gap-2">
                  <Button 
                    size="sm" 
                    className="flex-1"
                    onClick={() => handleTakeSupplement(item.supplement!.id)}
                  >
                    <CheckCircle className="w-4 h-4 mr-1" />
                    Take Now
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => handleSnoozeSupplement(item.supplement!.id, 30)}
                  >
                    <Timer className="w-4 h-4" />
                  </Button>
                </div>

                {item.schedule.withFood && (
                  <p className="text-xs text-muted-foreground mt-2 flex items-center gap-1">
                    <Utensils className="w-3 h-3" />
                    Take with food
                  </p>
                )}
              </CardContent>
            </Card>
          ))}

          {overdueSupplements.map((item) => (
            <Card key={`overdue-${item.supplement?.id}`} className="border-red-200 dark:border-red-800">
              <CardContent className="p-4">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <AlertTriangle className="w-4 h-4 text-red-500" />
                    <div>
                      <p className="font-medium text-card-foreground">
                        {item.supplement?.name}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {item.supplement?.dosage}{item.supplement?.unit} • Missed
                      </p>
                    </div>
                  </div>
                  <Badge className="bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200 text-xs">
                    Overdue
                  </Badge>
                </div>

                <div className="flex gap-2">
                  <Button 
                    size="sm" 
                    variant="outline"
                    className="flex-1"
                    onClick={() => handleTakeSupplement(item.supplement!.id)}
                  >
                    <CheckCircle className="w-4 h-4 mr-1" />
                    Take Late
                  </Button>
                  <Button variant="outline" size="sm">
                    Skip
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Daily Timeline */}
      <div className="space-y-4">
        <h3 className="font-medium text-card-foreground">Daily Schedule</h3>
        
        {timeSlots.map((timeSlot) => {
          const supplements = getSupplementsForTimeSlot(timeSlot.id);
          const completed = supplements.filter(item => item.intake?.status === 'taken').length;
          const total = supplements.length;
          const completionRate = total > 0 ? (completed / total) * 100 : 0;
          
          if (supplements.length === 0) return null;

          return (
            <Card 
              key={timeSlot.id}
              className={`${timeSlot.id === currentTimeSlot ? 'ring-2 ring-blue-200 dark:ring-blue-800' : ''}`}
            >
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-lg ${timeSlot.color}`}>
                      <timeSlot.icon className="w-4 h-4" />
                    </div>
                    <div>
                      <h4 className="font-medium text-card-foreground">
                        {timeSlot.label}
                      </h4>
                      <p className="text-sm text-muted-foreground">
                        {timeSlot.timeRange}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-card-foreground">
                      {completed}/{total}
                    </p>
                    <div className="w-16 bg-muted rounded-full h-1.5 mt-1">
                      <div 
                        className="h-1.5 bg-green-500 rounded-full transition-all"
                        style={{ width: `${completionRate}%` }}
                      />
                    </div>
                  </div>
                </div>
              </CardHeader>
              
              <CardContent className="space-y-3">
                {supplements.map((item) => (
                  <div 
                    key={item.supplement?.id} 
                    className="flex items-center justify-between p-3 bg-muted/30 rounded-lg"
                  >
                    <div className="flex items-center gap-3">
                      <Pill className="w-4 h-4 text-purple-500" />
                      <div>
                        <p className="font-medium text-card-foreground">
                          {item.supplement?.name}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {item.supplement?.dosage}{item.supplement?.unit} • {item.supplement?.form}
                        </p>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-2">
                      {item.intake ? (
                        <Badge className={`text-xs px-2 py-1 ${getStatusColor(item.intake.status)}`}>
                          {item.intake.status}
                          {item.intake.actualTime && (
                            <span className="ml-1">
                              {item.intake.actualTime}
                            </span>
                          )}
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="text-xs">
                          Pending
                        </Badge>
                      )}
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Quick Actions</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button variant="outline" className="w-full justify-start gap-2">
            <CheckCircle className="w-4 h-4" />
            Mark all {currentTimeSlot} supplements taken
          </Button>
          <Button variant="outline" className="w-full justify-start gap-2">
            <Clock className="w-4 h-4" />
            Set custom reminder times
          </Button>
          <Button variant="outline" className="w-full justify-start gap-2">
            <Pill className="w-4 h-4" />
            Add missed supplement entry
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}