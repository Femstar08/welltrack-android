import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { Switch } from "./ui/switch";
import { 
  Users,
  Plus,
  Settings,
  Shield,
  Share2,
  Crown,
  UserPlus,
  Edit,
  Trash2
} from "lucide-react";
import { UserProfile } from "./Profile";

interface FamilyManagementProps {
  mainUser: UserProfile;
  familyMembers: UserProfile[];
}

export function FamilyManagement({ mainUser, familyMembers }: FamilyManagementProps) {
  const [selectedMember, setSelectedMember] = useState<UserProfile | null>(null);

  const familySettings = {
    sharedMealPlanning: true,
    sharedShoppingList: true,
    sharedGoals: false,
    parentalControls: true
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Family Overview */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Users className="w-5 h-5 text-blue-500" />
              Family Account ({familyMembers.length + 1} members)
            </div>
            <Button size="sm">
              <UserPlus className="w-4 h-4 mr-1" />
              Add Member
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Main Account */}
          <Card className="border-blue-200 dark:border-blue-800">
            <CardContent className="p-4">
              <div className="flex items-center gap-4">
                <Avatar className="w-12 h-12">
                  <AvatarImage src={mainUser.avatar} alt={mainUser.name} />
                  <AvatarFallback>{mainUser.name.split(' ').map(n => n[0]).join('')}</AvatarFallback>
                </Avatar>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <h3 className="font-medium">{mainUser.name}</h3>
                    <Crown className="w-4 h-4 text-yellow-500" />
                    <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-950">Main Account</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">{mainUser.email}</p>
                  <p className="text-xs text-muted-foreground">Health Score: {mainUser.healthScore}</p>
                </div>
                <Button variant="outline" size="sm">
                  <Settings className="w-4 h-4" />
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Family Members */}
          {familyMembers.map((member) => (
            <Card key={member.id}>
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <Avatar className="w-12 h-12">
                    <AvatarImage src={member.avatar} alt={member.name} />
                    <AvatarFallback>{member.name.split(' ').map(n => n[0]).join('')}</AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <h3 className="font-medium">{member.name}</h3>
                      {member.age < 18 && (
                        <Badge className="bg-orange-100 text-orange-800 dark:bg-orange-950">Child Account</Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">{member.email}</p>
                    <p className="text-xs text-muted-foreground">
                      Health Score: {member.healthScore} • Age: {member.age}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button variant="outline" size="sm">
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button variant="outline" size="sm" className="text-red-500 hover:text-red-700">
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </CardContent>
      </Card>

      {/* Sharing Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Share2 className="w-5 h-5 text-green-500" />
            Data Sharing Settings
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {[
            { key: 'sharedMealPlanning', label: 'Shared Meal Planning', description: 'All family members can view and edit meal plans' },
            { key: 'sharedShoppingList', label: 'Shared Shopping Lists', description: 'Grocery lists are shared across all accounts' },
            { key: 'sharedGoals', label: 'Family Health Goals', description: 'Set and track goals together as a family' },
            { key: 'parentalControls', label: 'Parental Controls', description: 'Restrict access for child accounts' }
          ].map((setting) => (
            <div key={setting.key} className="flex items-center justify-between">
              <div>
                <p className="font-medium">{setting.label}</p>
                <p className="text-sm text-muted-foreground">{setting.description}</p>
              </div>
              <Switch checked={familySettings[setting.key as keyof typeof familySettings]} />
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Parental Controls */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Shield className="w-5 h-5 text-red-500" />
            Parental Controls
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Manage restrictions and permissions for child accounts in your family.
          </p>
          
          {familyMembers.filter(member => member.age < 18).map((child) => (
            <Card key={child.id} className="border-orange-200 dark:border-orange-800">
              <CardContent className="p-4 space-y-4">
                <div className="flex items-center gap-3">
                  <Avatar className="w-8 h-8">
                    <AvatarImage src={child.avatar} alt={child.name} />
                    <AvatarFallback>{child.name.split(' ').map(n => n[0]).join('')}</AvatarFallback>
                  </Avatar>
                  <h3 className="font-medium">{child.name}</h3>
                </div>
                
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div className="flex items-center justify-between">
                    <span>Can edit meal plans</span>
                    <Switch defaultChecked />
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Can view analytics</span>
                    <Switch defaultChecked={false} />
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Requires approval for goals</span>
                    <Switch defaultChecked />
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Can manage supplements</span>
                    <Switch defaultChecked={false} />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}