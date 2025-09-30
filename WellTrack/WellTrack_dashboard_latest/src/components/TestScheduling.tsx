import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Switch } from "./ui/switch";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { 
  Calendar,
  Bell,
  Clock,
  DollarSign,
  MapPin,
  Phone,
  Settings,
  Plus,
  AlertTriangle,
  CheckCircle,
  CalendarDays
} from "lucide-react";
import { TestPanel, Biomarker, BiomarkerReading } from "./Biomarkers";

interface TestSchedulingProps {
  testPanels: TestPanel[];
  biomarkers: Biomarker[];
  readings: BiomarkerReading[];
}

export function TestScheduling({ testPanels, biomarkers, readings }: TestSchedulingProps) {
  const [selectedPanel, setSelectedPanel] = useState<string>('');
  const [reminderSettings, setReminderSettings] = useState({
    enabled: true,
    daysBefore: 7,
    timeOfDay: '09:00'
  });

  // Calculate next test dates based on frequency
  const getNextTestDate = (panel: TestPanel) => {
    const lastTest = new Date('2024-10-15'); // Mock last test date
    const today = new Date();
    
    let intervalMonths = 0;
    switch (panel.frequency) {
      case 'monthly': intervalMonths = 1; break;
      case 'quarterly': intervalMonths = 3; break;
      case 'bi-annual': intervalMonths = 6; break;
      case 'annual': intervalMonths = 12; break;
    }
    
    const nextDate = new Date(lastTest);
    nextDate.setMonth(nextDate.getMonth() + intervalMonths);
    
    return nextDate;
  };

  const getDaysUntilTest = (date: Date) => {
    const today = new Date();
    const diffTime = date.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  };

  const getUrgencyColor = (days: number) => {
    if (days <= 7) return 'text-red-600 bg-red-50 border-red-200 dark:bg-red-950 dark:border-red-800';
    if (days <= 30) return 'text-orange-600 bg-orange-50 border-orange-200 dark:bg-orange-950 dark:border-orange-800';
    return 'text-blue-600 bg-blue-50 border-blue-200 dark:bg-blue-950 dark:border-blue-800';
  };

  const upcomingTests = testPanels.map(panel => ({
    ...panel,
    nextDate: getNextTestDate(panel),
    daysUntil: getDaysUntilTest(getNextTestDate(panel))
  })).sort((a, b) => a.daysUntil - b.daysUntil);

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Upcoming Tests */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between text-base">
            <div className="flex items-center gap-2">
              <Calendar className="w-5 h-5 text-blue-500" />
              Upcoming Tests
            </div>
            <Button size="sm">
              <Plus className="w-4 h-4 mr-1" />
              Schedule Test
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {upcomingTests.map((test) => (
            <Card key={test.id} className={`border ${getUrgencyColor(test.daysUntil)}`}>
              <CardContent className="p-4">
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <h3 className="font-medium text-card-foreground">{test.name}</h3>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                      <span className="flex items-center gap-1">
                        <CalendarDays className="w-3 h-3" />
                        {test.nextDate.toLocaleDateString()}
                      </span>
                      <span className="flex items-center gap-1">
                        <DollarSign className="w-3 h-3" />
                        ${test.cost}
                      </span>
                    </div>
                  </div>
                  
                  <div className="text-right">
                    <div className="font-semibold">
                      {test.daysUntil > 0 ? `${test.daysUntil} days` : 'Overdue'}
                    </div>
                    <p className="text-xs text-muted-foreground capitalize">{test.frequency}</p>
                  </div>
                </div>

                <p className="text-sm text-muted-foreground mb-3">{test.description}</p>

                <div className="flex items-center justify-between">
                  <div className="flex flex-wrap gap-1">
                    {test.biomarkerIds.slice(0, 3).map((biomarkerId) => {
                      const biomarker = biomarkers.find(b => b.id === biomarkerId);
                      return biomarker ? (
                        <Badge key={biomarkerId} variant="outline" className="text-xs">
                          {biomarker.name}
                        </Badge>
                      ) : null;
                    })}
                    {test.biomarkerIds.length > 3 && (
                      <Badge variant="outline" className="text-xs">
                        +{test.biomarkerIds.length - 3} more
                      </Badge>
                    )}
                  </div>
                  
                  <div className="flex items-center gap-2">
                    {test.fastingRequired && (
                      <Badge className="text-xs bg-yellow-100 text-yellow-800 dark:bg-yellow-950">
                        Fasting Required
                      </Badge>
                    )}
                    <Button variant="outline" size="sm">
                      <Phone className="w-3 h-3 mr-1" />
                      Book
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </CardContent>
      </Card>

      {/* Reminder Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Bell className="w-5 h-5 text-orange-500" />
            Reminder Settings
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">Test Reminders</p>
              <p className="text-sm text-muted-foreground">Get notified before scheduled tests</p>
            </div>
            <Switch 
              checked={reminderSettings.enabled}
              onCheckedChange={(checked) => 
                setReminderSettings(prev => ({ ...prev, enabled: checked }))
              }
            />
          </div>

          {reminderSettings.enabled && (
            <div className="bg-muted/30 rounded-lg p-4 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Remind me</label>
                  <Select 
                    value={reminderSettings.daysBefore.toString()}
                    onValueChange={(value) => 
                      setReminderSettings(prev => ({ ...prev, daysBefore: parseInt(value) }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="1">1 day before</SelectItem>
                      <SelectItem value="3">3 days before</SelectItem>
                      <SelectItem value="7">1 week before</SelectItem>
                      <SelectItem value="14">2 weeks before</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="space-y-2">
                  <label className="text-sm font-medium">Time of day</label>
                  <Select 
                    value={reminderSettings.timeOfDay}
                    onValueChange={(value) => 
                      setReminderSettings(prev => ({ ...prev, timeOfDay: value }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="09:00">9:00 AM</SelectItem>
                      <SelectItem value="12:00">12:00 PM</SelectItem>
                      <SelectItem value="15:00">3:00 PM</SelectItem>
                      <SelectItem value="18:00">6:00 PM</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="space-y-2">
                <p className="text-sm font-medium">Fasting Reminders</p>
                <div className="flex items-center space-x-2">
                  <Switch defaultChecked />
                  <span className="text-sm">Remind me to fast before tests that require it</span>
                </div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Test Optimization */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Settings className="w-5 h-5 text-purple-500" />
            Test Optimization
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="bg-purple-50 dark:bg-purple-950 border border-purple-200 dark:border-purple-800 rounded-lg p-4">
            <h3 className="font-medium mb-2">Recommended Test Strategy</h3>
            <p className="text-sm text-muted-foreground mb-3">
              Based on your current biomarker profile and goals, here's an optimized testing schedule:
            </p>
            
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span>High Priority Markers (Quarterly)</span>
                <Badge className="bg-red-100 text-red-800 dark:bg-red-950">3 markers</Badge>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span>Standard Monitoring (Bi-annual)</span>
                <Badge className="bg-yellow-100 text-yellow-800 dark:bg-yellow-950">4 markers</Badge>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span>Annual Comprehensive Panel</span>
                <Badge className="bg-green-100 text-green-800 dark:bg-green-950">8 markers</Badge>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="text-center p-3 bg-muted/30 rounded-lg">
              <div className="text-lg font-semibold text-green-600">$340</div>
              <p className="text-xs text-muted-foreground">Estimated annual cost</p>
            </div>
            <div className="text-center p-3 bg-muted/30 rounded-lg">
              <div className="text-lg font-semibold text-blue-600">4</div>
              <p className="text-xs text-muted-foreground">Tests per year</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Lab Integration */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <MapPin className="w-5 h-5 text-green-500" />
            Lab Integration
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 border border-border rounded-lg">
              <div className="flex items-center gap-3">
                <CheckCircle className="w-5 h-5 text-green-500" />
                <div>
                  <p className="font-medium">LabCorp</p>
                  <p className="text-sm text-muted-foreground">Connected • Auto-import enabled</p>
                </div>
              </div>
              <Button variant="outline" size="sm">
                <Settings className="w-4 h-4" />
              </Button>
            </div>
            
            <div className="flex items-center justify-between p-3 border border-border rounded-lg">
              <div className="flex items-center gap-3">
                <AlertTriangle className="w-5 h-5 text-orange-500" />
                <div>
                  <p className="font-medium">Quest Diagnostics</p>
                  <p className="text-sm text-muted-foreground">Not connected</p>
                </div>
              </div>
              <Button variant="outline" size="sm">
                Connect
              </Button>
            </div>
          </div>

          <div className="bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800 rounded-lg p-3">
            <p className="text-sm">
              <span className="font-medium">Pro Tip:</span> Connect your lab accounts to automatically 
              import results and get faster insights into your health trends.
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Preparation Reminders */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Clock className="w-5 h-5 text-blue-500" />
            Test Preparation
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="bg-yellow-50 dark:bg-yellow-950 border border-yellow-200 dark:border-yellow-800 rounded-lg p-3">
            <div className="flex items-center gap-2 mb-2">
              <AlertTriangle className="w-4 h-4 text-yellow-600" />
              <span className="font-medium text-sm">Upcoming Fasting Test</span>
            </div>
            <p className="text-sm text-muted-foreground">
              Your Complete Hormone Panel in 5 days requires 8-12 hour fasting. 
              We'll remind you the night before.
            </p>
          </div>

          <div className="space-y-2 text-sm">
            <h4 className="font-medium">General Preparation Tips:</h4>
            <ul className="space-y-1 text-muted-foreground ml-4">
              <li>• Stay hydrated before your test</li>
              <li>• Avoid strenuous exercise 24 hours prior</li>
              <li>• Get adequate sleep the night before</li>
              <li>• Take medications as prescribed unless instructed otherwise</li>
            </ul>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}