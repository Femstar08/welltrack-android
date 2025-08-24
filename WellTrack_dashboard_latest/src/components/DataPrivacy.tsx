import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Switch } from "./ui/switch";
import { 
  Shield,
  Download,
  Trash2,
  Eye,
  EyeOff,
  Lock,
  AlertTriangle,
  Database,
  Cloud,
  MapPin
} from "lucide-react";
import { UserProfile, HealthIntegration } from "./Profile";

interface DataPrivacyProps {
  user: UserProfile;
  integrations: HealthIntegration[];
}

export function DataPrivacy({ user, integrations }: DataPrivacyProps) {
  const privacySettings = {
    dataCollection: true,
    analytics: true,
    locationData: false,
    thirdPartySharing: false,
    marketingEmails: true
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Data Overview */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Database className="w-5 h-5 text-blue-500" />
            Your Data Overview
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center p-4 bg-muted/30 rounded-lg">
              <div className="text-2xl font-semibold text-card-foreground mb-1">2.4 GB</div>
              <p className="text-sm text-muted-foreground">Total Data Stored</p>
            </div>
            <div className="text-center p-4 bg-muted/30 rounded-lg">
              <div className="text-2xl font-semibold text-card-foreground mb-1">347</div>
              <p className="text-sm text-muted-foreground">Days of History</p>
            </div>
          </div>
          
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span>Meal data</span>
              <span>1.2 GB</span>
            </div>
            <div className="flex justify-between text-sm">
              <span>Health metrics</span>
              <span>0.8 GB</span>
            </div>
            <div className="flex justify-between text-sm">
              <span>Photos & images</span>
              <span>0.3 GB</span>
            </div>
            <div className="flex justify-between text-sm">
              <span>Analytics data</span>
              <span>0.1 GB</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Privacy Controls */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Shield className="w-5 h-5 text-green-500" />
            Privacy Controls
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {[
            { 
              key: 'dataCollection', 
              label: 'Data Collection for Improvement', 
              description: 'Allow anonymous usage data to help improve the app',
              icon: <Database className="w-4 h-4" />
            },
            { 
              key: 'analytics', 
              label: 'Analytics & Performance', 
              description: 'Help us understand app performance and crashes',
              icon: <Eye className="w-4 h-4" />
            },
            { 
              key: 'locationData', 
              label: 'Location Data', 
              description: 'Use location for restaurant recommendations',
              icon: <MapPin className="w-4 h-4" />
            },
            { 
              key: 'thirdPartySharing', 
              label: 'Third-Party Data Sharing', 
              description: 'Share anonymized data with research partners',
              icon: <Cloud className="w-4 h-4" />
            }
          ].map((setting) => (
            <div key={setting.key} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
              <div className="flex items-start gap-3">
                {setting.icon}
                <div>
                  <p className="font-medium">{setting.label}</p>
                  <p className="text-sm text-muted-foreground">{setting.description}</p>
                </div>
              </div>
              <Switch checked={privacySettings[setting.key as keyof typeof privacySettings]} />
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Connected Services */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Lock className="w-5 h-5 text-purple-500" />
            Connected Services Privacy
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {integrations.filter(i => i.isConnected).map((integration) => (
            <div key={integration.id} className="flex items-center justify-between p-3 border border-border rounded-lg">
              <div>
                <h3 className="font-medium">{integration.name}</h3>
                <p className="text-sm text-muted-foreground">
                  Sharing: {integration.dataTypes.join(', ')}
                </p>
              </div>
              <Button variant="outline" size="sm">
                <Eye className="w-4 h-4 mr-1" />
                Review
              </Button>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Data Management */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Download className="w-5 h-5 text-orange-500" />
            Data Management
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <Button variant="outline" className="w-full justify-start gap-2">
            <Download className="w-4 h-4" />
            Export All Data
          </Button>
          
          <Button variant="outline" className="w-full justify-start gap-2">
            <Cloud className="w-4 h-4" />
            Backup to Cloud
          </Button>
          
          <div className="border-t border-border pt-4">
            <div className="bg-red-50 dark:bg-red-950 border border-red-200 dark:border-red-800 rounded-lg p-4">
              <div className="flex items-center gap-2 mb-2">
                <AlertTriangle className="w-4 h-4 text-red-500" />
                <h3 className="font-medium text-red-800 dark:text-red-200">Danger Zone</h3>
              </div>
              <p className="text-sm text-red-700 dark:text-red-300 mb-4">
                These actions cannot be undone. Please be certain before proceeding.
              </p>
              <div className="space-y-2">
                <Button variant="outline" className="w-full justify-start gap-2 border-red-200 dark:border-red-800 text-red-600 hover:bg-red-50 dark:hover:bg-red-950">
                  <Trash2 className="w-4 h-4" />
                  Clear All Health Data
                </Button>
                <Button variant="outline" className="w-full justify-start gap-2 border-red-200 dark:border-red-800 text-red-600 hover:bg-red-50 dark:hover:bg-red-950">
                  <Trash2 className="w-4 h-4" />
                  Delete Account Permanently
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Account Security */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Lock className="w-5 h-5 text-red-500" />
            Account Security
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">Two-Factor Authentication</p>
              <p className="text-sm text-muted-foreground">Add an extra layer of security</p>
            </div>
            <Badge className="bg-green-100 text-green-800 dark:bg-green-950">Enabled</Badge>
          </div>
          
          <Button variant="outline" className="w-full justify-start gap-2">
            <Lock className="w-4 h-4" />
            Change Password
          </Button>
          
          <Button variant="outline" className="w-full justify-start gap-2">
            <Eye className="w-4 h-4" />
            View Login Activity
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}