import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Switch } from "./ui/switch";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { 
  Palette,
  Bell,
  Smartphone,
  Monitor,
  Moon,
  Sun,
  Volume2,
  VolumeX,
  Wifi,
  WifiOff,
  Sync,
  Clock
} from "lucide-react";
import { AppPreferences, HealthIntegration } from "./Profile";

interface AppSettingsProps {
  preferences: AppPreferences;
  integrations: HealthIntegration[];
}

export function AppSettings({ preferences, integrations }: AppSettingsProps) {
  const [localPreferences, setLocalPreferences] = useState(preferences);

  const themes = [
    { id: 'nature', name: 'Nature', description: 'Green and earthy tones', color: 'bg-green-500' },
    { id: 'calm', name: 'Calm Waters', description: 'Blues and peaceful colors', color: 'bg-blue-500' },
    { id: 'energizing', name: 'Energizing', description: 'Bright and vibrant colors', color: 'bg-orange-500' },
    { id: 'dynamic', name: 'Dynamic', description: 'Modern and bold colors', color: 'bg-purple-500' }
  ];

  const getIntegrationIcon = (type: string) => {
    switch (type) {
      case 'fitness': return '🏃‍♂️';
      case 'health': return '❤️';
      case 'nutrition': return '🍎';
      case 'sleep': return '😴';
      default: return '📱';
    }
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Theme & Appearance */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Palette className="w-5 h-5 text-purple-500" />
            Theme & Appearance
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Theme Selection */}
          <div className="space-y-3">
            <h3 className="font-medium">Color Theme</h3>
            <div className="grid grid-cols-2 gap-3">
              {themes.map((theme) => (
                <Button
                  key={theme.id}
                  variant={localPreferences.theme === theme.id ? "default" : "outline"}
                  className="h-auto p-3 justify-start"
                  onClick={() => setLocalPreferences(prev => ({ ...prev, theme: theme.id as any }))}
                >
                  <div className="flex items-center gap-3">
                    <div className={`w-4 h-4 rounded-full ${theme.color}`} />
                    <div className="text-left">
                      <p className="font-medium">{theme.name}</p>
                      <p className="text-xs text-muted-foreground">{theme.description}</p>
                    </div>
                  </div>
                </Button>
              ))}
            </div>
          </div>

          {/* Dark Mode */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              {localPreferences.darkMode ? <Moon className="w-4 h-4" /> : <Sun className="w-4 h-4" />}
              <span>Dark Mode</span>
            </div>
            <Switch 
              checked={localPreferences.darkMode}
              onCheckedChange={(checked) => setLocalPreferences(prev => ({ ...prev, darkMode: checked }))}
            />
          </div>

          {/* Font Size */}
          <div className="space-y-2">
            <label className="font-medium">Font Size</label>
            <Select 
              value={localPreferences.fontSize}
              onValueChange={(value) => setLocalPreferences(prev => ({ ...prev, fontSize: value as any }))}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="small">Small</SelectItem>
                <SelectItem value="medium">Medium</SelectItem>
                <SelectItem value="large">Large</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Notifications */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="w-5 h-5 text-blue-500" />
            Notifications
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {[
            { key: 'meals', label: 'Meal Reminders', description: 'Get notified about upcoming meals' },
            { key: 'supplements', label: 'Supplement Alerts', description: 'Reminders for supplement schedules' },
            { key: 'goals', label: 'Goal Updates', description: 'Progress updates and achievements' },
            { key: 'reports', label: 'Weekly Reports', description: 'Summary of your health progress' }
          ].map((notification) => (
            <div key={notification.key} className="flex items-center justify-between">
              <div>
                <p className="font-medium">{notification.label}</p>
                <p className="text-sm text-muted-foreground">{notification.description}</p>
              </div>
              <Switch 
                checked={localPreferences.notifications[notification.key as keyof typeof localPreferences.notifications] as boolean}
                onCheckedChange={(checked) => 
                  setLocalPreferences(prev => ({
                    ...prev,
                    notifications: { ...prev.notifications, [notification.key]: checked }
                  }))
                }
              />
            </div>
          ))}
          
          {/* Do Not Disturb */}
          <div className="bg-muted/30 rounded-lg p-4 space-y-3">
            <h4 className="font-medium">Do Not Disturb Hours</h4>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-sm text-muted-foreground">From</label>
                <Select value={localPreferences.notifications.doNotDisturbStart}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {Array.from({ length: 24 }, (_, i) => (
                      <SelectItem key={i} value={`${i.toString().padStart(2, '0')}:00`}>
                        {`${i.toString().padStart(2, '0')}:00`}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="text-sm text-muted-foreground">To</label>
                <Select value={localPreferences.notifications.doNotDisturbEnd}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {Array.from({ length: 24 }, (_, i) => (
                      <SelectItem key={i} value={`${i.toString().padStart(2, '0')}:00`}>
                        {`${i.toString().padStart(2, '0')}:00`}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Health Integrations */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Smartphone className="w-5 h-5 text-green-500" />
            Health Platform Integrations
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {integrations.map((integration) => (
            <Card key={integration.id} className="border border-border/50">
              <CardContent className="p-4">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <span className="text-xl">{getIntegrationIcon(integration.type)}</span>
                    <div>
                      <h3 className="font-medium">{integration.name}</h3>
                      <p className="text-sm text-muted-foreground capitalize">{integration.type}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <Badge className={integration.isConnected ? 'bg-green-100 text-green-800 dark:bg-green-950' : 'bg-gray-100 text-gray-800 dark:bg-gray-950'}>
                      {integration.isConnected ? 'Connected' : 'Disconnected'}
                    </Badge>
                    <Switch checked={integration.isConnected} />
                  </div>
                </div>

                {integration.isConnected && (
                  <div className="space-y-2 text-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Last Sync</span>
                      <span>{integration.lastSync ? new Date(integration.lastSync).toLocaleString() : 'Never'}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Auto Sync</span>
                      <Switch checked={integration.autoSync} size="sm" />
                    </div>
                    <div>
                      <p className="text-muted-foreground mb-1">Data Types:</p>
                      <div className="flex flex-wrap gap-1">
                        {integration.dataTypes.map((dataType) => (
                          <Badge key={dataType} variant="secondary" className="text-xs">
                            {dataType.replace('_', ' ')}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </CardContent>
      </Card>

      {/* Units & Measurements */}
      <Card>
        <CardHeader>
          <CardTitle>Units & Measurements</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="font-medium">Weight</label>
              <Select value={localPreferences.units.weight}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="kg">Kilograms (kg)</SelectItem>
                  <SelectItem value="lbs">Pounds (lbs)</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div className="space-y-2">
              <label className="font-medium">Height</label>
              <Select value={localPreferences.units.height}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="cm">Centimeters (cm)</SelectItem>
                  <SelectItem value="ft">Feet (ft)</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}